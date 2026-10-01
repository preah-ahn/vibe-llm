package io.vibe.llm.basis.rag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @since       2026.10.01
 * @author      preah
 * @description rag chat service
 **********************************************************************************************************************/
@Service
public class RagChatService {

    private final ChatClient ragChatClient;

    // ChatClient 빈이 둘이므로 @Qualifier 가 필요하다. Lombok 생성자 대신 직접 선언한다.
    public RagChatService(@Qualifier("ragChatClient") ChatClient ragChatClient) {
        this.ragChatClient = ragChatClient;
    }

    public String ask(String query, String conversationId) {
        String systemMessage = new SystemPromptTemplate(new ClassPathResource("prompt/rag-system-message.st")).render();

        return ragChatClient.prompt()
                .system(systemMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(query)
                .call()
                .content();
    }

    /**
     * {@link #ask} 와 프롬프트·어드바이저가 완전히 같고 전달 방식만 스트리밍이다.
     * 검색과 증강은 첫 토큰이 나오기 전에 끝나므로, 비스트리밍보다 첫 응답이 늦을 수 있다.
     */
    public Flux<String> askStream(String query, String conversationId) {
        String systemMessage = new SystemPromptTemplate(new ClassPathResource("prompt/rag-system-message.st")).render();

        return ragChatClient.prompt()
                .system(systemMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(query)
                .stream()
                .content();
    }
}
