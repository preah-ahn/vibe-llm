package io.vibe.llm.basis.document.repository;

import io.vibe.llm.basis.document.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document chunk repository
 **********************************************************************************************************************/
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
}
