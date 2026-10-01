package io.vibe.llm.common.engine.config.integrate.docling.handler;

import io.vibe.llm.common.engine.config.integrate.docling.exception.DoclingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * @since       2026.10.01
 * @author      preah
 * @description docling error handler
 **********************************************************************************************************************/
@Slf4j
@Component
public class DoclingErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        log.error("docling error url={} method={} status={} body={}", url, method, response.getStatusCode(), body);
        throw new DoclingException(body, response.getStatusCode());
    }
}
