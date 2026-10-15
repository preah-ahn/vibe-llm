package io.vibe.llm.alone.chunk.adapter;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
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

    private final Encoding tokenEncoding = Encodings.newLazyEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);

    private final ChunkAdapter chunkAdapter = new ChunkAdapter(TokenTextSplitter.builder().withChunkSize(1000).build(), tokenEncoding);

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
        chunks.forEach(chunk -> {
            // 청크 텍스트 맨 앞에 붙는 "[헤딩 경로]\n" prefix 는 분할 이후에 덧붙으므로, splitter 가 만든
            // 본문 자체만 chunkSize 한도를 지키면 된다. prefix 만큼은 한도 밖에서 빼고 비교한다.
            int headingPrefixTokens = chunk.headingPath().isEmpty()
                    ? 0
                    : tokenEncoding.countTokensOrdinary("[" + chunk.headingPath() + "]\n");
            assertThat(chunk.tokens() - headingPrefixTokens).isLessThanOrEqualTo(1000);
        });
    }

    @Test
    void toMarkdownChunk_표는_행_중간에서_쪼개지_않고_하나의_청크로_유지한다() {
        String markdown = "# 표 제목\n"
                + "| 이름 | 나이 |\n"
                + "| --- | --- |\n"
                + "| 철수 | 20 |\n"
                + "| 영희 | 21 |\n"
                + "\n"
                + "본문도 있습니다.\n";

        List<ChunkAdapter.Chunk> chunks = chunkAdapter.toMarkdownChunk(markdown);

        assertThat(chunks).anySatisfy(chunk -> {
            assertThat(chunk.text()).contains("| 철수 | 20 |").contains("| 영희 | 21 |");
            // 표 바로 앞 헤딩은 본문 없이 끊겨도(reader 가 통째로 버리는 경우) 헤딩 경로에 반영돼야 한다.
            assertThat(chunk.headingPath()).isEqualTo("표 제목");
        });
    }

    @Test
    void toMarkdownChunk_수평선에서도_섹션을_나눈다() {
        String markdown = "헤딩_없는_첫_블록\n\n---\n\n헤딩_없는_둘째_블록\n";

        List<ChunkAdapter.Chunk> chunks = chunkAdapter.toMarkdownChunk(markdown);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0).text()).contains("첫_블록");
        assertThat(chunks.get(1).text()).contains("둘째_블록");
    }
}
