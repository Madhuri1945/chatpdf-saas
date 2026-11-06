package com.example.chatpdf.chat.service;

import com.example.chatpdf.agent.AgentOrchestrator;
import com.example.chatpdf.agent.dto.AgentResult;
import com.example.chatpdf.auth.User;
import com.example.chatpdf.chat.Chat;
import com.example.chatpdf.chat.ChatRepository;
import com.example.chatpdf.chat.dto.ChatResponse;
import com.example.chatpdf.chat.dto.LLMMessage;
import com.example.chatpdf.document.entities.Document;
import com.example.chatpdf.document.entities.DocumentChunk;
import com.example.chatpdf.document.entities.Status;
import com.example.chatpdf.document.exception.DocumentNotFoundException;
import com.example.chatpdf.document.repository.DocumentRepository;
import com.example.chatpdf.document.service.SimilaritySearchService;
import com.example.chatpdf.exceptions.common.DocumentNotReadyException;
import com.example.chatpdf.message.Message;
import com.example.chatpdf.message.MessageRepository;
import com.example.chatpdf.message.Role;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ChatService {

    private final DocumentRepository documentRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final SimilaritySearchService similaritySearchService;
    private final LLMService llmService;
    private final AgentOrchestrator agentOrchestrator;
    private final ObjectMapper objectMapper;

    public ChatService(DocumentRepository documentRepository, ChatRepository chatRepository, MessageRepository messageRepository, SimilaritySearchService similaritySearchService, LLMService llmService
    ,AgentOrchestrator agentOrchestrator,ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.similaritySearchService = similaritySearchService;
        this.llmService = llmService;
        this.agentOrchestrator=agentOrchestrator;
        this.objectMapper=objectMapper;
    }

    public ChatResponse askQuestion(Long documentId, String question, User user){
        Document doc=documentRepository.findById(documentId).orElseThrow(
                ()->new DocumentNotFoundException()
        );
        if(!doc.getOwner().getId().equals(user.getId())){
            throw new DocumentNotFoundException();
        }
        if (doc.getStatus() != Status.READY) {
            throw new DocumentNotReadyException("document not found");
        }
        Chat chat=new Chat();
        chat.setDocument(doc);
        chatRepository.save(chat);
        Message message=new Message();
        message.setContent(question);
        message.setChat(chat);
        message.setRole(Role.USER);
        messageRepository.save(message);
        AgentResult result=agentOrchestrator.run(documentId,question, user.getId(), List.of());
        saveToolInteractions(chat,result.toolInteractions());
        Message assistantMessage=new Message();
        assistantMessage.setChat(chat);
        assistantMessage.setContent(result.answer());
        assistantMessage.setRole(Role.ASSISTANT);
        messageRepository.save(assistantMessage);
        List<ChatResponse.SourceResponse> sources = result.sources().stream()
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

    public ChatResponse sendMessage(Long chatId, String question, User user) {
        Chat chat=chatRepository.findById(chatId).orElseThrow(
                ()->new RuntimeException("chat not found")
        );
        if(!chat.getDocument().getOwner().getId().equals(user.getId())){
            throw new DocumentNotFoundException();
        }

        Long documentId=chat.getDocument().getId();
        List<Message> previousMessages=messageRepository.findByChatOrderByCreatedAtAsc(chat);
        List<LLMMessage> history=previousMessages.stream()
                .map(message->new LLMMessage(
                        message.getRole().name().toLowerCase(),
                        message.getContent()
                )).toList();
        Message userMessage=new Message();
        userMessage.setChat(chat);
        userMessage.setContent(question);
        userMessage.setRole(Role.USER);
        messageRepository.save(userMessage);
        AgentResult result = agentOrchestrator.run(
                documentId,
                question,
                user.getId(),
                history
        );
        saveToolInteractions(chat, result.toolInteractions());
        Message assistantMessage=new Message();
        assistantMessage.setChat(chat);
        assistantMessage.setContent(result.answer());
        assistantMessage.setRole(Role.ASSISTANT);
        messageRepository.save(assistantMessage);

        List<ChatResponse.SourceResponse> sources=result.sources().stream()
                .map(source->new ChatResponse.SourceResponse(
                        source.chunkId(), source.chunkIndex()
                )).toList();
        return new ChatResponse(
                chat.getId(),
                question, result.answer(), sources
        );
    }
    private void saveToolInteractions(
            Chat chat,
            List<LLMMessage> interactions
    ) {
        for (LLMMessage interaction : interactions) {

            Message message = new Message();

            // Associate this message with the current chat
            message.setChat(chat);

            // Convert LLM role into your database Role enum
            message.setRole(
                    Role.valueOf(interaction.role().toUpperCase())
            );

            // Save message content
            message.setContent(interaction.content());

            // Save tool call ID (used for tool-result messages)
            message.setToolCallId(interaction.toolCallId());
            // Save assistant tool calls as JSON
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
    }
}
