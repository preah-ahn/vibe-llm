# ADR-0004. 문서 파싱과 청킹을 docling-serve 에 위임

- 상태: 채택
- 날짜: 2026-09-30

## 배경

적재 파이프라인은 Tika + `TokenTextSplitter` 조합이었다.

```java
List<Document> parsed = new TikaDocumentReader(file.getResource()).get();
return TokenTextSplitter.builder()
        .withChunkSize(300)
        .withMinChunkSizeChars(100)
        .build()
        .apply(parsed);
```

두 가지 한계가 있었다.

**파싱** — Tika 는 문서를 평문으로 떨어뜨린다. 제목 계층, 표, 목록 구조가 사라져서
표 한 줄이 셀 구분 없이 이어붙은 문자열이 된다. 스캔 PDF 는 빈 문자열을 반환한다.

**청킹** — `TokenTextSplitter` 는 토큰 수만 보고 자른다. 문서 구조를 모르므로
문장 중간에서 끊긴다. 실제로 "대체 휴가 1.5일" 이 두 청크로 갈라져 모델이 두 값을
상충으로 해석한 사례가 있었다. overlap 옵션도 없다(`withChunkSize`,
`withMinChunkSizeChars`, `withMinChunkLengthToEmbed`, `withMaxNumChunks`,
`withKeepSeparator`, `withPunctuationMarks` 만 존재).

compose 에는 이미 `docling-serve-cpu` 가 떠 있었으나 "파싱 평가용, 앱 기본 파서는
Tika" 로 놀고 있었다.

## 결정

파싱과 청킹을 **모두** docling-serve 에 위임한다. 앱에서 파서와 분할기를 제거한다.

| 엔드포인트 | 용도 | 호출 시점 |
|---|---|---|
| `POST /v1/chunk/hybrid/file` | HybridChunker 청크 목록 | 분석·적재 |
| `POST /v1/convert/file` (`to_formats=md`) | 문서 전문 마크다운 | 분석만 |

청킹 옵션은 **전부 docling 기본값**을 쓴다. 앱이 넘기는 것은 파일뿐이다.

```java
MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
body.add("files", file.getResource());   // 이게 전부
```

제거한 의존성: `spring-ai-tika-document-reader`, jtokkit(cl100k 토큰 카운팅).

## 근거

### 분석에서만 docling 을 두 번 호출하는 이유

한 번의 호출로 청크와 마크다운을 함께 받을 방법을 찾지 못했다. 실측으로 확인한 것:

| 시도 | 결과 |
|---|---|
| `/v1/chunk/hybrid/file` + `include_converted_doc=true` | `json_content`(DoclingDocument)만 옴. `md_content` 는 `null` |
| `/v1/chunk/hybrid/file` + `to_formats=md` | 해당 파라미터가 스키마에 없어 무시됨 |
| `/v1/convert/file` + `to_formats=["md","chunks"]` | 200 이지만 `ExportDocumentResponse` 에 청크 필드가 없어 마크다운만 옴 |

DoclingDocument JSON 을 앱에서 마크다운으로 렌더하려면 docling 의 직렬화 규칙을 Java 로
다시 구현해야 한다. 호출 한 번을 아끼려고 파서를 또 만드는 것은 본말전도다.

그래서 분석은 2회, 적재는 1회다. 마크다운은 화면 표시 전용이며 적재 결과에 영향이 없다.

### 청킹 기본값을 그대로 쓰는 이유

`chunking_max_tokens`, `chunking_tokenizer`, `chunking_merge_peers` 를 노출하면
"언제 무엇을 바꿔야 하는가"라는 질문이 따라온다. 그 질문에 답할 실측 데이터가 아직 없다.
근거 없는 설정 항목은 늘어나기만 하고 아무도 건드리지 않는다.

기본값으로 확인한 동작 (3청크 테스트 문서):

```
Work Contract
Article 2 (Wage)
Base, Amount = 3000. Bonus, Amount = 500
```

청크 텍스트는 **contextualized** 다 — 상위 제목이 본문 앞에 붙는다. 제목 문맥이 벡터에
함께 들어가므로 이 텍스트를 그대로 임베딩한다. 표는 기본값에서 마크다운이 아니라 위처럼
triplet 으로 직렬화된다(`chunking_use_markdown_tables` 로 변경 가능하나 기본값 유지).

### 토큰 수 출처 변경

jtokkit(cl100k)로 직접 세던 것을 docling 이 주는 `num_tokens` 로 바꿨다. 청킹 주체가
바뀌었으니 토큰 수도 그 주체의 토크나이저(`all-MiniLM-L6-v2`) 기준이어야 일관된다.
둘 다 임베딩 모델(Qwen3)의 실제 토크나이저와 다르다는 점은 그대로이므로,
화면 캡션에 근사치임을 계속 밝힌다.

### 업로드 형식 제한 해제

Tika 기준으로 pdf/doc/docx/ppt/pptx/html/txt 만 받던 것을 docling 이 받는 전 형식으로
넓혔다. `InputFormat` enum 기준 — pdf, docx/doc/rtf, pptx/ppt, xlsx/xls, odt/ods/odp,
pages, html/mhtml, md, csv, asciidoc, latex, epub, eml/msg, xml, json, 이미지, 오디오,
비디오, vtt.

형식 판별은 docling 이 **파일 확장자**로 한다. 그래서 멀티파트 파트에 Content-Type 을
지정하지 않는다 — `application/octet-stream` 으로 보내도 정상 인식함을 실측 확인했다.

`.txt` 는 `InputFormat` enum 에 없지만 마크다운으로 처리된다(실측 확인). 그래서
`accept` 목록에 유지했다.

## 감수한 비용

**속도** — `docling-serve-cpu` 는 CPU 로만 모델을 돌린다. 문서 한 건에 수십 초가 걸리고
분석은 그걸 두 번 한다. `docling.read-timeout` 기본값을 10분으로 두고, RestClient 에
`SimpleClientHttpRequestFactory` 로 명시적 타임아웃을 건다. 기본 타임아웃으로는 끊긴다.

**단일 장애점** — docling-serve 가 내려가면 문서 적재가 전부 실패한다. 대체 파서를 두지
않았다. Tika 폴백을 남기면 "어느 파서가 만든 청크인지"에 따라 검색 결과가 달라져,
디버깅이 어려운 비결정성이 생긴다. 실패는 명시적인 편이 낫다.

**측정 공백** — ADR-0002 의 유사도 실측(0.32 vs 0.70)은 TokenTextSplitter 기준이다.
docling 청킹으로 **재측정하지 않았다.** `similarityThreshold` 0.2 는 그 측정에 근거한
값이므로, 재측정 전까지는 근거가 한 단계 약해진 상태다.

## 결과

- `./gradlew compileJava` 통과
- `POST /documents/analysis` (3청크 테스트 문서) — 200, `chunks` 3개, `num_tokens` 16/18/18,
  `markdown` 에 제목·표 구조 보존 확인
- `POST /documents` — 200, `{"chunks":3}`, pgvector 저장 확인
- 브라우저 — 마크다운 패널에 제목·표 렌더, 렌더/원문 토글 동작, 한글 파일명 정상
- 화면 변경은 [design.md](../design.md), 파이프라인 상세는
  [rag-pipeline.md](../rag-pipeline.md) 참고
