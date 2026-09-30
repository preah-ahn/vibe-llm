# ADR-0001. RAG 어드바이저로 RetrievalAugmentationAdvisor 채택

- 상태: 채택
- 날짜: 2026-09-30

## 배경

Spring AI 로 RAG 를 붙이는 대부분의 예제와 문서는 `QuestionAnswerAdvisor` 를 사용한다.
이 프로젝트는 Spring AI **2.0.1** (Spring Boot 4.1.1) 을 쓴다.

## 조사 결과

Maven Central 메타데이터와 실제 jar 를 확인했다.

- `spring-ai-advisors-vector-store` 는 **`2.0.0-M8` 이 마지막**이며 GA(`2.0.0`, `2.0.1`)에 없다.
- 따라서 `QuestionAnswerAdvisor` 는 2.0.x 에서 **사용할 수 없다.**
- 대체 클래스는 `spring-ai-rag` 아티팩트의 `org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor` 다.

동일 jar 에서 함께 확인한 구성 요소:

| 클래스 | 패키지 |
|---|---|
| `RetrievalAugmentationAdvisor` | `org.springframework.ai.rag.advisor` |
| `VectorStoreDocumentRetriever` | `org.springframework.ai.rag.retrieval.search` |
| `ContextualQueryAugmenter` | `org.springframework.ai.rag.generation.augmentation` |

## 결정

`RetrievalAugmentationAdvisor` 를 사용하고, `build.gradle` 에 `spring-ai-rag` 를 **명시적으로** 선언한다.

```gradle
implementation 'org.springframework.ai:spring-ai-rag'
```

`spring-ai-rag` 는 `spring-ai-starter-model-openai` 와
`spring-ai-starter-vector-store-pgvector` **어느 쪽에서도 전이 포함되지 않는다.**
starter 만 추가하고 넘어가면 컴파일 단계에서 클래스를 찾지 못한다.

## 근거

버전에 따라 API 가 바뀌는 영역이므로 기억이나 블로그 예제가 아니라 실제 아티팩트를 확인했다.
설정 프로퍼티 이름도 같은 방식으로 검증했다 — 2.0.1 의 `spring.ai.openai.embedding.*` 은
flat 키(`model`, `dimensions`, `encoding-format`)와 중첩 `options.*` 를 모두 지원하며,
1.x 의 `embeddings-path` 는 존재하지 않는다.

## 결과

- 컴파일 성공, 빈 생성 성공, RAG 응답 정상.
- 교훈: Spring AI 는 마이너 버전 간 모듈 재편이 잦다. RAG 관련 클래스는
  코드를 쓰기 전에 해당 버전 jar 로 확인한다.

## 참고

- Spring AI issue #7046 — `spring-ai-starter-vector-store-pgvector` 가
  `spring-boot-starter-jdbc` 를 전이로 끌어와 DataSource 미설정 앱의 기동을 깨뜨린다.
  이 프로젝트는 `spring.datasource` 가 이미 있어 해당 없음.
