package io.vibe.llm.basis.rag.controller;

/**
 * @since       2026.10.01
 * @author      preah
 * @description ingest response
 **********************************************************************************************************************/
public record IngestResponse(String source, int chunks) {
}
