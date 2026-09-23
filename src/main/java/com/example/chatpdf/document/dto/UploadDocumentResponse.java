package com.example.chatpdf.document.dto;

import com.example.chatpdf.document.entities.Status;

public record UploadDocumentResponse(
        Long id,
        String title,
        Status status
) {}
