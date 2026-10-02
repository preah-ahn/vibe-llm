package io.vibe.llm.alone.integrate.embedding.adapter;

import io.vibe.llm.basis.document.entity.Document;
import io.vibe.llm.basis.document.entity.DocumentChunk;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since       2026.10.02
 * @author      preah
 * @description embedding adapter test
 **********************************************************************************************************************/
class EmbeddingAdapterTest {

    private final VectorStore vectorStore = mock(VectorStore.class);
    private final EmbeddingModel embeddingModel = mock(EmbeddingModel.class);
    private final EmbeddingAdapter embeddingAdapter = new EmbeddingAdapter(vectorStore, embeddingModel);

    @Test
    void embed_document의_chunk_텍스트_전체를_한번에_임베딩모델로_돌린다() {
        DocumentChunk chunk1 = DocumentChunk.builder().id(1L).text("첫번째 청크").build();
        DocumentChunk chunk2 = DocumentChunk.builder().id(2L).text("두번째 청크").build();
        Document document = Document.builder().chunks(List.of(chunk1, chunk2)).build();

        when(embeddingModel.embed(List.of("첫번째 청크", "두번째 청크")))
                .thenReturn(List.of(new float[]{0.1f, 0.2f}, new float[]{0.3f, 0.4f}));

        embeddingAdapter.embed(document);

        verify(embeddingModel).embed(List.of("첫번째 청크", "두번째 청크"));
    }

    @Test
    void embed_chunk이_없으면_빈_텍스트_목록으로_호출한다() {
        Document document = Document.builder().chunks(List.of()).build();

        when(embeddingModel.embed(List.<String>of())).thenReturn(List.of());

        embeddingAdapter.embed(document);

        verify(embeddingModel).embed(List.of());
    }
}
