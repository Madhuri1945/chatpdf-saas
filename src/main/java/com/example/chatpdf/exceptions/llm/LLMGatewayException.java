package com.example.chatpdf.exceptions.llm;

public class LLMGatewayException extends RuntimeException {

    public LLMGatewayException(String message) {
        super(message);
    }

    public LLMGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
