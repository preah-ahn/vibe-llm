# 설계 결정 기록 (ADR)

| # | 결정 | 상태 |
|---|---|---|
| [0001](0001-retrieval-augmentation-advisor.md) | RAG 어드바이저로 `RetrievalAugmentationAdvisor` 채택 | 채택 |
| [0002](0002-similarity-threshold.md) | `similarityThreshold` 를 0.2 로 낮추고 관련성 판단을 LLM 에 위임 | 채택 |
| [0003](0003-separate-rag-chat-client.md) | RAG 를 별도 `ChatClient` 빈 + 별도 엔드포인트로 분리 | 채택 |
| [0004](0004-docling-parsing-chunking.md) | 문서 파싱과 청킹을 docling-serve 에 위임 | 채택 |
