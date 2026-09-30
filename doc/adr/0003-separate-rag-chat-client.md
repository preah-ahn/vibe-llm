# ADR-0003. RAG 를 별도 ChatClient 빈 + 별도 엔드포인트로 분리

- 상태: 채택
- 날짜: 2026-09-30

## 배경

기존에는 `ChatClient` 빈이 하나였고 `/chats`, `/chat-streams` 가 이를 공유했다.
RAG 를 붙이는 방법은 세 가지였다.

1. 기존 `ChatClient` 의 `defaultAdvisors` 에 RAG 어드바이저 추가 (전역 적용)
2. 기존 엔드포인트에 `?rag=true` 플래그로 런타임 분기
3. 별도 `ChatClient` 빈 + 별도 엔드포인트

## 결정

**3번** — `ragChatClient` 빈과 `GET /rag-chats` 를 신설한다. 기존 두 엔드포인트는 무변경.

```java
@Bean
@Primary
public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) { ... }

@Bean
public ChatClient ragChatClient(ChatClient.Builder builder, ChatMemory chatMemory, VectorStore vectorStore) {
    return builder.clone()
            .defaultAdvisors(a -> a.advisors(memoryAdvisor, ragAdvisor, new SimpleLoggerAdvisor())
                    .param(ChatMemory.CONVERSATION_ID, "default"))
            .build();
}
```

## 근거

**1번(전역 적용)을 기각한 이유**

- `/chat-streams` 는 user 메시지가 `user-message.st` 로 렌더링된
  "«X» 개념을 상세히 설명해 주세요." 문장이다. 검색 질의가 개념어가 아니라
  이 문장 전체가 되어 검색 품질이 떨어진다.
- 기존 두 엔드포인트의 응답이 벡터 스토어 내용에 의존하게 된다.
- RAG 유무 비교가 불가능해진다. 학습 프로젝트에서 이 비교가 핵심 가치다.

**2번(런타임 플래그)을 기각한 이유** — 엔드포인트는 안 늘지만 서비스 계층에 분기가 생기고,
기존 메서드 시그니처를 건드려야 한다.

**3번의 비용** — `AiConfig` 에 빈 하나(약 25줄)와 컨트롤러·서비스 각 1개.
그 대가로 기존 동작이 바이트 단위로 보존되고 대조 실험이 가능하다.

## 구현 세부

### builder.clone() 이 필수인 이유

`ChatClient.Builder` 의 `defaultAdvisors()` 는 빌더 상태를 변경한다.
같은 빌더 인스턴스에 두 번 호출하면 어드바이저가 누적되어 기존 `chatClient` 가 오염될 수 있다.
`clone()` 으로 파생하면 이 위험이 사라진다.

### @Primary 가 필요한 이유

`ChatClient` 후보가 둘이 되면서 기존 `ChatService` 의
`private final ChatClient chatClient` 타입 주입이 모호해진다.
파라미터 이름 기반 fallback 으로 우연히 동작할 수는 있으나 의도가 드러나지 않는다.
기존 빈에 `@Primary` 를 붙여 명시한다.

### RagChatService 는 생성자를 직접 선언

`@Qualifier("ragChatClient")` 가 필요하다. Lombok `@RequiredArgsConstructor` 는
`lombok.copyableAnnotations` 설정에 따라 애너테이션 복사 여부가 달라지므로,
생성자 하나를 직접 쓰는 편이 단순하고 확실하다.

```java
public RagChatService(@Qualifier("ragChatClient") ChatClient ragChatClient) {
    this.ragChatClient = ragChatClient;
}
```

### 어드바이저 순서는 기본값 유지

`MessageChatMemoryAdvisor`(order = `HIGHEST_PRECEDENCE + 1000`) 가
`RetrievalAugmentationAdvisor`(order = 0) 보다 먼저 실행된다.

이 순서 덕분에 대화 메모리에는 **발췌가 주입되기 전의 원본 질문**이 저장된다.
순서가 뒤바뀌면 `maxMessages(5)` 메모리 창이 문서 발췌로 가득 차 대화 맥락이 사라진다.

실측 확인:

```sql
SELECT type, left(content,90) FROM spring_ai_chat_memory WHERE conversation_id='f2';
-- USER | API 키는 어디에 보관하고 회전 주기는 며칠인가?   ← 원본 질문 그대로
```

### 패키지 분리

신규 코드는 `io.vibe.llm.rag` 에 둔다. 기존 `io.vibe.llm.chart` 는 `chat` 의 오타지만,
이름을 바꾸면 모든 기존 파일이 diff 에 섞여 RAG 변경이 묻힌다.
오타를 새 코드로 전파하지 않으면서 기존 코드는 건드리지 않는 선택이다.

## 후속 — 스트리밍 변형 추가

`/ui` 비교 화면을 스트리밍으로 바꾸면서 `/rag-chat-streams` 와 `/plain-chat-streams` 를
추가했다. 여기서도 같은 판단을 반복했다.

- 기존 엔드포인트에 `stream=true` 플래그를 붙이는 2번 방식은 다시 기각.
- 일반 패널에 기존 `/chat-streams` 를 재사용하는 것도 기각 — `user-message.st` 를 적용해
  질문이 "«X» 개념을 상세히 설명해 주세요." 로 바뀐다. RAG 패널은 원본 질문을 쓰므로
  두 패널의 user 메시지가 달라져 비교가 성립하지 않는다.

새 엔드포인트 둘은 각각 `/rag-chats`·`/chats` 와 프롬프트·어드바이저·메모리 키 전달
방식이 같고 `.call()` 이 `.stream()` 으로 바뀐 것뿐이다. 기존 세 엔드포인트는 무변경.

## 결과

- `/rag-chats` 는 문서 근거로 응답, `/chats` 는 동일 질문에 모른다고 응답 → RAG 효과 입증
- `/chats`, `/chat-streams` 회귀 없음 (변경 후 재검증 완료)
- `./gradlew test` 통과 — 기존 `@SpringBootTest` 가 신규 빈과 함께 정상 기동
