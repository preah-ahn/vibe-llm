package io.vibe.llm.common.attach.exception;

/**
 * @since       2026.10.01
 * @author      preah
 * @description attach exception
 **********************************************************************************************************************/
public class AttachException extends RuntimeException {

    public AttachException(String message) {
        super(message);
    }

    public AttachException(String message, Throwable cause) {
        super(message, cause);
    }
}
