package com.example.chatpdf.agent.tool;

import com.example.chatpdf.agent.dto.LLMFunctionDefinition;
import com.example.chatpdf.agent.dto.LLMTool;
import com.example.chatpdf.agent.dto.SummarizeDocumentArguments;
import com.example.chatpdf.chat.service.LLMService;
import com.example.chatpdf.document.entities.Document;
import com.example.chatpdf.document.entities.DocumentChunk;
import com.example.chatpdf.document.entities.Status;
import com.example.chatpdf.document.repository.DocumentChunkRepository;
import com.example.chatpdf.document.service.DocumentService;
import com.example.chatpdf.exceptions.common.DocumentNotReadyException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SummarizeDocumentTool implements AgentTool{

    private final ObjectMapper objectMapper;
    private final DocumentService documentService;
    private final DocumentChunkRepository chunkRepository;
    private final LLMService llmService;


    public SummarizeDocumentTool(ObjectMapper objectMapper, DocumentService documentService, DocumentChunkRepository chunkRepository, LLMService llmService) {
        this.objectMapper = objectMapper;
        this.documentService = documentService;
        this.chunkRepository = chunkRepository;
        this.llmService = llmService;
    }
    @Override
    public String getName(){
        return "summarize_document";
    }

    @Override
    public String getDescription(){
        return "Generate a summary of the user's entire uploaded PDF.";
    }

    @Override
    public LLMTool getDefinition(){
        Map<String,Object> summaryTypeProperty=Map.of(
                "type", "string",
                "description",
                "Summary style: brief, detailed, or key_points.",
                "enum", List.of("brief", "detailed", "key_points")
        );
        Map<String, Object> parameters = Map.of(
                "type", "object",
                "properties", Map.of(
                        "summaryType", summaryTypeProperty
                ),
                "required", List.of("summaryType"),
                "additionalProperties", false
        );
        LLMFunctionDefinition function =
                new LLMFunctionDefinition(
                        "summarize_document",
                        "Summarize the entire uploaded PDF. "
                                + "Choose brief, detailed, or key_points.",
                        parameters,
                        true
                );
        return new LLMTool("function", function);
    }

    public Object execute(String arguments,Long documentId,Long userId){
        try{
            SummarizeDocumentArguments input=objectMapper.readValue(arguments,SummarizeDocumentArguments.class);
            if (input.summaryType() == null
                    || input.summaryType().isBlank()) {
                throw new IllegalArgumentException(
                        "Summary type cannot be empty"
                );
            }
            List<String> allowedTypes =
                    List.of("brief", "detailed", "key_points");
            if (!allowedTypes.contains(input.summaryType())) {
                throw new IllegalArgumentException(
                        "Unsupported summary type"
                );
            }
            Document document =
                    documentService.getOwnedDocument(documentId, userId);
            if (document.getStatus() != Status.READY) {
                throw new DocumentNotReadyException(
                        "Document is not ready for summarization"
                );
            }
            List<DocumentChunk> chunks =
                    chunkRepository.findByDocumentOrderByChunkIndex(document);
            if (chunks.isEmpty()) {
                throw new IllegalStateException(
                        "No content found in the document"
                );
            }
            String context = chunks.stream()
                    .map(DocumentChunk::getContent)
                    .collect(Collectors.joining("\n\n"));
            String prompt = buildPrompt(input.summaryType());
            String summary = llmService.generateAnswer(
                    prompt,
                    context,
                    List.of()
            );
            return Map.of(
                    "documentId", documentId,
                    "summaryType", input.summaryType(),
                    "summary", summary
            );

        }catch(Exception e){
            if (e instanceof IllegalArgumentException
                    || e instanceof DocumentNotReadyException) {
                throw e;
            }

            throw new IllegalStateException(
                    "Failed to summarize document",
                    e
            );
        }
    }
    private String buildPrompt(String summaryType) {

        return switch (summaryType) {

            case "brief" ->
                    """
                    Summarize the entire provided document.
                    Explain its overall purpose and main ideas
                    in a concise manner.
                    Use only information supported by the document.
                    """;

            case "detailed" ->
                    """
                    Create a detailed summary of the entire document.
                    Explain its important concepts, sections,
                    arguments and conclusions.
                    Preserve important details and relationships.
                    Use only information supported by the document.
                    """;

            case "key_points" ->
                    """
                    Extract the most important points from the entire
                    document.
                    Present them as clear, organized bullet points.
                    Use only information supported by the document.
                    """;

            default -> throw new IllegalArgumentException(
                    "Unsupported summary type"
            );
        };
    }
}
