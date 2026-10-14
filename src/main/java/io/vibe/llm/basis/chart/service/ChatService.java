package io.vibe.llm.basis.chart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @since       2026.09.21
 * @author      preah
 * @description chat service
 **********************************************************************************************************************/
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient  chatClient;
    private final VectorStore vectorStore;

    public String getChatResponse(String query, String userId) {
        return chatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, userId))
                .user(query)
                .call()
                .content();
    }


    /**
     * {@link #getChatResponse} 와 프롬프트가 완전히 같고 전달 방식만 스트리밍이다.
     * {@link #getStreamChat} 과 달리 user-message.st 템플릿을 적용하지 않는다 —
     * RAG 비교 화면은 양쪽 패널이 같은 질문 문장을 받아야 비교가 성립한다.
     */
    public Flux<String> getPlainStreamChat(String query, String userId) {
        return chatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, userId))
                .user(query)
                .stream()
                .content();
    }

    public Flux<String> getStreamChat(String query, String conversationId) {

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                            .topK(5)
                            .similarityThreshold(0.3)
                            .query(query)
                            .build()
        );

        return chatClient.prompt()
                .system  (system -> system.param("documents", documents.stream().map(Document::getText).collect(Collectors.joining("\n"))))
                .user    (query)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }
}
