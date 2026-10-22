package com.example.chatpdf.document.service;

import com.example.chatpdf.document.entities.DocumentChunk;
import com.example.chatpdf.document.repository.DocumentChunkRepository;
import com.pgvector.PGvector;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class SimilaritySearchService {

    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;

    public SimilaritySearchService(EmbeddingService embeddingService, DocumentChunkRepository documentChunkRepository) {
        this.embeddingService = embeddingService;
        this.documentChunkRepository = documentChunkRepository;
    }

    public List<DocumentChunk> search(Long documentId,String question, int limit){

        List<float[]> embeddings=embeddingService.generateEmbeddings(List.of(question));
       float[] questionEmbedding=embeddings.get(0);
        return documentChunkRepository.findSimilarChunks(documentId,questionEmbedding,limit);
    }
}
