package com.example.chatpdf.document.repository;

import com.example.chatpdf.auth.User;
import com.example.chatpdf.document.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface DocumentRepository extends JpaRepository<Document,Long> {
    List<Document> findByOwner(User owner);
}
