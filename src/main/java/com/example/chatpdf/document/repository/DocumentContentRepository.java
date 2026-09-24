package com.example.chatpdf.document.repository;

import com.example.chatpdf.document.entities.DocumentContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentContentRepository extends JpaRepository<DocumentContent,Long> {
}
