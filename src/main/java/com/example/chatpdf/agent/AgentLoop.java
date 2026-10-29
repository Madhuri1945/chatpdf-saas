package com.example.chatpdf.agent;

import com.example.chatpdf.agent.dto.AgentResult;
import com.example.chatpdf.chat.dto.LLMMessage;
import com.example.chatpdf.chat.dto.LLMTool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentLoop {
    public AgentResult run(
            Long documentId,
            Long userId,
            List<LLMMessage> messages,
            List<LLMTool> tools
    ) {
        throw new UnsupportedOperationException(
                "Agent loop is not implemented yet"
        );
    }
}
