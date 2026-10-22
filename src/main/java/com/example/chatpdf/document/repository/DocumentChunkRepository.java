package com.example.chatpdf.document.repository;

import com.example.chatpdf.document.entities.Document;
import com.example.chatpdf.document.entities.DocumentChunk;
import com.pgvector.PGvector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk,Long> {

    List<DocumentChunk> findByDocumentOrderByChunkIndex(Document document);


    @Query(value = """
        SELECT *
        FROM document_chunk
        where document_id=:documentId
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(
            @Param("documentId") Long documentId,
            @Param("embedding") float[] embedding,
            @Param("limit") int limit
    );
}
