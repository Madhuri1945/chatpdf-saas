package com.example.chatpdf.chat;

import com.example.chatpdf.document.entities.Document;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="chat")
@Data
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @ManyToOne
    @JoinColumn(name="document_id",nullable=false)
    private Document document;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
