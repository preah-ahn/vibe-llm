package io.vibe.llm.rag.controller;

import java.util.List;

/**
 * 적재 전 미리보기. 파싱과 청킹까지만 수행한 결과이며 저장도 임베딩 호출도 하지 않는다.
 *
 * @param markdown       docling 이 변환한 문서 전문. 화면 렌더용이며 적재되지 않는다
 * @param existingChunks 이미 적재된 같은 파일명의 청크 수. 0 이면 신규
 */
public record AnalysisResponse(
        String source,
        long sizeBytes,
        int textLength,
        int chunkCount,
        int totalTokens,
        String embeddingModel,
        int dimensions,
        int existingChunks,
        String existingIngestedAt,
        String markdown,
        List<ChunkPreview> chunks) {

    /** @param text 청크 전문. 마크다운 렌더를 위해 자르지 않는다 */
    public record ChunkPreview(int index, int chars, int tokens, String text) {
    }
}
