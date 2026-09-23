package com.example.chatpdf.document;

import com.example.chatpdf.auth.User;
import com.example.chatpdf.document.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface DocumentRepository extends JpaRepository<Document,Long> {
    List<Document> findByOwner(User owner);
}
