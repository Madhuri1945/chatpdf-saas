package com.example.chatpdf.agent.tool;

import com.example.chatpdf.agent.dto.LLMTool;

public interface AgentTool {
    String getName();
    String getDescription();
    LLMTool getDefinition();
    Object execute(String arguments,Long documentId,Long userId);
}
