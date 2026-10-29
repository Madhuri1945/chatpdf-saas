package com.example.chatpdf.agent;

import com.example.chatpdf.agent.dto.AgentResult;
import com.example.chatpdf.agent.tool.ToolRegistry;
import com.example.chatpdf.chat.dto.LLMMessage;
import com.example.chatpdf.chat.dto.LLMTool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentOrchestrator {
    private final AgentLoop agentLoop;
    private final ToolRegistry toolRegistry;
    public AgentOrchestrator(AgentLoop agentLoop, ToolRegistry toolRegistry){
        this.agentLoop=agentLoop;

        this.toolRegistry = toolRegistry;
    }
    public AgentResult run(
            Long documentId,
            String question,
            Long userId,
            List<LLMMessage> history
    ) {
        List<LLMMessage> messages = new ArrayList<>(history);
        messages.add(new LLMMessage("user", question));
        List<LLMTool> availableToolDef=toolRegistry.getDefinitions();
        AgentResult result=agentLoop.run(documentId,userId,history,availableToolDef);
        return result;
    }
}
