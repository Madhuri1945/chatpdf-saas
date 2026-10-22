package com.example.chatpdf.chat;

import com.example.chatpdf.auth.CustomUserDetails;
import com.example.chatpdf.chat.dto.ChatRequest;
import com.example.chatpdf.chat.dto.ChatResponse;
import com.example.chatpdf.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/documents/{documentId}/chats")
    public ResponseEntity<ChatResponse> askQuestion(
            @PathVariable Long documentId,
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ){
        ChatResponse response=chatService.askQuestion(
                documentId, request.message(), userDetails.getUser()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/chats/{chatId}/messages")
    public ResponseEntity<ChatResponse> sendMessage(
            @PathVariable Long chatId,
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        ChatResponse response=chatService.sendMessage(
                chatId, request.message(), userDetails.getUser()
        );
        return ResponseEntity.ok(response);
    }
}
