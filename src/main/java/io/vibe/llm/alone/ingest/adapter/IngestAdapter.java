package io.vibe.llm.alone.ingest.adapter;

import io.vibe.llm.basis.document.entity.DocumentChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @since       2026.10.01
 * @author      preah
 * @description ingest adapter
 **********************************************************************************************************************/
@Component
@RequiredArgsConstructor
public class IngestAdapter {

    /** DocumentChunk 목록을 Spring AI 의 {@code Document} 목록으로 바꾼다. */
    public List<org.springframework.ai.document.Document> ingest(List<DocumentChunk> chunks) {
        return chunks.stream()
                .map(chunk -> org.springframework.ai.document.Document.builder()
                        .id(chunk.toVectorId())
                        .text(chunk.getText())
                        .metadata("documentId", chunk.getDocument().getId())
                        .metadata("chunkIndex", chunk.getChunkIndex())
                        .build())
                .toList();
    }
}
