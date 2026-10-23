package com.example.chatpdf.chat.dto;

public record LLMTool(
        String type,
        LLMFunctionDefinition function
) {
}
