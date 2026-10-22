package com.example.chatpdf.document.dto;

import java.util.List;

public record EmbeddingRequest(
        String model, List<String> input
) {
}
