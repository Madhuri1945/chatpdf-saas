package com.example.chatpdf.agent.dto;

import java.util.List;

public record AgentResult(
        String answer,
        List<AgentSource> sources
) {
    public record AgentSource(
            Long chunkId,
            Integer chunkIndex
    ){}
}
