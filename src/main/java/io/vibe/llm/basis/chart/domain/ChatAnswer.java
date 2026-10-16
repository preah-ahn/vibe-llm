package io.vibe.llm.basis.chart.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @since       2026.10.16
 * @author      preah
 * @description chat answer
 **********************************************************************************************************************/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ChatAnswer {

    private String content;
    private List<SourceRef> sources;
}
