package io.vibe.llm.basis.document.controller;

import io.vibe.llm.basis.document.form.DocumentForm.Request;
import io.vibe.llm.basis.document.form.DocumentForm.Response;
import io.vibe.llm.basis.document.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static io.vibe.llm.basis.document.mapper.DocumentMapper.mapper;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document controller
 **********************************************************************************************************************/
@RestController
@RequiredArgsConstructor
@RequestMapping("${property.api.end-point}")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/documents/pages")
    public Page<Response.FindAll> getPage(@Valid Request.Find find, Pageable pageable) {
        return documentService.getPage(find, pageable).map(mapper::toFindAll);
    }

    @GetMapping("/documents/{documentId}")
    public Response.FindOne get(@PathVariable Long documentId) {
        return mapper.toFindOne(documentService.get(documentId));
    }

    /** 저장된 마크다운/청크 조회. 여기서 변환하지 않는다. */
    @GetMapping("/documents/{documentId}/analysis")
    public Response.Analysis analyze(@PathVariable Long documentId) {
        return documentService.analyze(documentId);
    }

    @PostMapping(value = "/documents", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response.FindOne add(@Valid @RequestBody Request.Add add) {
        return mapper.toFindOne(documentService.add(mapper.toDocument(add)));
    }

    @PutMapping("/documents/{documentId}")
    public Response.FindOne modify(@PathVariable Long documentId, @Valid @RequestBody Request.Modify modify) {
        return mapper.toFindOne(documentService.modify(mapper.toDocument(documentId, modify)));
    }

    @PatchMapping("/documents/{documentId}/status")
    public Response.FindOne changeStatus(@PathVariable Long documentId, @Valid @RequestBody Request.ChangeStatus changeStatus) {
        return mapper.toFindOne(documentService.changeStatus(mapper.toDocument(documentId, changeStatus)));
    }

    @DeleteMapping("/documents/{documentId}")
    public void remove(@PathVariable Long documentId) {
        documentService.remove(documentId);
    }
}
