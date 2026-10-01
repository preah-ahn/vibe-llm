package io.vibe.llm.basis.rag.service;

import io.vibe.llm.alone.docling.adapter.DoclingAdapter;
import io.vibe.llm.basis.rag.controller.AnalysisResponse;
import io.vibe.llm.basis.rag.controller.DocumentSummary;
import io.vibe.llm.basis.rag.mapper.DocumentSummaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document ingest service
 **********************************************************************************************************************/
@Service
@RequiredArgsConstructor
public class DocumentIngestService {

    private final VectorStore vectorStore;
    private final DocumentSummaryMapper documentSummaryMapper;
    private final DoclingClient doclingClient;
    private final DoclingAdapter doclingAdapter;

    @Value("${spring.ai.openai.embedding.model:}")
    private String embeddingModel;

    @Value("${spring.ai.vectorstore.pgvector.dimensions:0}")
    private int dimensions;

    /**
     * 적재 전 미리보기. 저장하지 않으며 임베딩도 호출하지 않는다.
     * 청크 경계는 {@link #ingest} 와 동일한 경로로 계산하므로 실제 적재 결과와 일치한다.
     */
    public AnalysisResponse analyze(MultipartFile file) {
        String source = file.getOriginalFilename();

        // 청킹과 마크다운 변환은 docling 의 서로 다른 엔드포인트라 호출이 두 번이다.
        // 청크 응답에는 마크다운이 없고, 변환 응답에는 청크가 없다.
        List<DoclingClient.Chunk> chunks = doclingClient.chunk(file);
        String markdown = doclingAdapter.toMarkdown(file);

        List<AnalysisResponse.ChunkPreview> previews = new ArrayList<>(chunks.size());
        int textLength = 0;
        int totalTokens = 0;

        for (DoclingClient.Chunk chunk : chunks) {
            int tokens = chunk.numTokens() == null ? 0 : chunk.numTokens();
            textLength += chunk.text().length();
            totalTokens += tokens;
            previews.add(new AnalysisResponse.ChunkPreview(
                    chunk.index(), chunk.text().length(), tokens, chunk.text()));
        }

        DocumentSummary existing = documentSummaryMapper.findBySource(source);

        return new AnalysisResponse(
                source,
                file.getSize(),
                textLength,
                chunks.size(),
                totalTokens,
                embeddingModel,
                dimensions,
                existing.chunks(),
                existing.ingestedAt(),
                markdown,
                previews);
    }

    public int ingest(MultipartFile file) {
        String source = file.getOriginalFilename();

        // 같은 파일명을 다시 올리면 이전 청크를 지워 중복 누적을 막는다
        vectorStore.delete(bySource(source));

        List<Document> chunks = toDocuments(file, Instant.now().toString());
        if (chunks.isEmpty()) {
            // docling 이 추출할 텍스트를 찾지 못한 경우
            return 0;
        }

        vectorStore.add(chunks);
        return chunks.size();
    }

    public List<DocumentSummary> list() {
        return documentSummaryMapper.findAll();
    }

    public int deleteBySource(String source) {
        int chunks = documentSummaryMapper.findBySource(source).chunks();

        vectorStore.delete(bySource(source));
        return chunks;
    }

    /** docling 청크를 벡터 스토어에 넣을 Document 로 바꾼다. */
    private List<Document> toDocuments(MultipartFile file, String ingestedAt) {
        String source = file.getOriginalFilename();

        return doclingClient.chunk(file).stream()
                .map(chunk -> Document.builder()
                        .text(chunk.text())
                        .metadata("source", source)
                        .metadata("ingestedAt", ingestedAt)
                        .build())
                .toList();
    }

    private Filter.Expression bySource(String source) {
        return new FilterExpressionBuilder().eq("source", source).build();
    }
}
