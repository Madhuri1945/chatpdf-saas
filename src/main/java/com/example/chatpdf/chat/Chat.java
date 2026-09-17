package com.example.chatpdf.chat;

import com.example.chatpdf.document.Document;
import jakarta.persistence.*;

@Entity
@Table(name="chat")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @ManyToOne
    @JoinColumn(name="document_id")
    private Document document;
}
