package io.vibe.llm.alone.integrate.embedding.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @since       2026.10.01
 * @author      preah
 * @description embedding adapter
 **********************************************************************************************************************/
@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingAdapter {

    private final VectorStore vectorStore;

    /** 문서 목록을 임베딩해 VectorStore 에 저장한다. */
    public void embed(List<Document> documents) {
        vectorStore.add(documents);
    }
}
