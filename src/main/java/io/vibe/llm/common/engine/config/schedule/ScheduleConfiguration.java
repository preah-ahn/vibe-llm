package io.vibe.llm.common.engine.config.schedule;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @since       2026.10.01
 * @author      preah
 * @description schedule configuration
 **********************************************************************************************************************/
@Configuration
@EnableScheduling
@Profile({"local", "dev", "prod"})
public class ScheduleConfiguration {

}
