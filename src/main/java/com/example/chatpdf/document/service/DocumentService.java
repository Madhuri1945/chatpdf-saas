package com.example.chatpdf.document.service;

import com.example.chatpdf.auth.User;
import com.example.chatpdf.document.dto.DocumentListResponse;
import com.example.chatpdf.document.dto.DocumentResponse;
import com.example.chatpdf.document.dto.UploadDocumentResponse;
import com.example.chatpdf.document.entities.Document;
import com.example.chatpdf.document.entities.DocumentChunk;
import com.example.chatpdf.document.entities.DocumentContent;
import com.example.chatpdf.document.entities.Status;
import com.example.chatpdf.document.exception.DocumentNotFoundException;
import com.example.chatpdf.document.exception.EmptyPdfException;
import com.example.chatpdf.document.exception.FileTooLargeException;
import com.example.chatpdf.document.exception.InvalidFileException;
import com.example.chatpdf.document.repository.DocumentChunkRepository;
import com.example.chatpdf.document.repository.DocumentContentRepository;
import com.example.chatpdf.document.repository.DocumentRepository;
import com.example.chatpdf.document.storage.StorageService;
import com.pgvector.PGvector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository repo;
    private final StorageService storageService;
    private final PdfProcessingService pdfProcessingService;
    private final DocumentContentRepository contentRepository;
    private final TextChunkingService chunkingService;
    private final DocumentChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    public DocumentService(
            DocumentRepository repo,
            StorageService storageService,
            PdfProcessingService pdfProcessingService,
            DocumentContentRepository contentRepository, TextChunkingService chunkingService, DocumentChunkRepository chunkRepository, EmbeddingService embeddingService
    ) {
        this.repo = repo;
        this.storageService = storageService;
        this.pdfProcessingService = pdfProcessingService;
        this.contentRepository = contentRepository;
        this.chunkingService = chunkingService;
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
    }

    public UploadDocumentResponse upload(
            MultipartFile file,
            String title,
            User owner
    ) {

        validateFile(file);

        String storagePath = storageService.store(file);

        Document document = new Document();
        document.setTitle(title);
        document.setFilename(file.getOriginalFilename());
        document.setStoragePath(storagePath);
        document.setMimeType(file.getContentType());
        document.setSize(file.getSize());
        document.setStatus(Status.UPLOADING);
        document.setUploadedAt(LocalDateTime.now());
        document.setOwner(owner);

        Document saved = repo.save(document);

        // Only one line
        processDocument(saved);

        return new UploadDocumentResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getStatus()
        );
    }

    public List<DocumentListResponse> getMyDocuments(User owner) {
        return repo.findByOwner(owner)
                .stream()
                .map(doc -> new DocumentListResponse(
                        doc.getId(),
                        doc.getTitle(),
                        doc.getStatus(),
                        doc.getUploadedAt()
                ))
                .toList();
    }

    public DocumentResponse getDocument(Long id, User owner) {

        Document document = repo.findById(id)
                .orElseThrow(DocumentNotFoundException::new);

        if (!document.getOwner().getId().equals(owner.getId())) {
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


    // -----------------------------
    // Processing Pipeline
    // -----------------------------
    private void processDocument(Document document) {

        document.setStatus(Status.PROCESSING);
        repo.save(document);

        try {

            String text = pdfProcessingService.extractText(
                    document.getStoragePath()
            );

            if (text.isBlank()) {
                throw new EmptyPdfException();
            }

            DocumentContent content = new DocumentContent();
            content.setDocument(document);
            content.setExtractedText(text);

            contentRepository.save(content);
            List<String> chunks=chunkingService.chunk(text);
            List<float[]> embeddings=embeddingService.generateEmbeddings(chunks);

            int start=0;
            for(int index=0;index<chunks.size();index++){
                String chunk=chunks.get(index);
                float[] embedding=embeddings.get(index);
                DocumentChunk entity=new DocumentChunk();
                entity.setDocument(document);
                entity.setContent(chunk);
                entity.setEmbedding(embedding);
                entity.setChunkIndex(index);
                entity.setStartOffset(start);
                entity.setEndOffset(start+chunk.length());
                chunkRepository.save(entity);

            }

            document.setStatus(Status.READY);

        } catch (Exception e) {

            document.setStatus(Status.FAILED);
        }

        repo.save(document);

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
    public Document getOwnedDocument(Long id, Long userId) {

        Document document = repo.findById(id)
                .orElseThrow(DocumentNotFoundException::new);

        if (!document.getOwner().getId().equals(userId)) {
            throw new DocumentNotFoundException();
        }

        return document;
    }
}