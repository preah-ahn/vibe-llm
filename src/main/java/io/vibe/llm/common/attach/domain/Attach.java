package io.vibe.llm.common.attach.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @since       2026.10.01
 * @author      preah
 * @description attach
 **********************************************************************************************************************/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class Attach {

    private String path;
    private String name;
    private String originalName;
    private Long size;
}
