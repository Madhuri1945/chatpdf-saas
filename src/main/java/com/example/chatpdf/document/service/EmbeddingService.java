package com.example.chatpdf.document.service;

import com.pgvector.PGvector;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmbeddingService {
    List<PGvector> generateEmbeddings(List<String> texts);
}
