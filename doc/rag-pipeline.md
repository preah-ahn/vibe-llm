# RAG 파이프라인 상세

## 적재 (DocumentIngestService)

```
MultipartFile
  → vectorStore.delete(source == 파일명)      # 교체 준비
  → DoclingClient.chunk(file)                 # docling 파싱 + 청킹 (호출 1회)
  → Document.builder() + 메타데이터           # source, ingestedAt
  → vectorStore.add(chunks)                   # 임베딩 + 저장
  → chunks.size()
```

### 파싱·청킹 — docling-serve

파싱과 청킹을 모두 docker 로 띄운 docling-serve 에 위임한다. 앱에는 파서도 분할기도 없다.

| 엔드포인트 | 용도 | 호출 시점 |
|---|---|---|
| `POST /v1/chunk/hybrid/file` | HybridChunker 청크 목록 | 분석·적재 |
| `POST /v1/convert/file` (`to_formats=md`) | 문서 전문 마크다운 | 분석만 (화면 표시용) |

**한 번에 둘 다 받을 수 없다.** 청크 응답에는 마크다운이 없고
(`include_converted_doc` 은 DoclingDocument JSON 만 준다), 변환 응답에는 청크가 없다
(`to_formats=chunks` 는 inbody 응답에서 조용히 무시된다). 그래서 **분석은 호출 2회,
적재는 1회**다. 분석이 적재보다 느린 이유가 이것이다.

지원 형식은 docling 이 받는 전부다 — pdf, docx/doc/rtf, pptx/ppt, xlsx/xls, odt/ods/odp,
pages, html/mhtml, md, csv, asciidoc, latex, epub, eml/msg, xml, 이미지(png/jpg/tiff 등),
오디오·비디오, vtt. `.txt` 는 docling 의 `InputFormat` enum 에 없지만 마크다운으로
처리된다(실측 확인).

형식 판별은 docling 이 **파일명 확장자**로 한다. 그래서 멀티파트 파트에 Content-Type 을
지정하지 않는다 — `application/octet-stream` 으로 보내도 정상 인식한다(실측 확인).

OCR 이 기본 켜짐이라 스캔 PDF 와 이미지에서도 텍스트가 나온다. Tika 시절의
"텍스트 레이어 없으면 0청크" 전제는 더 이상 일반적이지 않다.

### 타임아웃

```java
SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
f.setConnectTimeout(Duration.ofSeconds(10));
f.setReadTimeout(readTimeout);   // docling.read-timeout, 기본 10m
```

`docling-serve-cpu` 는 CPU 로만 모델을 돌린다. 문서 한 건 변환에 수십 초가 걸리고
분석은 그걸 두 번 한다. RestClient 기본 타임아웃으로는 끊긴다.

### 청킹 옵션

`chunking_max_tokens` 만 지정하고 나머지는 docling HybridChunker 기본값을 쓴다.

| 옵션 | 값 | 의미 |
|---|---|---|
| `chunking_max_tokens` | **1000** (`DoclingClient.CHUNK_MAX_TOKENS`) | 청크 토큰 상한. 미지정 시 docling 은 250 을 쓴다 |
| `chunking_tokenizer` | `sentence-transformers/all-MiniLM-L6-v2` (기본값) | 청크 크기 판단 기준 |
| `chunking_merge_peers` | `true` (기본값) | 같은 제목 아래 짧은 청크 병합 |

**overlap 은 지정할 수 없다.** docling-serve v1.35.0 의 openapi 전체에 `overlap` 이라는
문자열이 없다 — HybridChunker·HierarchicalChunker 어느 쪽 옵션에도 없다. 제거한
`TokenTextSplitter` 도, Spring AI 2.0.1 의 다른 분할기도 마찬가지다(`TextSplitter` 와
`TokenTextSplitter` 둘뿐). 오버랩이 필요하면 앱에서 직접 이어붙여야 한다.

실측 — 30,273자 단일 제목 문서:

| `chunking_max_tokens` | 청크 수 | num_tokens |
|---|---|---|
| 미지정 (docling 기본) | 20 | 전부 250 |
| 400 | 14 | 127 ~ 373 |
| **1000 (현재)** | 5 | 전부 988 |

청크 텍스트(`text`)는 **contextualized** 다 — 그 청크가 속한 상위 제목들이 본문 앞에
붙는다. 예:

```
Work Contract
Article 2 (Wage)
Base, Amount = 3000. Bonus, Amount = 500
```

제목 문맥이 벡터에 함께 들어가 검색에 유리하므로 이 텍스트를 그대로 임베딩한다.
표는 기본 설정에서 마크다운이 아니라 위처럼 triplet 으로 직렬화된다
(`chunking_use_markdown_tables` 로 바꿀 수 있으나 기본값을 유지한다).

토큰 수(`num_tokens`)도 docling 이 함께 준다. 이 값은 위 토크나이저 기준이므로
임베딩 모델(Qwen3)의 실제 토크나이저와는 다르다. 화면 표시는 근사치로 읽어야 한다.

### 메타데이터

| 키 | 값 | 용도 |
|---|---|---|
| `source` | 원본 파일명 | 재적재 교체 기준, 인용 표기 |
| `ingestedAt` | ISO-8601 문자열 | 적재 시각 추적 |

`Instant` 객체가 아니라 **문자열**로 저장한다. 메타데이터는 JSON 으로 직렬화되므로
시간 객체를 그대로 넣으면 직렬화·필터링이 깨진다.

docling 청크는 이미 분할된 상태로 오므로, 청크마다 `Document.builder()` 로 직접 만들면서
메타데이터를 심는다. Tika + `TokenTextSplitter` 시절의 "split 이전에 부모 문서에 한 번만
심는다" 요령은 더 이상 해당 없다.

### 청킹 방식이 검색 품질을 좌우한다

TokenTextSplitter 시절 동일 문서·동일 질문("주말 온콜 근무자 보상은?")에 대한 실측:

| 청킹 방식 | 유사도 |
|---|---|
| 문서 전체가 한 청크 (chunkSize 600) | **0.32** |
| 조항 단위로 분리 (chunkSize 300) | **0.70** |

청크가 커지면 한 벡터에 여러 주제가 섞여 특정 질문과의 유사도가 희석된다. 이 수치는
docling 도입 **이전** 측정값이지만, 청킹 경계가 검색 품질을 좌우한다는 결론은 그대로다.

`TokenTextSplitter` 는 토큰 수만 보고 잘라서 문장 중간에서 끊겼다. 실제로 "대체 휴가
1.5일" 이 두 청크로 갈라져 모델이 두 값을 상충으로 해석한 사례가 있었다. HybridChunker
는 문서 구조(제목·문단·표)를 먼저 보고 나눈 뒤 토큰 상한으로 조정하므로 이 유형의 절단이
줄어든다. 다만 docling 도입 후 유사도는 **재측정하지 않았다** — 위 표는 과거 기록이다.

### 재적재 (교체 동작)

```java
vectorStore.delete(new FilterExpressionBuilder().eq("source", source).build());
```

같은 파일명으로 다시 올리면 이전 청크를 **먼저 지운다**. 이것이 단순 upsert 보다 안전한 이유:
문서가 줄어든 경우(v1 은 12청크, v2 는 7청크) upsert 만으로는 고아 청크 5개가 남아
계속 검색에 걸린다.

검증:

```sh
curl -F "file=@sample.docx" http://localhost:8081/documents   # chunks: 2
curl -F "file=@sample.docx" http://localhost:8081/documents   # chunks: 2
```

```sql
SELECT count(*) FROM vector_store;   -- 여전히 2
```

### 빈 결과 처리

docling 이 추출할 텍스트를 찾지 못하면 청크가 0개다. 이때 `vectorStore.add()` 를
건너뛰고 `chunks: 0` 을 반환한다. 의미 없는 벡터를 저장하지 않는다.

OCR 이 기본으로 켜져 있어 스캔 PDF·이미지도 대개 텍스트가 나온다. 0청크는
빈 파일이나 docling 이 다루지 못하는 내용일 때 주로 발생한다.

## 검색 (ragChatClient)

```java
DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
        .vectorStore(vectorStore)
        .topK(4)
        .similarityThreshold(0.2)
        .build();
```

### 임계값을 낮게 둔 이유

실측 데이터(저장된 청크 대상, 최고 유사도):

| 질문 | 문서에 존재? | 유사도 |
|---|---|---|
| VIBE-2291 롤백 기준 오류율 | ✅ | 0.5537 |
| API 키 보관 위치 | ✅ | 0.4036 |
| 프로덕션 배포 시점 | ✅ | 0.3987 |
| 주말 온콜 보상 | ✅ | 0.3011 |
| SEV-2 의 뜻 | ✅ | 0.2692 |
| 김치찌개 끓이는 방법 | ❌ | **0.3101** |
| 파이썬 웹 크롤러 | ❌ | 0.3054 |

무관한 질문(0.3101)이 문서에 실제로 있는 질문(0.3011, 0.2692)보다 **높다.**
즉 어떤 임계값을 골라도 관련/무관을 가를 수 없다. 초기값 0.5 는 진짜 근거를
조용히 버려서, 문서에 답이 있는데도 일반 지식으로 답하는 결과를 냈다.

결론: 임계값은 순수 노이즈 차단용 하한(0.2)으로만 쓰고, 관련성 판단은
`topK` 로 후보를 좁힌 뒤 **LLM 에 맡긴다**. 이 판단 규칙이 `rag-augment.st` 에 있다.

### 발췌 포매팅

```java
.documentFormatter(documents -> documents.stream()
        .map(d -> "[출처: " + d.getMetadata().get("source") + "]\n" + d.getText())
        .collect(Collectors.joining("\n\n")))
```

기본 `documentFormatter` 는 본문 텍스트만 넘긴다. 그러면 모델이 파일명을 알 수 없어
출처 인용이 불가능하다. 각 발췌 앞에 `[출처: 파일명]` 을 붙여 해결한다.

### 증강 템플릿

`ContextualQueryAugmenter` 의 기본 템플릿은 **영어**이며 다음 문장을 포함한다.

```
Given the context information and no prior knowledge, answer the query.
1. If the answer is not in the context, just say that you don't know.
```

이 지시가 **user 메시지**에 들어가므로 한국어 system 프롬프트보다 강하게 작동한다.
실제로 문서와 무관한 질문에 영어로 거절하는 응답이 나왔다. 그래서
`prompt/rag-augment.st` 로 교체했다.

필수 placeholder — `{context}`, `{query}`. 누락 시 빈 생성 시점에 즉시 실패한다.

규칙 요약:

1. 발췌가 관련 있으면 발췌만 근거로 답하고 `근거: 파일명` 표기
2. 발췌가 무관하면 무시하고 일반 지식으로 답하되 근거 없음을 밝힘
3. "발췌에 따르면" 류 표현 금지
4. 한국어로 답변

### allowEmptyContext

```java
.allowEmptyContext(true)
```

기본값 `false` 는 검색 결과가 0건일 때 "정보가 부족하다"는 고정 응답을 낸다.
학습 프로젝트는 벡터 스토어가 빈 상태로 시작하므로, `false` 면 첫 업로드 전
모든 질문이 거절된다. `true` 로 두어 일반 대화가 가능하게 했다.

## 튜닝 파라미터 정리

| 파라미터 | 위치 | 현재 값 | 영향 |
|---|---|---|---|
| `chunking_max_tokens` | `DoclingClient` | **1000** | 검색 정확도. 크면 유사도 희석, 작으면 문맥 손실 |
| 나머지 청킹 옵션 | docling HybridChunker | 기본값 | overlap 은 docling 이 지원하지 않는다 |
| `docling.base-url` | `application.yaml` | `http://localhost:50010` | compose 의 포트 매핑과 일치해야 함 |
| `docling.read-timeout` | `application.yaml` | 10m | CPU 변환이 느리다. 짧으면 큰 문서에서 끊김 |
| `topK` | `AiConfig` | 4 | 주입 발췌 수. 늘리면 컨텍스트 토큰 소모 증가 |
| `similarityThreshold` | `AiConfig` | 0.2 | 노이즈 하한. 높이면 진짜 근거가 누락됨 |
| `maxMessages` | `AiConfig` | 5 | 대화 메모리 창 |
| `dimensions` | `application.yaml` | 1024 | 임베딩 모델과 **반드시** 일치 |
| `max-tokens` | `application.yaml` | 2048 | 응답 길이 상한 |

`topK` × 청크 크기가 `max-tokens` 와 모델 컨텍스트를 함께 소모한다. 청크 크기를 더 이상
앱에서 고정하지 않으므로, 컨텍스트 소모량은 문서 구조에 따라 달라진다. 발췌가 길어
응답이 잘린다면 `topK` 를 먼저 낮춘다.

## 로컬 임베딩 백엔드 주의사항

| 위험 | 증상 | 대응 |
|---|---|---|
| 차원 불일치 | 첫 적재 시 `expected N dimensions` | `DROP TABLE vector_store;` 후 재기동 |
| 배치 요청 | 400 또는 `data` 개수 부족 | `BatchingStrategy` 빈으로 배치 축소 |
| `encoding-format` | 역직렬화 오류 또는 전부 0 벡터 | `float` 고정 (현재 설정) |
| 청크 토큰 초과 | 조용한 절삭 | LM Studio 의 embedding `n_ctx` 확인 후 `CHUNK_MAX_TOKENS` 하향 |
| 모델 언로드 | `/v1/embeddings` 404 | 두 모델 동시 로드 또는 적재 후 대화 |
| 콜드 스타트 | 첫 호출만 타임아웃 | `spring.ai.openai.embedding.timeout` 상향 |

`encoding-format: float` 을 명시한 이유는, base64 요청에 float 응답이 오는
불일치가 발생하면 **오류 없이 0 벡터**가 저장되어 그럴듯하지만 무의미한
유사도 점수가 나오기 때문이다. 가장 찾기 어려운 실패 유형이다.
