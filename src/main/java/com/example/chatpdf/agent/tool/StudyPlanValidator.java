package com.example.chatpdf.agent.tool;

import com.example.chatpdf.agent.dto.StudyDay;
import com.example.chatpdf.agent.dto.StudyPlanResponse;
import org.springframework.stereotype.Component;

@Component
public class StudyPlanValidator {

    public void validate(
            StudyPlanResponse plan,
            int expectedDays,
            double hoursPerDay
    ) {

        if (plan == null) {
            throw new IllegalArgumentException(
                    "Study plan cannot be null"
            );
        }

        if (plan.title() == null || plan.title().isBlank()) {
            throw new IllegalArgumentException(
                    "Study plan title cannot be empty"
            );
        }

        if (plan.totalDays() == null
                || plan.totalDays() != expectedDays) {
            throw new IllegalArgumentException(
                    "Incorrect total number of days"
            );
        }

        if (plan.dailyPlans() == null
                || plan.dailyPlans().size() != expectedDays) {
            throw new IllegalArgumentException(
                    "Study plan must contain exactly "
                            + expectedDays + " daily plans"
            );
        }

        for (int i = 0; i < plan.dailyPlans().size(); i++) {

            StudyDay day = plan.dailyPlans().get(i);

            if (day == null) {
                throw new IllegalArgumentException(
                        "Day plan cannot be null"
                );
            }

            if (day.day() == null || day.day() != i + 1) {
                throw new IllegalArgumentException(
                        "Study days must be sequential"
                );
            }

            if (day.topic() == null || day.topic().isBlank()) {
                throw new IllegalArgumentException(
                        "Topic cannot be empty"
                );
            }

            if (day.activities() == null
                    || day.activities().isEmpty()
                    || day.activities().stream()
                    .anyMatch(a -> a == null || a.isBlank())) {
                throw new IllegalArgumentException(
                        "Each day must contain valid activities"
                );
            }

            if (day.goal() == null || day.goal().isBlank()) {
                throw new IllegalArgumentException(
                        "Each day must have a learning goal"
                );
            }

            if (day.estimatedHours() == null
                    || day.estimatedHours() <= 0
                    || day.estimatedHours() > hoursPerDay) {
                throw new IllegalArgumentException(
                        "Invalid estimated study hours for day "
                                + day.day()
                );
            }
        }
    }
}