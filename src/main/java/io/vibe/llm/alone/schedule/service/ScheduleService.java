package io.vibe.llm.alone.schedule.service;

import io.vibe.llm.alone.chunk.adapter.ChunkAdapter;
import io.vibe.llm.alone.integrate.docling.adapter.DoclingAdapter;
import io.vibe.llm.basis.document.entity.DocumentChunk;
import io.vibe.llm.basis.document.enumerate.DocumentStatusType;
import io.vibe.llm.basis.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @since       2026.10.01
 * @author      preah
 * @description schedule service
 **********************************************************************************************************************/
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final DocumentRepository documentRepository;
    private final DoclingAdapter doclingAdapter;
    private final ChunkAdapter chunkAdapter;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void documentToMarkdown() {
        documentRepository.findFirstByStatusTypeAndMarkdownIsNullOrderByIdAsc(DocumentStatusType.WAITING).ifPresent(document -> {
            String markdown = doclingAdapter.toMarkdown(document.getFile());
            document.setMarkdown(markdown);
            document.setStatusType(DocumentStatusType.MARKDOWN_IN_COMPLETED);
            documentRepository.save(document);
            log.info("document markdown updated: {}", document.getId());
        });
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void markdownToChunk() {
        documentRepository.findFirstByStatusTypeAndMarkdownIsNotNullOrderByIdDesc(DocumentStatusType.MARKDOWN_IN_COMPLETED).ifPresent(document -> {
            List<ChunkAdapter.Chunk> chunks = chunkAdapter.toMarkdownChunk(document.getMarkdown());

            document.getChunks().addAll(chunks.stream()
                    .map(chunk -> DocumentChunk.builder()
                            .chunkIndex(chunk.index())
                            .text(chunk.text())
                            .chars(chunk.chars())
                            .tokens(chunk.tokens())
                            .document(document)
                            .build())
                    .toList());
            document.setStatusType(DocumentStatusType.CHUNKING_IN_COMPLETED);

            documentRepository.save(document);
            log.info("document markdown chunked: {} {}", document.getId(), chunks);
        });
    }
}
