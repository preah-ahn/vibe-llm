package io.vibe.llm.common.engine.config.integrate.docling;

import io.vibe.llm.common.engine.config.integrate.docling.handler.DoclingErrorHandler;
import io.vibe.llm.common.engine.config.integrate.docling.interceptor.DoclingRestClientInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * @since       2026.10.01
 * @author      preah
 * @description docling rest client configuration
 **********************************************************************************************************************/
@Configuration
@RequiredArgsConstructor
public class DoclingRestClientConfiguration {

    @Value("${property.integrate.docling.end-point}")
    private String doclingEndPoint;

    @Value("${property.integrate.docling.options.read-timeout}")
    private Duration doclingReadTimeout;

    private final DoclingErrorHandler doclingErrorHandler;
    private final DoclingRestClientInterceptor doclingRestClientInterceptor;

    @Bean
    public RestClient doclingRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(doclingReadTimeout);

        return RestClient.builder()
                .baseUrl(doclingEndPoint)
                .requestFactory(requestFactory)
                .requestInterceptor(doclingRestClientInterceptor)
                .defaultStatusHandler(doclingErrorHandler)
                .build();
    }
}
