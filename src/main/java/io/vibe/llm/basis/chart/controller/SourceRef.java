package io.vibe.llm.basis.chart.controller;

import java.util.List;

/**
 * @since       2026.10.15
 * @author      preah
 * @description source ref
 **********************************************************************************************************************/
public record SourceRef(Long documentId, String fileName, List<String> headingPaths) {
}
