package io.vibe.llm.alone.ingest.adapter;

import io.vibe.llm.basis.document.entity.DocumentChunk;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
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
    public List<org.springframework.ai.document.Document> ingest(io.vibe.llm.basis.document.entity.Document document) {
        return document.getChunks().stream()
                .map(chunk -> org.springframework.ai.document.Document.builder()
                        .id(chunk.toVectorId())
                        .text(chunk.getText())
                        .metadata("documentId", chunk.getDocument().getId())
                        .metadata("chunkIndex", chunk.getChunkIndex())
                        .metadata("source", document.getFile().getOriginalName())
                        .metadata("ingestedAt", document.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toString())
                        .build())
                .toList();
    }
}
