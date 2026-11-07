package com.example.chatpdf.chat.service;


import com.example.chatpdf.agent.dto.AgentResult;
import com.example.chatpdf.chat.Chat;
import com.example.chatpdf.chat.dto.LLMMessage;
import com.example.chatpdf.message.Message;
import com.example.chatpdf.message.MessageRepository;
import com.example.chatpdf.message.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class ChatMessagePersistenceService {

    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    public ChatMessagePersistenceService(
            MessageRepository messageRepository,
            ObjectMapper objectMapper
    ) {
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveAgentResponse(Chat chat, AgentResult result) {

        for (LLMMessage interaction : result.toolInteractions()) {

            Message message = new Message();
            message.setChat(chat);
            message.setRole(
                    Role.valueOf(interaction.role().toUpperCase())
            );
            message.setContent(interaction.content());
            message.setToolCallId(interaction.toolCallId());

            if (interaction.toolCalls() != null
                    && !interaction.toolCalls().isEmpty()) {

                message.setToolCalls(
                        objectMapper.writeValueAsString(
                                interaction.toolCalls()
                        )
                );
            }

            messageRepository.save(message);
        }

        Message assistantMessage = new Message();
        assistantMessage.setChat(chat);
        assistantMessage.setRole(Role.ASSISTANT);
        assistantMessage.setContent(result.answer());

        messageRepository.save(assistantMessage);
    }
}
