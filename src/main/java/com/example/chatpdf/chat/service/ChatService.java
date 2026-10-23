package com.example.chatpdf.chat.service;

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
import org.springframework.stereotype.Service;

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

    public ChatService(DocumentRepository documentRepository, ChatRepository chatRepository, MessageRepository messageRepository, SimilaritySearchService similaritySearchService, LLMService llmService) {
        this.documentRepository = documentRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.similaritySearchService = similaritySearchService;
        this.llmService = llmService;
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
        List<DocumentChunk> chunks=similaritySearchService.search(documentId,question,5);
        String context = chunks.stream()
                .map(DocumentChunk::getContent)
                .collect(java.util.stream.Collectors.joining(
                        "\n\n--- Relevant passage ---\n\n"
                ));
        String answer= llmService.generateAnswer(question,context,List.of());
        Message assistantMessage=new Message();
        assistantMessage.setChat(chat);
        assistantMessage.setContent(answer);
        assistantMessage.setRole(Role.ASSISTANT);
        messageRepository.save(assistantMessage);
        List<ChatResponse.SourceResponse> sources =
                chunks.stream()
                        .map(chunk ->
                                new ChatResponse.SourceResponse(
                                        chunk.getId(),
                                        chunk.getChunkIndex()
                                )
                        )
                        .toList();
        return new ChatResponse(
                chat.getId(),
                question,
                answer,
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
        List<DocumentChunk> chunks=similaritySearchService.search(documentId,question,5);
        String context=chunks.stream().map(DocumentChunk::getContent).collect(java.util.stream.Collectors.joining("\n\n"));
        String answer= llmService.generateAnswer(
                question,context,history
        );
        Message assistantMessage=new Message();
        assistantMessage.setChat(chat);
        assistantMessage.setContent(answer);
        assistantMessage.setRole(Role.ASSISTANT);
        messageRepository.save(assistantMessage);

        List<ChatResponse.SourceResponse> sources=chunks.stream()
                .map(chunk->new ChatResponse.SourceResponse(
                        chunk.getId(),chunk.getChunkIndex()
                )).toList();
        return new ChatResponse(
                chat.getId(),
                question,answer,sources
        );
    }
}
