package io.vibe.llm.basis.chart.controller;

import java.util.List;

/**
 * @since       2026.10.15
 * @author      preah
 * @description chat answer
 **********************************************************************************************************************/
public record ChatAnswer(String content, List<SourceRef> sources) {
}
