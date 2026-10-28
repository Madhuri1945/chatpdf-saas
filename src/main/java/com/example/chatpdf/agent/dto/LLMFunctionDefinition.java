package com.example.chatpdf.agent.dto;

import java.util.Map;

public record LLMFunctionDefinition(
        String name,
        String description,
        Map<String,Object> parameters,
        Boolean strict
) {

}
