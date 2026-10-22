package com.example.chatpdf.document.service;

import com.pgvector.PGvector;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmbeddingService {
    List<float[]> generateEmbeddings(List<String> texts);
}
