# vibe-llm 문서

로컬 LLM(LM Studio) + pgvector + docling-serve 기반 RAG 학습 프로젝트의 설계 문서 모음.

## 무엇을 하는 프로젝트인가

1. **문서 적재** — pdf/docx 등을 업로드하면 docling-serve 가 파싱·청킹하고 임베딩해 pgvector 에 저장
2. **근거 기반 대화** — 질문할 때 pgvector 를 조회해 관련 문서 발췌를 프롬프트에 주입

기존의 일반 채팅 엔드포인트(`/chats`, `/chat-streams`)와 RAG 엔드포인트(`/rag-chats`)가
**별도로** 존재하므로, 같은 질문을 양쪽에 던져 RAG 효과를 바로 비교할 수 있다.
Thymeleaf 화면(`/ui`)에서 이 비교를 나란히 볼 수 있다.

## 문서 목록

| 문서 | 내용 |
|---|---|
| [setup.md](setup.md) | 로컬 환경 구성 및 실행·검증 절차 |
| [architecture.md](architecture.md) | 컴포넌트 구조, 빈 구성, 데이터 흐름, DB 스키마 |
| [api-spec.md](api-spec.md) | 엔드포인트 명세 |
| [rag-pipeline.md](rag-pipeline.md) | 적재/검색 파이프라인 상세와 튜닝 파라미터 |
| [design.md](design.md) | 화면 설계 — 라우팅, 화면별 구성, 스타일, 접근성 |
| [adr/](adr/) | 주요 설계 결정 기록 |

## 기술 스택

| 항목 | 값 |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 |
| Spring AI | 2.0.1 (BOM 관리) |
| Gradle | 8.14.3 (wrapper) |
| Web | Spring MVC (`spring-boot-starter-webmvc`) |
| 화면 | Thymeleaf + Bootstrap 5.3 + marked + DOMPurify (CDN) |
| LLM 백엔드 | LM Studio (OpenAI 호환 API) |
| Chat 모델 | `qwen3.5-4b-super-coder` |
| 임베딩 모델 | `text-embedding-qwen3-embedding-0.6b` (1024차원) |
| 벡터 스토어 | pgvector (`pgvector/pgvector:pg17`) |
| 문서 파서·청커 | docling-serve (`docling-serve-cpu:v1.35.0`, HybridChunker) |
| 대화 메모리 | Spring AI JDBC Chat Memory (동일 Postgres) |
| API 문서 | springdoc-openapi (`/swagger-ui.html`) |

## 설계 결정 요약

네 가지 결정은 모두 실측에 근거한다. 상세는 [adr/](adr/) 참고.

- [ADR-0001](adr/0001-retrieval-augmentation-advisor.md) — Spring AI 2.0.x 에는 `QuestionAnswerAdvisor` 가 없다. `RetrievalAugmentationAdvisor` 사용.
- [ADR-0002](adr/0002-similarity-threshold.md) — `similarityThreshold` 로는 관련/무관 질문을 가를 수 없어 0.2 로 낮추고 판단을 LLM 에 맡겼다.
- [ADR-0003](adr/0003-separate-rag-chat-client.md) — RAG 는 별도 `ChatClient` 빈으로 분리해 기존 엔드포인트를 보존한다.
- [ADR-0004](adr/0004-docling-parsing-chunking.md) — Tika + `TokenTextSplitter` 를 버리고 파싱·청킹을 docling-serve 에 위임한다.
