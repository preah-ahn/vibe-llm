package io.vibe.llm.alone.chunk.adapter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @since       2026.10.02
 * @author      preah
 * @description chunk adapter test
 **********************************************************************************************************************/
class ChunkAdapterTest {

    private final ChunkAdapter chunkAdapter = new ChunkAdapter();

    @Test
    void toMarkdownChunk_헤더별로_청크를_나눈다() {
        String markdown = """
                # 제목A
                본문A

                # 제목B
                본문B
                """;

        List<ChunkAdapter.Chunk> chunks = chunkAdapter.toMarkdownChunk(markdown);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0).index()).isZero();
        assertThat(chunks.get(1).index()).isEqualTo(1);
        assertThat(chunks.get(0).text()).contains("본문A");
        assertThat(chunks.get(1).text()).contains("본문B");
    }

    @Test
    void toMarkdownChunk_청크의_글자수와_토큰수를_채운다() {
        String markdown = "# 제목\n본문 내용입니다.";

        List<ChunkAdapter.Chunk> chunks = chunkAdapter.toMarkdownChunk(markdown);

        assertThat(chunks).isNotEmpty();
        chunks.forEach(chunk -> {
            assertThat(chunk.chars()).isEqualTo(chunk.text().length());
            assertThat(chunk.tokens()).isPositive();
        });
    }

    @Test
    void toMarkdownChunk_빈_문자열이면_빈_리스트를_반환한다() {
        List<ChunkAdapter.Chunk> chunks = chunkAdapter.toMarkdownChunk("");

        assertThat(chunks).isEmpty();
    }
}
