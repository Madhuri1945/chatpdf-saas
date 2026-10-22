package com.example.chatpdf.chat.dto;

import java.util.List;

public record LLMRequest(
        String model, List<LLMMessage> messages
) {
}
