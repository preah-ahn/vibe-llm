package io.vibe.llm.common.engine.config.ai;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @since       2026.09.22
 * @author      preah
 * @description ai chat configuration
 **********************************************************************************************************************/
@Slf4j
@Configuration
public class AiChatConfiguration {
    @Bean
    @Primary
    public ChatClient chatClient(ChatClient.Builder builder, VectorStore vectorStore, ChatMemory chatMemory) {

        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(4)
                .similarityThreshold(0.2)
                .build();

//        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
//                .documentRetriever(documentRetriever)
//                .build();

        ContextualQueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder()
//                .promptTemplate(new PromptTemplate(new ClassPathResource("config/prompt/rag-augment.st")))
                .allowEmptyContext(true)
//                .documentFormatter(documents -> documents.stream()
//                        .map(document -> "[출처: " + document.getMetadata().get("source") + "]\n" + document.getText())
//                        .collect(Collectors.joining("\n\n")))
                .build();

        Advisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(
                        loggingTransformer(RewriteQueryTransformer.builder()
                                .chatClientBuilder(builder.clone())
                                .build())
                        , loggingTransformer(TranslationQueryTransformer.builder()
                                .chatClientBuilder(builder.clone())
                                .targetLanguage("english")
                                .build()))
                .queryExpander(loggingExpander(MultiQueryExpander.builder()
                        .chatClientBuilder(builder.clone())
                        .build()))
                .documentRetriever(documentRetriever)
                .queryAugmenter(queryAugmenter)
                .build();

        return builder
//                .defaultSystem(new ClassPathResource("config/prompt/rag-system-message.st"))
                .defaultAdvisors(a -> a
                        .advisors(ragAdvisor, MessageChatMemoryAdvisor.builder(chatMemory).build(), new SimpleLoggerAdvisor())
                )
                .build();
    }

//    @Bean
//    @Primary
//    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
//        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
//
//        return builder
//                .defaultSystem(new ClassPathResource("config/prompt/rag-system-message.st"))
//                .defaultAdvisors(a -> a
//                        .advisors(messageChatMemoryAdvisor, new SimpleLoggerAdvisor())
//                        .param(ChatMemory.CONVERSATION_ID, "default")
//                )
//                .build();
//    }

    /**
     * RAG 전용 ChatClient. 기존 chatClient 빈을 오염시키지 않도록 builder.clone() 으로 파생한다.
     */
//    @Bean
//    public ChatClient ragChatClient(ChatClient.Builder builder, ChatMemory chatMemory, VectorStore vectorStore) {
//        // similarityThreshold 는 낮게 둔다. Qwen3-Embedding 실측 결과 문서에 실제로 있는 질문이
//        // 0.27~0.55, 문서와 무관한 질문이 0.31 로 나와 임계값으로는 둘을 가를 수 없었다.
//        // 대신 topK 로 후보만 좁히고, 근거 여부 판단은 rag-augment.st 규칙으로 LLM 에 맡긴다.
//        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
//                .vectorStore(vectorStore)
//                .topK(4)
//                .similarityThreshold(0.2)
//                .build();
//
//        // 기본 프롬프트는 영어이고 "모르면 모른다고 하라"가 user 메시지에 들어가 system 프롬프트를 덮는다.
//        // 기본 documentFormatter 는 본문만 넘기므로 출처를 알 수 없다. 파일명을 함께 붙인다.
//        ContextualQueryAugmenter queryAugmenter = ContextualQueryAugmenter.builder()
//                .promptTemplate(new PromptTemplate(new ClassPathResource("config/prompt/rag-augment.st")))
//                .allowEmptyContext(true)
//                .documentFormatter(documents -> documents.stream()
//                        .map(document -> "[출처: " + document.getMetadata().get("source") + "]\n" + document.getText())
//                        .collect(Collectors.joining("\n\n")))
//                .build();
//
//        Advisor ragAdvisor = RetrievalAugmentationAdvisor.builder()rngu
//                .documentRetriever(documentRetriever)
//                .queryAugmenter(queryAugmenter)
//                .build();
//
//        return builder.clone()
//                .defaultAdvisors(a -> a
//                        .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), ragAdvisor, new SimpleLoggerAdvisor())
//                        .param(ChatMemory.CONVERSATION_ID, "default"))
//                .build();
//    }

    /** QueryTransformer/QueryExpander 구현체는 변환 결과를 로그로 남기지 않아, 입출력을 직접 감싸서 찍는다. */
    private QueryTransformer loggingTransformer(QueryTransformer delegate) {
        return query -> {
            Query transformed = delegate.transform(query);
            log.debug("query transform: '{}' -> '{}'", query.text(), transformed.text());
            return transformed;
        };
    }

    private QueryExpander loggingExpander(QueryExpander delegate) {
        return query -> {
            List<Query> expanded = delegate.expand(query);
            log.debug("query expand: '{}' -> {}", query.text(), expanded.stream().map(Query::text).toList());
            return expanded;
        };
    }

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder().chatMemoryRepository(repository).maxMessages(5).build();
    }

    /**
     * docling 은 마크다운 변환까지만 맡고, 청킹은 Spring AI 가 담당한다.
     * chunkSize 는 기존 docling chunking_max_tokens 값과 동일하게 맞춘다.
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(800)              // 청크 1개 목표 토큰 수
                .withMinChunkSizeChars(200)       // 청크 최소 글자 수, 미달 시 다음 청크와 병합
                .withMinChunkLengthToEmbed(10)    // 이 글자 수 미만 청크는 임베딩 대상에서 제외
                .withMaxNumChunks(5000)            // 문서 1개당 생성 가능한 청크 최대 개수
                .withKeepSeparator(true)          // 분리 기준 문자(개행 등)를 청크 텍스트에 유지
                .build();
    }

    /** 청크 글자수/토큰수 계산에 쓰는 인코딩. {@link TokenTextSplitter} 가 청킹에 쓰는 것과 같은 CL100K_BASE 다. */
    @Bean
    public Encoding tokenEncoding() {
        return Encodings.newLazyEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);
    }
//    public ChatClient chatClient(ChatClient.Builder builder) {
//        return builder.defaultAdvisors(new TokenPrintAdvisor(), new SimpleLoggerAdvisor(), new SafeGuardAdvisor(List.of("games"))).build();
//    }
}
