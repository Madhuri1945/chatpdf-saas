package com.example.chatpdf.agent;

import com.example.chatpdf.chat.dto.LLMTool;

public interface AgentTool {
    String getName();

    LLMTool getDefinition();

    AgentToolResult execute(
            String arguments,
            ToolExecutionContext context
    );
}
