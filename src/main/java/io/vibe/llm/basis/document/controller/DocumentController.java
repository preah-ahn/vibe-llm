package io.vibe.llm.basis.document.controller;

import io.vibe.llm.basis.document.form.DocumentForm.Request;
import io.vibe.llm.basis.document.form.DocumentForm.Response;
import io.vibe.llm.basis.document.service.DocumentService;
import io.vibe.llm.common.attach.entity.File;
import io.vibe.llm.common.attach.service.AttachService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

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
    private final AttachService   attachService;

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

    /** 원본 첨부 파일 다운로드. */
    @GetMapping("/documents/{documentId}/file")
    public ResponseEntity<Resource> download(@PathVariable Long documentId) {
        File file = documentService.get(documentId).getFile();
        Resource resource = attachService.loadAsResource(file);
        String encodedName = UriUtils.encode(file.getOriginalName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
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
