package io.vibe.llm.common.engine.config.integrate.docling.exception;

import org.springframework.http.HttpStatusCode;

/**
 * @since       2026.10.01
 * @author      preah
 * @description docling exception
 **********************************************************************************************************************/
public class DoclingException extends RuntimeException {

    private final HttpStatusCode status;

    public DoclingException(String message, HttpStatusCode status) {
        super(message);
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return status;
    }
}
