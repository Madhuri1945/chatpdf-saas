package com.example.chatpdf.agent.dto;

import java.util.List;

public record StudyPlanResponse(
        String title,
        Integer totalDays,
        List<StudyDay> dailyPlans
) {
}