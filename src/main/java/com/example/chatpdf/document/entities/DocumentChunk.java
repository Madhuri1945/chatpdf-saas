package com.example.chatpdf.document.entities;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="document_chunk")
@Data
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    @ManyToOne
    @JoinColumn(name="document_id",nullable = false)
    private Document document;

    @Column(nullable = false)
    private Integer chunkIndex;

    @Lob
    @Column(columnDefinition = "TEXT",nullable=false)
    private String content;

    @Column(columnDefinition = "vector(1536)")
    private PGvector embedding;
    private Integer startOffset;
    private Integer endOffset;
}
