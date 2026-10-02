package io.vibe.llm.basis.document.service;

import io.vibe.llm.basis.document.entity.Document;
import io.vibe.llm.basis.document.entity.DocumentChunk;
import io.vibe.llm.basis.document.enumerate.DocumentStatusType;
import io.vibe.llm.basis.document.form.DocumentForm.Request;
import io.vibe.llm.basis.document.form.DocumentForm.Response;
import io.vibe.llm.basis.document.repository.DocumentRepository;
import io.vibe.llm.common.attach.entity.File;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document service
 **********************************************************************************************************************/
@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final VectorStore vectorStore;

    @Transactional(readOnly = true)
    public Page<Document> getPage(Request.Find find, Pageable pageable) {
        if (StringUtils.hasText(find.getName())) {
            return documentRepository.findByNameContainingIgnoreCase(find.getName(), pageable);
        }
        return documentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Document get(Long id) {
        return documentRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    /**
     * 등록 직후 상태는 항상 대기다. 파이프라인 진행은 {@link #changeStatus} 로만 바꾼다.
     * file 은 Document.file 의 cascade(PERSIST) 로 같이 insert 된다 — File 이 FK 소유 쪽이라 역방향 참조를 직접 잡아줘야 한다.
     */
    public Document add(Document document) {
        document.setStatusType(DocumentStatusType.WAITING);
        document.getFile().setDocument(document);
        return documentRepository.save(document);
    }

    /**
     * JPA 변경 감지로 반영한다. 넘어온 document 는 id/수정 필드만 채워져 있어 그대로 save 하면 createdAt 이 날아간다.
     * file 도 새 row 로 바꾸지 않고 기존 row 필드만 덮어쓴다 — 예전 물리 파일 삭제는 여기서 안 한다.
     */
    public Document modify(Document document) {
        Document persisted = get(document.getId());
        persisted.setName(document.getName());
        persisted.setDescription(document.getDescription());
        persisted.setUrl(document.getUrl());

        File file = persisted.getFile();
        File update = document.getFile();
        file.setPath(update.getPath());
        file.setName(update.getName());
        file.setOriginalName(update.getOriginalName());
        file.setSize(update.getSize());

        return persisted;
    }

    public Document changeStatus(Document document) {
        Document persisted = get(document.getId());
        persisted.setStatusType(document.getStatusType());
        return persisted;
    }

    public void remove(Long id) {
        Document document = get(id);
        List<String> vectorIds = document.getChunks().stream()
                .map(DocumentChunk::toVectorId)
                .toList();

        documentRepository.deleteById(id);
        if (!vectorIds.isEmpty()) {
            vectorStore.delete(vectorIds);
        }
    }

    /** 변환은 하지 않는다 — 파이프라인이 미리 채워 둔 마크다운/청크를 테이블에서 그대로 조회한다. */
    @Transactional(readOnly = true)
    public Response.Analysis analyze(Long id) {
        Document document = get(id);

        List<Response.ChunkPreview> previews = document.getChunks().stream()
                .sorted(Comparator.comparingInt(DocumentChunk::getChunkIndex))
                .map(chunk -> {
                    Response.ChunkPreview preview = new Response.ChunkPreview();
                    preview.setIndex(chunk.getChunkIndex());
                    preview.setChars(chunk.getChars());
                    preview.setTokens(chunk.getTokens());
                    preview.setText(chunk.getText());
                    return preview;
                })
                .toList();

        Response.Analysis analysis = new Response.Analysis();
        analysis.setMarkdown(document.getMarkdown());
        analysis.setChunkCount(previews.size());
        analysis.setChunks(previews);
        return analysis;
    }
}
