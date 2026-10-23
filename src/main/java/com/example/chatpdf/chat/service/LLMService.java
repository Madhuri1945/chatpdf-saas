package com.example.chatpdf.chat.service;

import com.example.chatpdf.chat.dto.LLMMessage;
import com.example.chatpdf.chat.dto.LLMRequest;
import com.example.chatpdf.chat.dto.LLMResponse;
import com.example.chatpdf.message.Message;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface LLMService {

    String generateAnswer(
            String question,
            String context,
            List<LLMMessage> history
    );
    LLMResponse generateResponse(LLMRequest request);
}
