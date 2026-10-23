package com.example.chatpdf.agent;

import com.example.chatpdf.agent.dto.AgentResult;
import com.example.chatpdf.chat.dto.LLMMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentOrchestrator {
    private final AgentLoop agentLoop;

    public AgentOrchestrator(AgentLoop agentLoop){
        this.agentLoop=agentLoop;
    }
    public AgentResult run(
            Long documentId,
            String question,
            Long userId,
            List<LLMMessage> history
    ) {
        List<LLMMessage> messages = new ArrayList<>(history);
        messages.add(new LLMMessage("user", question));
        AgentResult result=agentLoop.run(documentId,userId,history,List.of());
        return result;
    }
}
