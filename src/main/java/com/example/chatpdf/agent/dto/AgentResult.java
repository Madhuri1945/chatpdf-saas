package com.example.chatpdf.agent.dto;

import com.example.chatpdf.chat.dto.LLMMessage;

import java.util.List;

public record AgentResult(
        String answer,
        List<AgentSource> sources,
        List<LLMMessage> toolInteractions
) {
    public record AgentSource(
            Long chunkId,
            Integer chunkIndex
    ){}
}
