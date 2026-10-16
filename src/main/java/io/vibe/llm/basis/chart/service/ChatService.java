package io.vibe.llm.basis.chart.service;

import io.vibe.llm.basis.chart.domain.SourceRef;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @since       2026.09.21
 * @author      preah
 * @description chat service
 **********************************************************************************************************************/
@Service
@RequiredArgsConstructor
public class ChatService {

    /**
     * 스트림 본문(답변 텍스트)과 출처 목록을 같은 Flux<String> 에 함께 실어 보내기 위한 구분자.
     * LLM 응답에 NUL 문자가 나올 일이 없어 경계로 안전하다.
     */
    private static final String SOURCES_MARKER = "\u0000SOURCES\u0000";

    /** 답변 생성 전 처리 단계(검색/생성)를 프론트에 알리기 위한 마커. 프론트 STAGE_MARKER_PATTERN 과 형식을 맞춰야 한다. */
    private static final String STAGE_MARKER_SEARCHING  = "\u0000STAGE:SEARCHING\u0000";
    private static final String STAGE_MARKER_GENERATING = "\u0000STAGE:GENERATING\u0000";

    /**
     * 검색된 문서가 임계값을 넘어도 질의와 무관할 수 있어(유사도로 구분 불가), 실제 근거 사용 여부는
     * LLM 답변 문구로 판단한다. rag-system-message.st/기본 augment 프롬프트의 "모르면 모른다고 답하라"
     * 지시를 따를 때 LLM 이 쓰는 전형적인 거절 표현 목록.
     */
    private static final List<String> NO_ANSWER_MARKERS = List.of(
            "정보가 없습니다", "정보를 찾을 수 없습니다", "모르겠습니다", "모릅니다", "알 수 없습니다",
            "답변할 수 없습니다", "답변드릴 수 없습니다", "확인할 수 없습니다", "확인되지 않습니다"
    );

    private final ChatClient   chatClient;
    private final ObjectMapper objectMapper;

    /**
     * rag-augment.st 의 "출처: 파일명" 지시는 LLM 이 빠뜨릴 수 있으므로, RetrievalAugmentationAdvisor 가
     * 실제로 검색한 문서 목록을 기준으로 출처 목록을 스트림 끝에 {@link #SOURCES_MARKER} 로 구분해 강제로 덧붙인다.
     */
    public Flux<String> getChatStream(String query, String conversationId) {
        AtomicReference<List<Document>> retrievedDocuments = new AtomicReference<>(List.of());
        StringBuilder fullAnswer = new StringBuilder();

        Flux<String> content = chatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(query)
                .stream()
                .chatClientResponse()
                .doOnNext(response -> {
                    List<Document> documents = extractDocuments(response);
                    if (!documents.isEmpty()) {
                        retrievedDocuments.set(documents);
                    }
                })
                // OpenAI 호환 스트리밍은 첫 청크가 role 만 있고 content 가 빈 문자열인 경우가 많다. 그 청크가
                // filter(hasLength) 로 걸러지기 전, 모델이 응답을 시작한 이 시점에 전환 마커를 끼워 넣어야
                // 질의 변환/검색 단계와 실제로 구분되는 틈이 생긴다. filter 뒤에 걸면 마커와 첫 토큰이 동시에 떨어진다.
                .switchOnFirst((signal, responses) -> {
                    Flux<String> mapped = responses
                            .mapNotNull(ChatClientResponse::chatResponse)
                            .map(chatResponse -> Optional.ofNullable(chatResponse.getResult())
                                    .map(Generation::getOutput)
                                    .map(AbstractMessage::getText)
                                    .orElse(""))
                            .filter(StringUtils::hasLength)
                            .doOnNext(fullAnswer::append);
                    return signal.hasValue() ? mapped.startWith(STAGE_MARKER_GENERATING) : mapped;
                });

        return Flux.concat(Mono.just(STAGE_MARKER_SEARCHING), content)
                .concatWith(Mono.defer(() -> {
                    List<SourceRef> sources = isNoAnswer(fullAnswer.toString()) ? List.of() : toSourceRefs(retrievedDocuments.get());
                    return sources.isEmpty() ? Mono.empty() : Mono.just(SOURCES_MARKER + objectMapper.writeValueAsString(sources));
                }));
    }

    private boolean isNoAnswer(String content) {
        return NO_ANSWER_MARKERS.stream().anyMatch(content::contains);
    }

    @SuppressWarnings("unchecked")
    private List<Document> extractDocuments(ChatClientResponse response) {
        Object documents = response.context().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);
        return documents != null ? (List<Document>) documents : List.of();
    }

    /**
     * source(파일명) 는 중복 업로드 시 고유하지 않으므로, 메타데이터의 documentId 를 기준 키로 쓴다.
     * 같은 파일에서 여러 청크가 검색되면 하나의 출처로 합치고, 헤딩 경로는 목록으로 모은다.
     */
    private List<SourceRef> toSourceRefs(List<Document> documents) {
        Map<Long, String>       fileNameByDocumentId   = new LinkedHashMap<>();
        Map<Long, List<String>> headingPathsByDocument = new LinkedHashMap<>();
        for (Document document : documents) {
            Object documentId = document.getMetadata().get("documentId");
            Object source = document.getMetadata().get("source");
            if (documentId instanceof Number number && source != null) {
                Long id = number.longValue();
                fileNameByDocumentId.putIfAbsent(id, source.toString());

                String headingPath = String.valueOf(document.getMetadata().getOrDefault("headingPath", ""));
                if (StringUtils.hasText(headingPath)) {
                    List<String> headingPaths = headingPathsByDocument.computeIfAbsent(id, key -> new ArrayList<>());
                    if (!headingPaths.contains(headingPath)) {
                        headingPaths.add(headingPath);
                    }
                }
            }
        }
        return fileNameByDocumentId.entrySet().stream()
                .map(entry -> new SourceRef(entry.getKey(), entry.getValue(),
                        headingPathsByDocument.getOrDefault(entry.getKey(), List.of())))
                .toList();
    }
}
