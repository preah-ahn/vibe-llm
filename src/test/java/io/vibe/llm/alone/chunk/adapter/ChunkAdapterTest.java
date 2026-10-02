package io.vibe.llm.alone.chunk.adapter;

import org.junit.jupiter.api.Test;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @since       2026.10.02
 * @author      preah
 * @description chunk adapter test
 **********************************************************************************************************************/
class ChunkAdapterTest {

    private final ChunkAdapter chunkAdapter = new ChunkAdapter(TokenTextSplitter.builder().withChunkSize(1000).build());

    @Test
    void toMarkdownChunk_헤더별로_청크를_나눈다() {
        String markdown = """
                # 제목A
                섹션 A 의 본문 내용입니다. 충분히 긴 문장으로 작성합니다.

                # 제목B
                섹션 B 의 본문 내용입니다. 역시 충분히 긴 문장으로 작성합니다.
                """;

        List<ChunkAdapter.Chunk> chunks = chunkAdapter.toMarkdownChunk(markdown);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0).index()).isZero();
        assertThat(chunks.get(1).index()).isEqualTo(1);
        assertThat(chunks.get(0).text()).contains("섹션 A");
        assertThat(chunks.get(1).text()).contains("섹션 B");
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

    @Test
    void toMarkdownChunk_한_섹션이_길면_토큰수_기준으로_추가_분할한다() {
        String longBody = "본문 ".repeat(2000);
        String markdown = "# 제목\n" + longBody;

        List<ChunkAdapter.Chunk> chunks = chunkAdapter.toMarkdownChunk(markdown);

        assertThat(chunks).hasSizeGreaterThan(1);
        chunks.forEach(chunk -> assertThat(chunk.tokens()).isLessThanOrEqualTo(1000));
    }
}
