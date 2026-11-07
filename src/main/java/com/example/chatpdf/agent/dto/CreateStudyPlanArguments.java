package com.example.chatpdf.agent.dto;

public record CreateStudyPlanArguments(
        Integer numberOfDays,
        Double hoursPerDay,
        String learningPreference
) {
}