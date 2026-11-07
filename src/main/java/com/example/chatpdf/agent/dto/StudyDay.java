package com.example.chatpdf.agent.dto;

import java.util.List;

public record StudyDay(
        Integer day,
        String topic,
        List<String> activities,
        Double estimatedHours,
        String goal
) {
}