package com.example.chatpdf.agent.tool;

import com.example.chatpdf.agent.dto.AgentResult;
import com.example.chatpdf.agent.dto.DocumentOutlineArguments;
import com.example.chatpdf.agent.dto.LLMFunctionDefinition;
import com.example.chatpdf.agent.dto.LLMTool;
import com.example.chatpdf.chat.service.LLMService;
import com.example.chatpdf.document.entities.Document;
import com.example.chatpdf.document.entities.DocumentChunk;
import com.example.chatpdf.document.entities.Status;
import com.example.chatpdf.document.repository.DocumentChunkRepository;
import com.example.chatpdf.document.service.DocumentService;
import com.example.chatpdf.exceptions.common.DocumentNotReadyException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GetDocumentOutlineTool implements AgentTool {

    private final ObjectMapper objectMapper;
    private final DocumentService documentService;
    private final DocumentChunkRepository documentChunkRepository;
    private final LLMService llmService;

    public GetDocumentOutlineTool(ObjectMapper objectMapper, DocumentService documentService, DocumentChunkRepository documentChunkRepository, LLMService llmService) {
        this.objectMapper = objectMapper;
        this.documentService = documentService;
        this.documentChunkRepository = documentChunkRepository;
        this.llmService = llmService;
    }

    @Override
    public String getName(){
        return "get_document_outline";
    }

    @Override
    public String getDescription(){
        return "Extract the chapters and sections from the uploaded PDF.";
    }

    @Override
    public LLMTool getDefinition(){
        Map<String, Object> detailLevelProperty = Map.of(
                "type", "string",
                "description", "Level of detail for the document outline.",
                "enum", List.of("brief", "detailed")
        );
        Map<String, Object> parameters = Map.of(
                "type", "object",
                "properties", Map.of(
                        "detailLevel", detailLevelProperty
                ),
                "required", List.of("detailLevel"),
                "additionalProperties", false
        );
        LLMFunctionDefinition function = new LLMFunctionDefinition(
                "get_document_outline",
                "Extract the document's chapter and section structure.",
                parameters,
                true
        );
        return new LLMTool("function", function);
    }

    public Object execute(String arguments,Long documentId,Long userId){
        try{
            DocumentOutlineArguments input=objectMapper.readValue(arguments, DocumentOutlineArguments.class);
            if (input.detailLevel() == null
                    || input.detailLevel().isBlank()) {
                throw new IllegalArgumentException(
                        "Detail level cannot be empty"
                );
            }

            if (!List.of("brief", "detailed")
                    .contains(input.detailLevel())) {
                throw new IllegalArgumentException(
                        "Unsupported detail level"
                );
            }
            Document document =
                    documentService.getOwnedDocument(documentId, userId);
            if (document.getStatus() != Status.READY) {
                throw new DocumentNotReadyException(
                        "Document is not ready"
                );
            }
            List<DocumentChunk> chunks =
                    documentChunkRepository.findByDocumentOrderByChunkIndex(document);
            if (chunks.isEmpty()) {
                throw new IllegalStateException(
                        "No document content available"
                );
            }
            String context = chunks.stream()
                    .map(DocumentChunk::getContent)
                    .collect(Collectors.joining("\n\n"));
            String prompt = buildPrompt(input.detailLevel());
            String outline = llmService.generateAnswer(
                    prompt,
                    context,
                    List.of()
            );
            return Map.of(
                    "documentId", documentId,
                    "detailLevel", input.detailLevel(),
                    "outline", outline
            );

        }catch(Exception e){
            if (e instanceof IllegalArgumentException
                    || e instanceof DocumentNotReadyException) {
                throw e;
            }

            throw new IllegalStateException(
                    "Failed to generate document outline",
                    e
            );
        }

    }
    private String buildPrompt(String detailLevel) {

        if ("brief".equals(detailLevel)) {
            return """
                    Extract the chapter and section headings
                    from the provided document.

                    Preserve their original order and hierarchy.
                    Use numbered headings and nested bullet points.
                    Do not invent headings that are not supported
                    by the document.
                    """;
        }

        return """
                Analyze the entire provided document and create
                a detailed hierarchical outline.

                Include:
                1. Main chapters
                2. Sections and subsections
                3. A short description of each major section

                Preserve the original document order.
                Do not invent chapters or sections.
                If an explicit heading cannot be identified,
                label it as an inferred topic rather than an
                original heading.
                """;
    }

}
