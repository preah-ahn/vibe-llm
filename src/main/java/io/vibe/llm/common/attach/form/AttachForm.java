package io.vibe.llm.common.attach.form;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @since       2026.10.01
 * @author      preah
 * @description attach form
 **********************************************************************************************************************/
public class AttachForm {

    public static class Request {

        @Getter
        @Setter
        @Builder(toBuilder = true)
        @ToString
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Remove {

            @NotBlank
            private String path;

            @NotBlank
            private String name;
        }
    }

    public static class Response {

        @Getter
        @Setter
        @Builder(toBuilder = true)
        @ToString
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FindOne {

            private String path;
            private String name;
            private Long size;
            private String originalName;
        }
    }
}
