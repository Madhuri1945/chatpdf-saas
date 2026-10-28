package com.example.chatpdf.agent.dto;

public record LLMTool(
        String type,
        LLMFunctionDefinition function
) {
}
