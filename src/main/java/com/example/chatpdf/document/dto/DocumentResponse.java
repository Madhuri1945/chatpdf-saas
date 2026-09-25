package com.example.chatpdf.document.dto;


import com.example.chatpdf.document.entities.Status;

import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        String title,
        String filename,
       Status status,
        String mimeType,
        long size,
        LocalDateTime uploadedAt
) {}