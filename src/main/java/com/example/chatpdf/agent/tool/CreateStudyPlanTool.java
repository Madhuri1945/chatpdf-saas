
package com.example.chatpdf.agent.tool;

import com.example.chatpdf.agent.dto.CreateStudyPlanArguments;
import com.example.chatpdf.agent.dto.LLMFunctionDefinition;
import com.example.chatpdf.agent.dto.LLMTool;
import com.example.chatpdf.agent.dto.StudyPlanResponse;
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
public class CreateStudyPlanTool implements AgentTool {

    private final ObjectMapper objectMapper;
    private final DocumentService documentService;
    private final DocumentChunkRepository chunkRepository;
    private final LLMService llmService;
    private final StudyPlanValidator validator;

    public CreateStudyPlanTool(
            ObjectMapper objectMapper,
            DocumentService documentService,
            DocumentChunkRepository chunkRepository,
            LLMService llmService,
            StudyPlanValidator validator
    ) {
        this.objectMapper = objectMapper;
        this.documentService = documentService;
        this.chunkRepository = chunkRepository;
        this.llmService = llmService;
        this.validator = validator;
    }

    @Override
    public String getName() {
        return "create_study_plan";
    }

    @Override
    public String getDescription() {
        return "Create a personalized study plan based on the uploaded PDF.";
    }

    @Override
    public LLMTool getDefinition() {

        Map<String, Object> parameters = Map.of(
                "type", "object",
                "properties", Map.of(
                        "numberOfDays", Map.of(
                                "type", "integer",
                                "minimum", 1,
                                "maximum", 30,
                                "description", "Number of days for the study plan"
                        ),
                        "hoursPerDay", Map.of(
                                "type", "number",
                                "minimum", 0.5,
                                "maximum", 12,
                                "description", "Available study hours per day"
                        ),
                        "learningPreference", Map.of(
                                "type", "string",
                                "description", "Preferred learning approach"
                        )
                ),
                "required", List.of(
                        "numberOfDays",
                        "hoursPerDay",
                        "learningPreference"
                ),
                "additionalProperties", false
        );

        LLMFunctionDefinition function = new LLMFunctionDefinition(
                "create_study_plan",
                "Create a study plan using the uploaded PDF content.",
                parameters,
                true
        );

        return new LLMTool("function", function);
    }

    @Override
    public Object execute(
            String arguments,
            Long documentId,
            Long userId
    ) {
        try {
            CreateStudyPlanArguments input =
                    objectMapper.readValue(
                            arguments,
                            CreateStudyPlanArguments.class
                    );

            validateArguments(input);

            Document document =
                    documentService.getOwnedDocument(documentId, userId);

            if (document.getStatus() != Status.READY) {
                throw new DocumentNotReadyException(
                        "Document is not ready"
                );
            }

            List<DocumentChunk> chunks =
                    chunkRepository.findByDocumentOrderByChunkIndex(document);

            if (chunks.isEmpty()) {
                throw new IllegalStateException(
                        "No document content available"
                );
            }

            String context = chunks.stream()
                    .map(DocumentChunk::getContent)
                    .collect(Collectors.joining("\n\n"));

            String prompt = buildPrompt(input);

            String response = llmService.generateAnswer(
                    prompt,
                    context,
                    List.of()
            );

            StudyPlanResponse plan = objectMapper.readValue(
                    response,
                    StudyPlanResponse.class
            );

            validator.validate(
                    plan,
                    input.numberOfDays(),
                    input.hoursPerDay()
            );

            return Map.of(
                    "documentId", documentId,
                    "studyPlan", plan
            );

        } catch (Exception e) {

            if (e instanceof IllegalArgumentException
                    || e instanceof DocumentNotReadyException) {
                throw e;
            }

            throw new IllegalStateException(
                    "Failed to create study plan",
                    e
            );
        }
    }

    private void validateArguments(CreateStudyPlanArguments input) {

        if (input.numberOfDays() == null
                || input.numberOfDays() < 1
                || input.numberOfDays() > 30) {
            throw new IllegalArgumentException(
                    "Number of days must be between 1 and 30"
            );
        }

        if (input.hoursPerDay() == null
                || input.hoursPerDay() < 0.5
                || input.hoursPerDay() > 12) {
            throw new IllegalArgumentException(
                    "Hours per day must be between 0.5 and 12"
            );
        }

        if (input.learningPreference() == null
                || input.learningPreference().isBlank()) {
            throw new IllegalArgumentException(
                    "Learning preference cannot be empty"
            );
        }
    }

    private String buildPrompt(CreateStudyPlanArguments input) {

        return """
                Create a study plan using the provided document.

                Duration: %d days.
                Available study time: %.1f hours per day.
                Learning preference: %s.

                Divide the document into logical topics.
                Distribute the topics across the requested days.
                Make the plan progressive, starting with fundamentals.

                Return valid JSON only, without Markdown fences.

                Use exactly this structure:

                {
                  "title": "Study plan title",
                  "totalDays": %d,
                  "dailyPlans": [
                    {
                      "day": 1,
                      "topic": "Topic name",
                      "activities": [
                        "Activity 1",
                        "Activity 2"
                      ],
                      "estimatedHours": 2.0,
                      "goal": "Learning goal"
                    }
                  ]
                }

                Requirements:
                - Generate exactly the requested number of days.
                - Never exceed the daily time budget.
                - Cover the important topics in the document.
                - Do not invent topics unrelated to the document.
                - Include practical revision activities where appropriate.
                """.formatted(
                input.numberOfDays(),
                input.hoursPerDay(),
                input.learningPreference(),
                input.numberOfDays()
        );
    }
}
