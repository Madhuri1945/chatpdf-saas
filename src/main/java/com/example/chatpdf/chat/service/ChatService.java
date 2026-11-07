
package com.example.chatpdf.chat.service;

import com.example.chatpdf.agent.AgentOrchestrator;
import com.example.chatpdf.agent.dto.AgentResult;
import com.example.chatpdf.auth.User;
import com.example.chatpdf.chat.Chat;
import com.example.chatpdf.chat.ChatRepository;
import com.example.chatpdf.chat.dto.ChatResponse;
import com.example.chatpdf.chat.dto.LLMMessage;
import com.example.chatpdf.document.entities.Document;
import com.example.chatpdf.document.entities.Status;
import com.example.chatpdf.document.exception.DocumentNotFoundException;
import com.example.chatpdf.document.repository.DocumentRepository;
import com.example.chatpdf.exceptions.common.DocumentNotReadyException;
import com.example.chatpdf.message.Message;
import com.example.chatpdf.message.MessageRepository;
import com.example.chatpdf.message.Role;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final DocumentRepository documentRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ConversationContextService contextService;
    private final AgentOrchestrator agentOrchestrator;
    private final ChatMessagePersistenceService chatMessagePersistenceService;

    public ChatService(
            DocumentRepository documentRepository,
            ChatRepository chatRepository,
            MessageRepository messageRepository,
            ConversationContextService contextService,
            AgentOrchestrator agentOrchestrator,
            ChatMessagePersistenceService chatMessagePersistenceService
    ) {
        this.documentRepository = documentRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.contextService = contextService;
        this.agentOrchestrator = agentOrchestrator;
        this.chatMessagePersistenceService = chatMessagePersistenceService;
    }

    public ChatResponse askQuestion(
            Long documentId,
            String question,
            User user
    ) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(DocumentNotFoundException::new);

        validateDocument(doc, user);

        Chat chat = new Chat();
        chat.setDocument(doc);
        chatRepository.save(chat);

        saveUserMessage(chat, question);

        AgentResult result = agentOrchestrator.run(
                documentId,
                question,
                user.getId(),
                List.of()
        );

        chatMessagePersistenceService.saveAgentResponse(chat, result);

        return buildResponse(chat, question, result);
    }

    public ChatResponse sendMessage(
            Long chatId,
            String question,
            User user
    ) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        validateDocument(chat.getDocument(), user);

        Long documentId = chat.getDocument().getId();

        List<LLMMessage> history = messageRepository
                .findByChatOrderByCreatedAtAscIdAsc(chat)
                .stream()
                .map(this::toLLMMessage)
                .toList();

        history = contextService.buildContext(history);

        saveUserMessage(chat, question);

        AgentResult result = agentOrchestrator.run(
                documentId,
                question,
                user.getId(),
                history
        );

        chatMessagePersistenceService.saveAgentResponse(chat, result);

        return buildResponse(chat, question, result);
    }

    private void validateDocument(Document doc, User user) {

        if (!doc.getOwner().getId().equals(user.getId())) {
            throw new DocumentNotFoundException();
        }

        if (doc.getStatus() != Status.READY) {
            throw new DocumentNotReadyException("Document is not ready");
        }
    }

    private void saveUserMessage(Chat chat, String question) {

        Message message = new Message();
        message.setChat(chat);
        message.setContent(question);
        message.setRole(Role.USER);

        messageRepository.save(message);
    }

    private LLMMessage toLLMMessage(Message message) {

        List<com.example.chatpdf.chat.dto.LLMToolCall> toolCalls = null;

        if (message.getToolCalls() != null
                && !message.getToolCalls().isBlank()) {

            try {
                toolCalls = new tools.jackson.databind.ObjectMapper()
                        .readValue(
                                message.getToolCalls(),
                                new tools.jackson.core.type.TypeReference<
                                        List<com.example.chatpdf.chat.dto.LLMToolCall>
                                        >() {}
                        );
            } catch (Exception e) {
                throw new IllegalStateException(
                        "Failed to deserialize tool calls for message "
                                + message.getId(),
                        e
                );
            }
        }

        return new LLMMessage(
                message.getRole().name().toLowerCase(),
                message.getContent(),
                toolCalls,
                message.getToolCallId()
        );
    }

    private ChatResponse buildResponse(
            Chat chat,
            String question,
            AgentResult result
    ) {
        List<ChatResponse.SourceResponse> sources = result.sources()
                .stream()
                .map(source -> new ChatResponse.SourceResponse(
                        source.chunkId(),
                        source.chunkIndex()
                ))
                .toList();

        return new ChatResponse(
                chat.getId(),
                question,
                result.answer(),
                sources
        );
    }
}
