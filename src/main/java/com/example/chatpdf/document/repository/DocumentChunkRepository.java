package com.example.chatpdf.document.repository;

import com.example.chatpdf.document.entities.Document;
import com.example.chatpdf.document.entities.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk,Long> {

    List<DocumentChunk> findByDocumentOrderByChunkIndex(Document document);
}
