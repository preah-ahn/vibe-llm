package io.vibe.llm.basis.rag.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

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
        String systemMessage = new SystemPromptTemplate(new ClassPathResource("config/prompt/rag-system-message.st")).render();

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
     * <p>
     * rag-augment.st 의 "근거: 파일명" 지시는 LLM 이 빠뜨릴 수 있으므로, RetrievalAugmentationAdvisor 가
     * 실제로 검색한 문서 목록을 기준으로 출처 줄을 스트림 끝에 강제로 덧붙인다.
     */
    public Flux<String> askStream(String query, String conversationId) {
        String systemMessage = new SystemPromptTemplate(new ClassPathResource("config/prompt/rag-system-message.st")).render();

        AtomicReference<List<Document>> retrievedDocuments = new AtomicReference<>(List.of());

        Flux<String> content = ragChatClient.prompt()
                .system(systemMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(query)
                .stream()
                .chatClientResponse()
                .doOnNext(response -> {
                    @SuppressWarnings("unchecked")
                    List<Document> documents = (List<Document>) response.context().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);
                    if (documents != null) {
                        retrievedDocuments.set(documents);
                    }
                })
                .mapNotNull(ChatClientResponse::chatResponse)
                .map(chatResponse -> Optional.ofNullable(chatResponse.getResult())
                        .map(Generation::getOutput)
                        .map(AbstractMessage::getText)
                        .orElse(""))
                .filter(StringUtils::hasLength);

        return content.concatWith(Mono.defer(() -> Mono.justOrEmpty(buildSourceFooter(retrievedDocuments.get()))));
    }

    private String buildSourceFooter(List<Document> documents) {
        Set<String> sources = new LinkedHashSet<>();
        for (Document document : documents) {
            Object source = document.getMetadata().get("source");
            if (source != null) {
                sources.add(source.toString());
            }
        }
        return sources.isEmpty() ? null : "\n\n출처: " + String.join(", ", sources);
    }
}
