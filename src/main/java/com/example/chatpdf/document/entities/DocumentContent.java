package com.example.chatpdf.document.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="document_content")
@Data

public class DocumentContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="document_id",nullable = false)
    private Document document;

    @Lob
    @Column(columnDefinition = "Text")
    private String extractedText;
}
