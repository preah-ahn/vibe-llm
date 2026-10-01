package io.vibe.llm.basis.rag.controller;

/**
 * @since       2026.09.30
 * @author      preah
 * @description document summary
 **********************************************************************************************************************/
public record DocumentSummary(String source, int chunks, String ingestedAt) {
}
