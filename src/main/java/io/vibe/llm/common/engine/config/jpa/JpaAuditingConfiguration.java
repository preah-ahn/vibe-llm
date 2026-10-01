package io.vibe.llm.common.engine.config.jpa;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @since       2026.10.01
 * @author      preah
 * @description jpa auditing configuration
 **********************************************************************************************************************/
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
}
