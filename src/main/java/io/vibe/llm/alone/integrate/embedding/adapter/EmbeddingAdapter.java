package io.vibe.llm.alone.integrate.embedding.adapter;

import io.vibe.llm.basis.document.entity.DocumentChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @since       2026.10.01
 * @author      preah
 * @description embedding adapter
 **********************************************************************************************************************/
@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingAdapter {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    @Value("${spring.ai.openai.embedding.model:}")
    private String embeddingModelName;

    @Value("${spring.ai.vectorstore.pgvector.dimensions:0}")
    private int dimensions;

    /** 문서 목록을 임베딩해 VectorStore 에 저장한다. */
    public void embed(List<Document> documents) {
        vectorStore.add(documents);
    }

//    /** DocumentChunk 를 임베딩 모델로 돌려 결과를 로그로 남긴다. */
//    public void embed(DocumentChunk chunk) {
//        float[] embedding = embeddingModel.embed(Document.builder().text(chunk.getText()).build());
//        log.info("chunk embedded: {} model={} dimensions={} {}", chunk.getId(), embeddingModelName, dimensions, Arrays.toString(embedding));
//    }

    /** document 의 chunk 전체를 한 번에 임베딩 모델로 돌려 chunk 별 결과를 로그로 남긴다. */
    public List<float[]> embed(io.vibe.llm.basis.document.entity.Document document) {
        List<DocumentChunk> chunks = document.getChunks();
        List<String> texts = chunks.stream().map(DocumentChunk::getText).toList();
        return embeddingModel.embed(texts);
    }
}
