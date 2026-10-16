package io.vibe.llm.basis.rag.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @since       2026.10.16
 * @author      preah
 * @description document summary
 **********************************************************************************************************************/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DocumentSummary {

    private String source;
    private int chunks;
    private String ingestedAt;
}
