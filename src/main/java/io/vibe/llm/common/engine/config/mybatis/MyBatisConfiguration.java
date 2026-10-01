package io.vibe.llm.common.engine.config.mybatis;

import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * @since       2026.10.01
 * @author      preah
 * @description my batis configuration
 **********************************************************************************************************************/
/**
 * MyBatis 는 벡터 스토어 조회 전용이다. 일반 조회는 Spring Data JPA 를 쓴다.
 * <p>
 * basePackages 를 basis.rag.mapper 로 좁힌 이유: @MapperScan 은 대상 패키지의 모든 인터페이스를
 * 매퍼로 등록하므로, 범위를 넓게 잡으면 JPA 리포지토리 인터페이스까지 삼켜 빈이 깨진다.
 */
@org.springframework.context.annotation.Configuration
@MapperScan(basePackages = "io.vibe.llm.basis.rag.mapper")
public class MyBatisConfiguration {

    // 매퍼 SQL 의 ${tableName} 에 쓰인다. Spring AI 설정값과 어긋나지 않도록 같은 키를 읽는다.
    @Value("${spring.ai.vectorstore.pgvector.table-name:vector_store}")
    private String tableName;

    @Bean
    public ConfigurationCustomizer vectorStoreTableNameCustomizer() {
        return (Configuration configuration) -> configuration.getVariables().setProperty("tableName", tableName);
    }
}
