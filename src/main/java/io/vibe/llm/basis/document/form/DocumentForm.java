package io.vibe.llm.basis.document.form;

import io.vibe.llm.basis.document.enumerate.DocumentStatusType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document form
 **********************************************************************************************************************/
public class DocumentForm {

    public static class Request {

        @Getter
        @Setter
        @Builder
        @ToString
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Find {

            private String name;
        }

        @Getter
        @Setter
        @Builder(toBuilder = true)
        @ToString
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Add {

            @NotBlank
            @Size(max = 50)
            private String name;

            @Size(max = 4000)
            private String description;

            @Size(max = 4000)
            private String url;

            @NotNull
            @Valid
            private File file;
        }

        @Getter
        @Setter
        @Builder(toBuilder = true)
        @ToString
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Modify {

            @NotBlank
            @Size(max = 50)
            private String name;

            @Size(max = 4000)
            private String description;

            @Size(max = 4000)
            private String url;

            @NotNull
            @Valid
            private File file;
        }

        @Getter
        @Setter
        @Builder(toBuilder = true)
        @ToString
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ChangeStatus {

            @NotNull
            private DocumentStatusType statusType;
        }

        @Getter
        @Setter
        @Builder(toBuilder = true)
        @ToString
        @NoArgsConstructor
        @AllArgsConstructor
        public static class File {

            @NotBlank
            @Size(max = 200)
            private String path;

            @NotBlank
            @Size(max = 50)
            private String name;

            @NotBlank
            @Size(max = 200)
            private String originalName;

            @NotNull
            private Long size;
        }
    }

    public static class Response {

        @Data
        public static class FindAll {

            private Long id;
            private String name;
            private DocumentStatusType statusType;
            private String description;
            private String url;
            private File file;
            private LocalDateTime createdAt;
        }

        @Data
        public static class FindOne {

            private Long id;
            private String name;
            private DocumentStatusType statusType;
            private String description;
            private String url;
            private File file;
            private LocalDateTime createdAt;
        }

        @Data
        public static class File {

            private String path;
            private String name;
            private String originalName;
            private Long size;
        }

        @Data
        public static class Analysis {

            private String markdown;
            private int chunkCount;
            private List<ChunkPreview> chunks;
        }

        @Data
        public static class ChunkPreview {

            private int index;
            private int chars;
            private int tokens;
            private String text;
        }
    }
}
