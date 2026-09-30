# API 명세

- Base URL: `http://localhost:8081`
- OpenAPI: `/v3/api-docs`, Swagger UI: `/swagger-ui.html`
- 별도 인증 없음 (로컬 학습용)

응답 본문은 대부분 `text/plain` 문자열이다. 전용 응답 DTO 는 적재 API 에만 있다.

## 엔드포인트 요약

| Method | Path | 용도 | RAG |
|---|---|---|---|
| POST | `/documents/analysis` | 적재 전 미리보기 (저장·임베딩 없음) | — |
| POST | `/documents` | 문서 적재 (청킹·임베딩·저장) | — |
| GET | `/documents` | 적재된 문서 목록 | — |
| DELETE | `/documents` | 문서 단위 삭제 | — |
| GET | `/rag-chats` | 문서 근거 기반 대화 | ✅ |
| GET | `/rag-chat-streams` | 위의 스트리밍 변형. 비교 화면이 쓴다 | ✅ |
| GET | `/chats` | 일반 동기 대화 | ❌ |
| GET | `/plain-chat-streams` | 위의 스트리밍 변형. 비교 화면이 쓴다 | ❌ |
| GET | `/chat-streams` | 개념 설명 전용 스트리밍 (프롬프트 템플릿 적용) | ❌ |

화면(`/ui`, `/ui/documents`, `/ui/stream`)은 REST 가 아니라 Thymeleaf 페이지다.
[design.md](design.md) 참고.

---

## POST /documents/analysis

적재하면 어떻게 잘리는지 미리 보여준다. **저장하지 않고 임베딩도 호출하지 않는다.**

**Content-Type:** `multipart/form-data` — part 이름은 `file`, `POST /documents` 와 동일하다.

**응답 200** — `AnalysisResponse`

```json
{
  "source": "vibe-rag-spec.docx",
  "sizeBytes": 1583,
  "textLength": 475,
  "chunkCount": 2,
  "totalTokens": 436,
  "embeddingModel": "text-embedding-qwen3-embedding-0.6b",
  "dimensions": 1024,
  "existingChunks": 2,
  "existingIngestedAt": "2026-09-30T03:31:23.100365800Z",
  "markdown": "# 바이브 사내 규정 VIBE-2291\n\n## 1. 배포 창구\n\n| 항목 | 내용 |\n|---|---|\n…",
  "chunks": [
    { "index": 0, "chars": 320, "tokens": 297, "text": "바이브 사내 규정 VIBE-2291 문서\n1. 배포 창구\n…" }
  ]
}
```

| 필드 | 설명 |
|---|---|
| `textLength` | 청크 본문 길이의 합 (문자) |
| `totalTokens` | 청크 토큰 수의 합 |
| `embeddingModel` / `dimensions` | 적재 시 사용될 설정값. 설정을 읽을 뿐 모델을 호출하지 않는다 |
| `existingChunks` | 같은 파일명으로 이미 적재된 청크 수. `0` 이면 신규, `>0` 이면 적재 시 교체됨 |
| `existingIngestedAt` | 기존 적재 시각. 신규면 `null` |
| `markdown` | docling 이 변환한 **문서 전문** 마크다운. 화면 표시용이며 적재되지 않는다 |
| `chunks[].text` | 청크 **전문**. 자르지 않는다 |

청크 경계는 `POST /documents` 와 동일한 docling 호출로 계산하므로 **실제 적재 결과와 일치한다.**

`tokens` 는 docling HybridChunker 의 토크나이저(`all-MiniLM-L6-v2`) 기준이며,
임베딩 모델의 실제 토크나이저와는 다를 수 있다.

이 엔드포인트만 docling 을 **두 번** 호출한다 — 청킹(`/v1/chunk/hybrid/file`) 과
마크다운 변환(`/v1/convert/file`). 그래서 `POST /documents` 보다 느리다.

```sh
curl -F "file=@sample.pdf" http://localhost:8081/documents/analysis
```

---

## POST /documents

문서를 파싱·청킹·임베딩해 pgvector 에 저장한다.

**Content-Type:** `multipart/form-data`

| Part | 타입 | 필수 | 설명 |
|---|---|---|---|
| `file` | file | ✅ | docling 이 지원하는 모든 형식 (아래) |

지원 형식 — pdf, docx/doc/rtf, pptx/ppt, xlsx/xls, odt/ods/odp, pages, html/mhtml,
md/txt, csv, asciidoc, latex, epub, eml/msg, xml, json, 이미지(png/jpg/tiff/bmp/webp/gif),
오디오·비디오(wav/mp3/mp4 등), vtt. 형식 판별은 docling 이 **파일 확장자**로 한다.

**응답 200** — `IngestResponse`

```json
{ "source": "vibe-rag-spec.docx", "chunks": 2 }
```

| 필드 | 설명 |
|---|---|
| `source` | 원본 파일명. 메타데이터 키이자 재적재 시 교체 기준 |
| `chunks` | 저장된 청크 수. `0` 이면 추출된 텍스트가 없음 |

**동작 특성**

- **교체 방식** — 같은 파일명으로 다시 올리면 이전 청크를 먼저 삭제한다. 중복 누적 없음.
- **`chunks: 0`** — docling 이 추출한 텍스트가 없는 경우. 오류가 아니라 정상 응답이며 저장은 건너뛴다.
  OCR 이 기본 켜짐이라 스캔 PDF·이미지도 대개 텍스트가 나온다.
- **업로드 한도** — 20MB (`spring.servlet.multipart.max-file-size`). 초과 시 413.
- **응답 시간** — docling-serve-cpu 는 문서 한 건에 수십 초가 걸릴 수 있다.
  읽기 타임아웃은 `docling.read-timeout`(기본 10m).
- **docling 장애** — docling-serve 가 내려가 있으면 5xx 를 반환한다. 앱에 대체 파서는 없다.
- 서버에 파일을 보관하지 않는다. 벡터와 메타데이터만 남는다.

```sh
curl -F "file=@sample.pdf" http://localhost:8081/documents
```

---

## GET /documents

적재된 문서를 `source` 메타데이터 기준으로 집계해 반환한다.
`VectorStore` 에는 목록 API 가 없어 `vector_store` 테이블을 직접 조회한다.

**응답 200** — `List<DocumentSummary>`

```json
[
  { "source": "vibe-rag-spec.docx", "chunks": 2, "ingestedAt": "2026-09-30T03:31:23.100365800Z" }
]
```

| 필드 | 설명 |
|---|---|
| `source` | 파일명 |
| `chunks` | 해당 문서의 청크 수 |
| `ingestedAt` | 적재 시각 (UTC ISO-8601). 같은 문서의 청크 중 최댓값 |

적재 시각 내림차순 정렬. 적재된 문서가 없으면 빈 배열.

---

## DELETE /documents

해당 `source` 의 모든 청크를 삭제한다. 되돌릴 수 없다.

| Param | 필수 | 설명 |
|---|---|---|
| `source` | ✅ | 삭제할 파일명 |

**응답 200** — `DeleteResponse`

```json
{ "source": "vibe-rag-spec.docx", "deleted": 2 }
```

없는 `source` 를 지정해도 200 에 `deleted: 0` 을 반환한다.

```sh
curl -X DELETE "http://localhost:8081/documents?source=sample.pdf"
```

---

## GET /rag-chats

pgvector 에서 관련 문서 발췌를 찾아 프롬프트에 주입한 뒤 응답한다.

| Param | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| `query` | query | ✅ | — | 질문 |
| `conversationId` | query | | `default` | 대화 메모리 키 |

**응답 200** — `text/plain`

```sh
curl "http://localhost:8081/rag-chats?query=SEV-2%20%EB%8A%94%20%EB%AC%B4%EC%8A%A8%20%EB%9C%BB%EC%9D%B8%EA%B0%80"
```

**동작 특성**

- 검색 결과가 질문과 관련 있으면 발췌만을 근거로 답하고 `근거: 파일명` 을 덧붙이려 시도한다.
- 관련 발췌가 없으면 발췌를 무시하고 일반 지식으로 답한다 (`allowEmptyContext: true`).
  즉 문서를 하나도 올리지 않은 상태에서도 정상 응답한다.
- 인용 표기와 "근거를 찾지 못했습니다" 안내는 `rag-augment.st` 규칙에 따른 것으로,
  4B 모델이라 항상 형식을 지키지는 않는다.

---

## GET /rag-chat-streams

`/rag-chats` 와 **프롬프트·어드바이저·파라미터가 완전히 같고** 전달 방식만 스트리밍이다.
`/ui` 비교 화면이 쓴다.

| Param | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| `query` | query | ✅ | — | 질문 |
| `conversationId` | query | | `default` | 대화 메모리 키 |

**응답 200** — `Flux<String>` 스트리밍

```sh
curl -N "http://localhost:8081/rag-chat-streams?query=...&conversationId=c1"
```

검색과 증강은 첫 토큰이 나오기 전에 끝난다. 그래서 첫 조각까지의 지연이
`/plain-chat-streams` 보다 길다.

---

## GET /chats

RAG 를 쓰지 않는 기존 동기 대화. RAG 효과 비교용 대조군.

| 위치 | 이름 | 필수 | 설명 |
|---|---|---|---|
| query | `query` | ✅ | 질문 |
| header | `userId` | ✅ | 대화 메모리 키로 사용 |

```sh
curl -H "userId: u1" "http://localhost:8081/chats?query=..."
```

`userId` 헤더가 없으면 400.

---

## GET /plain-chat-streams

`/chats` 와 **프롬프트가 완전히 같고** 전달 방식만 스트리밍이다. `/ui` 비교 화면이 쓴다.

| 위치 | 이름 | 필수 | 설명 |
|---|---|---|---|
| query | `query` | ✅ | 질문 |
| header | `userId` | ✅ | 대화 메모리 키로 사용 |

**응답 200** — `Flux<String>` 스트리밍

```sh
curl -N -H "userId: u1" "http://localhost:8081/plain-chat-streams?query=..."
```

**`/chat-streams` 와 다른 점** — 프롬프트 템플릿을 적용하지 않는다. `/chat-streams` 는
질문을 "«X» 개념을 상세히 설명해 주세요." 로 렌더하므로, 비교 화면에서 쓰면 두 패널의
user 메시지가 서로 달라져 비교가 성립하지 않는다.

---

## GET /chat-streams

RAG 를 쓰지 않는 개념 설명 전용 스트리밍 대화. `prompt/system-message.st`(Java 코딩 가이드
페르소나)와 `prompt/user-message.st`(`{concept}` 치환)를 사용한다. `/ui/stream` 화면이 쓴다.

| Param | 필수 | 기본값 | 설명 |
|---|---|---|---|
| `query` | | `안녕` | `{concept}` 에 치환될 값 |
| `conversationId` | | `default` | 대화 메모리 키 |

**응답 200** — `Flux<String>` 스트리밍

```sh
curl -N "http://localhost:8081/chat-streams?query=record&conversationId=c1"
```

---

## 공통 오류

| 상태 | 원인 |
|---|---|
| 400 | 필수 파라미터/헤더 누락 |
| 400 | 한글 쿼리 인코딩 깨짐 — [setup.md](setup.md#한글-쿼리-인코딩-주의) 참고 |
| 413 | 업로드 파일이 20MB 초과 (`MaxUploadSizeExceededException` → `CONTENT_TOO_LARGE`) |
| 500 | LM Studio 미기동, 모델 언로드, 임베딩 차원 불일치 |
