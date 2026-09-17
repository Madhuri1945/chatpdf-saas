package com.example.chatpdf.message;

import com.example.chatpdf.chat.Chat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name="created_at")
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name="chat_id")
    private Chat chat;
}
