package com.example.chatpdf.document.entities;

import com.example.chatpdf.auth.User;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name="document")
@Data
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String filename;
    private String storagePath;
    private long size;
    @Enumerated(EnumType.STRING)
    private Status status;

    private String mimeType;
    @Column(name="uploaded_at")
    private LocalDateTime uploadedAt;

    @ManyToOne
    @JoinColumn(name="owner_id")
    private User owner;
}
