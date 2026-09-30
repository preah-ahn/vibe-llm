package io.vibe.llm.rag.controller;

public record DocumentSummary(String source, int chunks, String ingestedAt) {
}
