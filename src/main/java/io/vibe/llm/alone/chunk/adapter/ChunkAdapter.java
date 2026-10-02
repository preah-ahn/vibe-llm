package io.vibe.llm.alone.chunk.adapter;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @since       2026.10.01
 * @author      preah
 * @description chunk adapter
 **********************************************************************************************************************/
@Component
@RequiredArgsConstructor
public class ChunkAdapter {

    private final TokenTextSplitter tokenTextSplitter;

    private final Encoding tokenEncoding = Encodings.newLazyEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);

    /**
     * 마크다운 텍스트를 Spring AI 의 {@link MarkdownDocumentReader} 로 섹션 단위로 나눈 뒤,
     * 섹션 하나가 과도하게 길어 임베딩 모델의 최대 입력 토큰을 넘지 않도록 {@link TokenTextSplitter} 로 한 번 더 쪼갠다.
     */
    public List<Chunk> toMarkdownChunk(String markdown) {
        Resource resource = new ByteArrayResource(markdown.getBytes(StandardCharsets.UTF_8));
        MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, MarkdownDocumentReaderConfig.defaultConfig());
        List<org.springframework.ai.document.Document> sections = reader.get();

        List<org.springframework.ai.document.Document> documents = tokenTextSplitter.split(sections);

        return IntStream.range(0, documents.size())
                .mapToObj(index -> {
                    String text = documents.get(index).getText();
                    return new Chunk(index, text, text.length(), tokenEncoding.countTokensOrdinary(text));
                })
                .toList();
    }

    public record Chunk(int index, String text, int chars, int tokens) {
    }
}
