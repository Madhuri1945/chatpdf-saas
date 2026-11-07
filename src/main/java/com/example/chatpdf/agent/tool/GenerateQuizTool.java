package com.example.chatpdf.agent.tool;

import com.example.chatpdf.agent.dto.GenerateQuizArguments;
import com.example.chatpdf.agent.dto.LLMFunctionDefinition;
import com.example.chatpdf.agent.dto.LLMTool;
import com.example.chatpdf.agent.dto.QuizResponse;
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

public class GenerateQuizTool implements AgentTool{

    private final ObjectMapper objectMapper;
    private final DocumentService documentService;
    private final DocumentChunkRepository documentChunkRepository;
    private final LLMService llmService;
    private final QuizValidator quizValidator;

    public GenerateQuizTool(ObjectMapper objectMapper, DocumentService documentService, DocumentChunkRepository documentChunkRepository, LLMService llmService, QuizValidator quizValidator) {
        this.objectMapper = objectMapper;
        this.documentService = documentService;
        this.documentChunkRepository = documentChunkRepository;
        this.llmService = llmService;
        this.quizValidator = quizValidator;
    }

    @Override
    public String getName() {
        return "generate_quiz";
    }

    @Override
    public String getDescription() {
        return "Generate a quiz based on the content of the uploaded PDF.";
    }

    @Override
    public LLMTool getDefinition() {
        Map<String,Object> numberProperty=Map.of(
                "type", "integer",
                "description", "Number of questions to generate (1 to 20).",
                "minimum", 1,
                "maximum", 20
        );
        Map<String, Object> difficultyProperty = Map.of(
                "type", "string",
                "description", "Quiz difficulty.",
                "enum", List.of("easy", "medium", "hard")
        );
        Map<String, Object> questionTypeProperty = Map.of(
                "type", "string",
                "description", "Type of quiz questions.",
                "enum", List.of("multiple_choice", "true_false")
        );
        Map<String,Object> parameters=Map.of(
                "type","object",
                "properties", Map.of(
                        "numberOfQuestions", numberProperty,
                        "difficulty", difficultyProperty,
                        "questionType", questionTypeProperty
                ),
                "required", List.of(
                        "numberOfQuestions",
                        "difficulty",
                        "questionType"
                ),
                "additionalProperties", false
        );
        LLMFunctionDefinition function = new LLMFunctionDefinition(
                "generate_quiz",
                "Generate quiz questions from the user's uploaded PDF.",
                parameters,
                true
        );
        return new LLMTool("function", function);
    }

    @Override
    public Object execute(String arguments, Long documentId, Long userId) {
       try{
           GenerateQuizArguments input = objectMapper.readValue(
                   arguments,
                   GenerateQuizArguments.class
           );
           validateArguments(input);
           Document document =
                   documentService.getOwnedDocument(documentId, userId);
           if (document.getStatus() != Status.READY) {
               throw new DocumentNotReadyException(
                       "Document is not ready for quiz generation"
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
           String prompt = buildPrompt(input);
           String quizJson = llmService.generateAnswer(
                   prompt,
                   context,
                   List.of()
           );
           QuizResponse quiz;
           try {
               quiz = objectMapper.readValue(
                       quizJson,
                       QuizResponse.class
               );
           } catch (Exception e) {
               throw new IllegalStateException(
                       "LLM returned invalid quiz JSON",
                       e
               );
           }
           quizValidator.validate(
                   quiz,
                   input.numberOfQuestions(),
                   input.questionType()
           );

           return Map.of(
                   "documentId", documentId,
                   "numberOfQuestions", input.numberOfQuestions(),
                   "difficulty", input.difficulty(),
                   "questionType", input.questionType(),
                   "quiz", quiz
           );
       }catch(Exception e){
           if (e instanceof IllegalArgumentException
                   || e instanceof DocumentNotReadyException) {
               throw e;
           }

           throw new IllegalStateException(
                   "Failed to generate quiz",
                   e
           );
       }
    }
    private String buildPrompt(GenerateQuizArguments input) {

        String formatInstructions;

        if ("multiple_choice".equals(input.questionType())) {
            formatInstructions = """
                    Each question must have exactly four options:
                    A, B, C, and D.

                    Include the correct answer and a short explanation
                    for every question.
                    """;
        } else {
            formatInstructions = """
                    Each question must have a True or False answer.

                    Include the correct answer and a short explanation
                    for every question.
                    """;
        }

        return """
                Generate exactly %d quiz questions from the provided
                document content.

                Difficulty: %s

                Question type: %s

                %s

                Return the result in JSON format using this structure:

                {
                  "title": "Quiz title",
                  "questions": [
                    {
                      "question": "Question text",
                      "options": ["A", "B", "C", "D"],
                      "correctAnswer": "A",
                      "explanation": "Explanation"
                    }
                  ]
                }

                For true/false questions, options should contain
                ["True", "False"], and correctAnswer must be
                either "True" or "False".

                Important:
                - Generate questions only from the provided document.
                - Do not invent facts or answers.
                - Avoid duplicate questions.
                - Return valid JSON without Markdown code fences.
                - Generate exactly the requested number of questions.
                """.formatted(
                input.numberOfQuestions(),
                input.difficulty(),
                input.questionType(),
                formatInstructions
        );
    }
    private void validateArguments(GenerateQuizArguments input) {

        if (input.numberOfQuestions() == null
                || input.numberOfQuestions() < 1
                || input.numberOfQuestions() > 20) {
            throw new IllegalArgumentException(
                    "Number of questions must be between 1 and 20"
            );
        }

        if (input.difficulty() == null
                || !List.of("easy", "medium", "hard")
                .contains(input.difficulty())) {
            throw new IllegalArgumentException(
                    "Difficulty must be easy, medium, or hard"
            );
        }

        if (input.questionType() == null
                || !List.of("multiple_choice", "true_false")
                .contains(input.questionType())) {
            throw new IllegalArgumentException(
                    "Question type must be multiple_choice or true_false"
            );
        }
    }
}
