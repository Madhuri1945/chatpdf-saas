package com.example.chatpdf.chat.dto;

import java.util.List;

public record ChatResponse(
        Long chatId,
        String question,
        String answer,
        List<SourceResponse> sources
) {
    public record SourceResponse(
      Long chunkId,
      Integer chunkIndex
    ){

    }
}
