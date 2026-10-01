package io.vibe.llm.basis.rag.controller;

import io.vibe.llm.basis.rag.service.DocumentIngestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document controller
 **********************************************************************************************************************/
@RestController
@RequiredArgsConstructor
public class DocumentFileController {

    private final DocumentIngestService documentIngestService;

    /** 적재 전 미리보기. 저장하지 않는다. */
    @PostMapping(value = "/documents/analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisResponse> analyze(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(documentIngestService.analyze(file));
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IngestResponse> upload(@RequestPart("file") MultipartFile file) {
        int chunks = documentIngestService.ingest(file);
        return ResponseEntity.ok(new IngestResponse(file.getOriginalFilename(), chunks));
    }

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentSummary>> list() {
        return ResponseEntity.ok(documentIngestService.list());
    }

    @DeleteMapping("/documents")
    public ResponseEntity<DeleteResponse> delete(@RequestParam("source") String source) {
        int deleted = documentIngestService.deleteBySource(source);
        return ResponseEntity.ok(new DeleteResponse(source, deleted));
    }
}
