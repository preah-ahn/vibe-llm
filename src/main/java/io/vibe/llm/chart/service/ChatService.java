package io.vibe.llm.chart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;

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
        String systemMessage = new SystemPromptTemplate(new ClassPathResource("prompt/system-message.st")).render();
        String userMessage = new PromptTemplate(new ClassPathResource("prompt/user-message.st")).render(Map.of("concept", query));

        return chatClient.prompt()
                .system(systemMessage)
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }
}
