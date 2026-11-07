package com.example.chatpdf.chat.service;

import com.example.chatpdf.chat.dto.LLMMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationContextService {

    @Value("${agent.context.max-characters:12000}")
    private int maxCharacters;

    public List<LLMMessage> buildContext(List<LLMMessage> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }
        int start = history.size();
        int totalCharacters = 0;
        for (int i = history.size() - 1; i >= 0; i--) {
            LLMMessage message = history.get(i);
            int messageSize = estimateCharacters(message);
            if (totalCharacters + messageSize > maxCharacters) {
                break;
            }
            totalCharacters += messageSize;
            start = i;
        }
        if (start == history.size()) {
            start = history.size() - 1;
        }
        if ("tool".equals(history.get(start).role())) {
            for (int i = start - 1; i >= 0; i--) {
                LLMMessage previous = history.get(i);
                if ("assistant".equals(previous.role()) && previous.toolCalls() != null && !previous.toolCalls().isEmpty()) {
                    start = i;
                    break;
                }

            }

        }
        return history.subList(start, history.size());
    }
    private int estimateCharacters(LLMMessage message){
        int size=message.content()==null?0:message.content().length();
        if(message.toolCalls()!=null){
            size+=message.toolCalls().toString().length();
        }
        if(message.toolCallId()!=null){
            size+=message.toolCallId().length();
        }
        return size;
    }


}
