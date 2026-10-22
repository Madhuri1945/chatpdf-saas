package com.example.chatpdf.chat;

import com.example.chatpdf.document.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository  extends JpaRepository<Chat,Long> {

    List<Chat> findByDocumentOrderByCreatedAtDesc(Document document);
}
