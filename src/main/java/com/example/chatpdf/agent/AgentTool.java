package com.example.chatpdf.agent;

import com.example.chatpdf.agent.dto.AgentToolResult;
import com.example.chatpdf.agent.dto.LLMTool;
import com.example.chatpdf.agent.tool.ToolExecutionContext;


public interface AgentTool {
    String getName();

    LLMTool getDefinition();

    AgentToolResult execute(
            String arguments,
            ToolExecutionContext context
    );
}
