package com.example.chatpdf.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LLMRequest(
        String model, List<LLMMessage> messages,
        List<LLMTool> tools,
        @JsonProperty("tool_choice") String toolChoice
) {
    public LLMRequest(String model, List<LLMMessage> messages) {
        this(model, messages, null, null);
    }
}
