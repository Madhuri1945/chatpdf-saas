package com.example.chatpdf.agent.dto;

import java.util.List;

public record QuizResponse(
        String title,
        List<QuizQuestion> questions
) {
}