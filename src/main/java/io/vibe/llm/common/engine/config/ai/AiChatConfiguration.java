package io.vibe.llm.common.engine.config.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.stream.Collectors;

/**
 * @since       2026.09.22
 * @author      preah
 * @description ai chat configuration
 **********************************************************************************************************************/
@Configuration
public class AiChatConfiguration {
    @Bean
    @Primary
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        return builder.defaultAdvisors(a -> a
                        .advisors(messageChatMemoryAdvisor, new SimpleLoggerAdvisor())
                        .param(ChatMemory.CONVERSATION_ID, "default"))
                .build();
    }

    /**
     * RAG 전용 ChatClient. 기존 chatClient 빈을 오염시키지 않도록 builder.clone() 으로 파생한다.
     */
    @Bean
    public ChatClient ragChatClient(ChatClient.Builder builder, ChatMemory chatMemory, VectorStore vectorStore) {
        // similarityThreshold 는 낮게 둔다. Qwen3-Embedding 실측 결과 문서에 실제로 있는 질문이
        // 0.27~0.55, 문서와 무관한 질문이 0.31 로 나와 임계값으로는 둘을 가를 수 없었다.
        // 대신 topK 로 후보만 좁히고, 근거 여부 판단은 rag-augment.st 규칙으로 LLM 에 맡긴다.
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(4)
                .similarityThreshold(0.2)
                .build();

        // 기본 프롬프트는 영어이고 "모르면 모른다고 하라"가 user 메시지에 들어가 system 프롬프트를 덮는다.
        // 기본 documentFormatter 는 본문만 넘기므로 출처를 알 수 없다. 파일명을 함께 붙인다.
        ContextualQueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder()
                .promptTemplate(new PromptTemplate(new ClassPathResource("prompt/rag-augment.st")))
                .allowEmptyContext(true)
                .documentFormatter(documents -> documents.stream()
                        .map(document -> "[출처: " + document.getMetadata().get("source") + "]\n" + document.getText())
                        .collect(Collectors.joining("\n\n")))
                .build();

        Advisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(queryAugmenter)
                .build();

        return builder.clone()
                .defaultAdvisors(a -> a
                        .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), ragAdvisor, new SimpleLoggerAdvisor())
                        .param(ChatMemory.CONVERSATION_ID, "default"))
                .build();
    }

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder().chatMemoryRepository(repository).maxMessages(5).build();
    }
//    public ChatClient chatClient(ChatClient.Builder builder) {
//        return builder.defaultAdvisors(new TokenPrintAdvisor(), new SimpleLoggerAdvisor(), new SafeGuardAdvisor(List.of("games"))).build();
//    }
}
