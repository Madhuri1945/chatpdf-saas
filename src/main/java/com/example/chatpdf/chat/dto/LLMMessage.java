package com.example.chatpdf.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record LLMMessage(
        String role,
        String content,
        @JsonProperty("tool_calls") List<LLMToolCall> toolCalls,
        @JsonProperty("tool_call_id") String toolCallId
) {
    public LLMMessage(String role, String content) {
        this(role, content, null, null);
    }
}