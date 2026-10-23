package com.example.chatpdf.chat.dto;

public record LLMToolCall(
        String id,
        String type,
        LLMFunctionCall function
) {
}
