package com.example.chatpdf.document.service;

import com.example.chatpdf.auth.User;
import com.example.chatpdf.document.dto.DocumentListResponse;
import com.example.chatpdf.document.dto.DocumentResponse;
import com.example.chatpdf.document.dto.UploadDocumentResponse;
import com.example.chatpdf.document.entities.Document;
import com.example.chatpdf.document.entities.DocumentContent;
import com.example.chatpdf.document.entities.Status;
import com.example.chatpdf.document.exception.DocumentNotFoundException;
import com.example.chatpdf.document.exception.EmptyPdfException;
import com.example.chatpdf.document.exception.FileTooLargeException;
import com.example.chatpdf.document.exception.InvalidFileException;
import com.example.chatpdf.document.repository.DocumentContentRepository;
import com.example.chatpdf.document.repository.DocumentRepository;
import com.example.chatpdf.document.storage.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository repo;
    private final StorageService storageService;
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;
    private final PdfProcessingService pdfProcessingService;
    private final DocumentContentRepository contentRepository;

    public DocumentService(DocumentRepository repo, StorageService storageService, PdfProcessingService pdfProcessingService, DocumentContentRepository contentRepository) {
        this.repo = repo;
        this.storageService = storageService;
        this.pdfProcessingService = pdfProcessingService;
        this.contentRepository = contentRepository;
    }
    public UploadDocumentResponse upload(
            MultipartFile file,
            String title,
            User owner
    ){
        validateFile(file);
        String storagePath= storageService.store(file);
        Document document=new Document();
        document.setTitle(title);
        document.setFilename(file.getOriginalFilename());
        document.setStoragePath(storagePath);
        document.setSize(file.getSize());
        document.setStatus(Status.UPLOADING);
        document.setUploadedAt(LocalDateTime.now());
        document.setOwner(owner);
        Document saved=repo.save(document);
        saved.setStatus(Status.PROCESSING);
        repo.save(saved);
        try{
            String text= pdfProcessingService.extractText(saved.getStoragePath());
            if(text.isBlank()) throw new EmptyPdfException();
            DocumentContent content=new DocumentContent();
            content.setDocument(saved);
            content.setExtractedText(text);
            contentRepository.save(content);
            saved.setStatus(Status.READY);
        }catch(Exception e){
                saved.setStatus(Status.FAILED);
        }
        repo.save(saved);
        return new UploadDocumentResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getStatus()
        );
    }
    public List<DocumentListResponse> getMyDocuments(User owner){
        return repo.findByOwner(owner).stream()
                .map(doc->new DocumentListResponse(
                        doc.getId(),doc.getTitle(),doc.getStatus(),doc.getUploadedAt()
                )).toList();
    }
    public DocumentResponse getDocument(Long id, User owner){
        Document document=repo.findById(id).orElseThrow(DocumentNotFoundException::new);
        if(!document.getOwner().getId().equals(owner.getId())){
            throw new DocumentNotFoundException();
        }
        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getFilename(),
                document.getStatus(),
                document.getMimeType(),
                document.getSize(),
                document.getUploadedAt()
        );
    }
    private void validateFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new InvalidFileException("Only PDF files are allowed");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileTooLargeException();
        }
    }

}
