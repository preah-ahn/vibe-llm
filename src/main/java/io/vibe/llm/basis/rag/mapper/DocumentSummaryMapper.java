package io.vibe.llm.basis.rag.mapper;

import io.vibe.llm.basis.rag.controller.DocumentSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @since       2026.10.01
 * @author      preah
 * @description document summary mapper
 **********************************************************************************************************************/
/**
 * 벡터 스토어 테이블의 적재 현황 조회. 쓰기는 {@code VectorStore} API 가 담당하므로 읽기 전용이다.
 * <p>
 * 목록/집계 조회는 VectorStore API 로 불가능해 테이블을 직접 읽는다. {@code metadata->>'source'} 는
 * Postgres JSONB 연산자라 JPA 로 표현할 수 없어 이 영역만 MyBatis 를 쓴다.
 * <p>
 * {@code ${tableName}} 은 {@code MyBatisConfig} 가 Spring AI 설정값으로 채운다.
 */
@Mapper
public interface DocumentSummaryMapper {

    @Select("""
            SELECT metadata->>'source' AS source,
                   count(*) AS chunks,
                   max(metadata->>'ingestedAt') AS ingested_at
            FROM ${tableName}
            GROUP BY 1
            ORDER BY 3 DESC
            """)
    List<DocumentSummary> findAll();

    /**
     * 같은 파일명으로 적재된 청크 수와 적재 시각. 집계 쿼리라 적재 이력이 없어도 한 행이 돌아오며,
     * 이때 chunks 는 0, 나머지는 null 이다.
     */
    @Select("""
            SELECT max(metadata->>'source') AS source,
                   count(*) AS chunks,
                   max(metadata->>'ingestedAt') AS ingested_at
            FROM ${tableName}
            WHERE metadata->>'source' = #{source}
            """)
    DocumentSummary findBySource(@Param("source") String source);
}
