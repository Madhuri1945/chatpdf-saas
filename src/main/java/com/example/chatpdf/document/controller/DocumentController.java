package com.example.chatpdf.document.controller;

import com.example.chatpdf.auth.CustomUserDetails;
import com.example.chatpdf.config.ApiResponse;
import com.example.chatpdf.document.dto.DocumentListResponse;
import com.example.chatpdf.document.dto.DocumentResponse;
import com.example.chatpdf.document.dto.UploadDocumentResponse;
import com.example.chatpdf.document.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UploadDocumentResponse>> upload(
            @RequestParam("file")MultipartFile file,
            @RequestParam("title") String title,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ){
        UploadDocumentResponse response=documentService.upload(file,title,userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentListResponse>>> getDocuments(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        List<DocumentListResponse> response=documentService.getMyDocuments(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        DocumentResponse response=documentService.getDocument(id,userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
