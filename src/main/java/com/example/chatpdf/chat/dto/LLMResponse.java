package com.example.chatpdf.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LLMResponse(
        String model,
        List<Choice> choices

) {
    public record Choice(
            Message message,
            @JsonProperty("finish_reason") String finishReason
    ) {}

    public record Message(
            String role,
            String content,
            @JsonProperty("tool_calls") List<LLMToolCall> toolCalls
    ) {}
}
