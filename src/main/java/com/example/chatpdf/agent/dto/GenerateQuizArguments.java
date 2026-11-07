package com.example.chatpdf.agent.dto;

public record GenerateQuizArguments(
        Integer numberOfQuestions,
        String difficulty,
        String questionType
) {
}
