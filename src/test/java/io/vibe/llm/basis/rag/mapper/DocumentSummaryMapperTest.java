package io.vibe.llm.basis.rag.mapper;

import io.vibe.llm.basis.rag.controller.DocumentSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document summary mapper test
 **********************************************************************************************************************/
/**
 * ${tableName} 치환과 DocumentSummary record 생성자 매핑은 런타임에만 드러나므로
 * 실제 Postgres 에 쿼리를 날려 확인한다. vector_store 는 Spring AI 가 생성한다.
 */
@SpringBootTest
@Transactional
class DocumentSummaryMapperTest {

    @Autowired
    private DocumentSummaryMapper documentSummaryMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findAll_적재된_파일을_청크수와_함께_집계한다() {
        insertChunk("매퍼테스트.pdf", "2026-10-01T00:00:00Z");
        insertChunk("매퍼테스트.pdf", "2026-10-01T00:00:00Z");

        List<DocumentSummary> summaries = documentSummaryMapper.findAll();

        assertThat(summaries)
                .filteredOn(summary -> "매퍼테스트.pdf".equals(summary.source()))
                .singleElement()
                .satisfies(summary -> {
                    assertThat(summary.chunks()).isEqualTo(2);
                    assertThat(summary.ingestedAt()).isEqualTo("2026-10-01T00:00:00Z");
                });
    }

    @Test
    void findBySource_같은_파일명의_청크만_센다() {
        insertChunk("대상.pdf", "2026-10-01T00:00:00Z");
        insertChunk("무관.pdf", "2026-10-01T00:00:00Z");

        DocumentSummary summary = documentSummaryMapper.findBySource("대상.pdf");

        assertThat(summary.source()).isEqualTo("대상.pdf");
        assertThat(summary.chunks()).isEqualTo(1);
        assertThat(summary.ingestedAt()).isEqualTo("2026-10-01T00:00:00Z");
    }

    @Test
    void findBySource_적재_이력이_없으면_chunks_가_0_이다() {
        DocumentSummary summary = documentSummaryMapper.findBySource("존재하지-않는-파일.pdf");

        assertThat(summary).isNotNull();
        assertThat(summary.chunks()).isZero();
        assertThat(summary.ingestedAt()).isNull();
    }

    /** VectorStore.add() 는 임베딩 호출이 필요하므로 테스트에서는 행을 직접 넣는다. 트랜잭션은 롤백된다. */
    private void insertChunk(String source, String ingestedAt) {
        String embedding = "[1" + ",0".repeat(1023) + "]";

        jdbcTemplate.update("""
                INSERT INTO vector_store (id, content, metadata, embedding)
                VALUES (?, ?, jsonb_build_object('source', ?, 'ingestedAt', ?), ?::vector)
                """,
                UUID.randomUUID(), "본문", source, ingestedAt, embedding);
    }
}
