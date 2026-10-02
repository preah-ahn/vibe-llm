package io.vibe.llm.basis.document.repository;

import io.vibe.llm.basis.document.entity.Document;
import io.vibe.llm.basis.document.enumerate.DocumentStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document repository
 **********************************************************************************************************************/
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Document> findFirstByStatusTypeAndMarkdownIsNullOrderByIdAsc(DocumentStatusType statusType);

    Optional<Document> findFirstByStatusTypeAndMarkdownIsNotNullOrderByIdAsc(DocumentStatusType statusType);

    @EntityGraph(attributePaths = "chunks")
    Optional<Document> findFirstByStatusTypeOrderByIdAsc(DocumentStatusType statusType);
}
