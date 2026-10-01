package io.vibe.llm.basis.document.service;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import io.vibe.llm.basis.document.entity.Document;
import io.vibe.llm.basis.document.entity.DocumentChunk;
import io.vibe.llm.basis.document.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document chunk service
 **********************************************************************************************************************/
@Service
@Transactional
@RequiredArgsConstructor
public class DocumentChunkService {

    private final DocumentChunkRepository documentChunkRepository;
    private final TokenTextSplitter tokenTextSplitter;

    private final Encoding tokenEncoding = Encodings.newLazyEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);

    /** document 의 마크다운을 {@link TokenTextSplitter} 로 쪼개 DocumentChunk 로 저장한다. */
    public List<DocumentChunk> split(Document document) {
        List<org.springframework.ai.document.Document> splits = tokenTextSplitter.split(
                org.springframework.ai.document.Document.builder().text(document.getMarkdown()).build());

        List<DocumentChunk> chunks = IntStream.range(0, splits.size())
                .mapToObj(index -> {
                    String text = splits.get(index).getText();
                    return DocumentChunk.builder()
                            .chunkIndex(index)
                            .text(text)
                            .chars(text.length())
                            .tokens(tokenEncoding.countTokensOrdinary(text))
                            .document(document)
                            .build();
                })
                .toList();

        return documentChunkRepository.saveAll(chunks);
    }
}
