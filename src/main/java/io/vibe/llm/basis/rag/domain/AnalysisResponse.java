package io.vibe.llm.basis.rag.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @since       2026.10.16
 * @author      preah
 * @description analysis response
 **********************************************************************************************************************/
/** 적재 전 미리보기. 파싱과 청킹까지만 수행한 결과이며 저장도 임베딩 호출도 하지 않는다. */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class AnalysisResponse {

    private String source;
    private long sizeBytes;
    private int textLength;
    private int chunkCount;
    private int totalTokens;
    private String embeddingModel;
    private int dimensions;
    private int existingChunks;
    private String existingIngestedAt;
    /** docling 이 변환한 문서 전문. 화면 렌더용이며 적재되지 않는다. */
    private String markdown;
    private List<ChunkPreview> chunks;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder(toBuilder = true)
    public static class ChunkPreview {

        private int index;
        private int chars;
        private int tokens;
        /** 청크 전문. 마크다운 렌더를 위해 자르지 않는다. */
        private String text;
    }
}
