package com.example.chatpdf.document.dto;

import com.example.chatpdf.document.entities.Status;

public record DocumentListResponse(
        Long id,
        String title,
        Status status,
        java.time.LocalDateTime uploadedAt) {
}
