package com.example.chatpdf.chat.dto;

import java.util.List;

public record LLMResponse(
        String model,
        List<Choice> choices
) {
    public record Choice(
            Message message
    ) {}

    public record Message(
            String role,
            String content
    ) {}
}
