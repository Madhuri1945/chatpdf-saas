package com.example.chatpdf.agent.tool;

import com.example.chatpdf.agent.dto.QuizQuestion;
import com.example.chatpdf.agent.dto.QuizResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuizValidator {

    public void validate(
            QuizResponse quiz,
            int expectedQuestions,
            String questionType
    ) {

        if (quiz == null) {
            throw new IllegalArgumentException(
                    "Quiz response cannot be null"
            );
        }

        if (quiz.title() == null || quiz.title().isBlank()) {
            throw new IllegalArgumentException(
                    "Quiz title cannot be empty"
            );
        }

        List<QuizQuestion> questions = quiz.questions();

        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Quiz must contain questions"
            );
        }

        if (questions.size() != expectedQuestions) {
            throw new IllegalArgumentException(
                    "Expected " + expectedQuestions
                            + " questions but received "
                            + questions.size()
            );
        }

        for (int i = 0; i < questions.size(); i++) {

            QuizQuestion question = questions.get(i);

            validateQuestion(question, i + 1, questionType);
        }
    }

    private void validateQuestion(
            QuizQuestion question,
            int questionNumber,
            String questionType
    ) {

        if (question == null) {
            throw new IllegalArgumentException(
                    "Question " + questionNumber + " is null"
            );
        }

        if (question.question() == null
                || question.question().isBlank()) {
            throw new IllegalArgumentException(
                    "Question " + questionNumber + " has no text"
            );
        }

        if (question.explanation() == null
                || question.explanation().isBlank()) {
            throw new IllegalArgumentException(
                    "Question " + questionNumber
                            + " has no explanation"
            );
        }

        List<String> options = question.options();

        if (options == null) {
            throw new IllegalArgumentException(
                    "Question " + questionNumber
                            + " has no options"
            );
        }

        for (String option : options) {
            if (option == null || option.isBlank()) {
                throw new IllegalArgumentException(
                        "Question " + questionNumber
                                + " contains an empty option"
                );
            }
        }

        if ("multiple_choice".equals(questionType)) {

            if (options.size() != 4) {
                throw new IllegalArgumentException(
                        "Question " + questionNumber
                                + " must have exactly four options"
                );
            }

            if (!List.of("A", "B", "C", "D")
                    .contains(question.correctAnswer())) {
                throw new IllegalArgumentException(
                        "Question " + questionNumber
                                + " has an invalid correct answer"
                );
            }

        } else if ("true_false".equals(questionType)) {

            if (options.size() != 2
                    || !options.equals(List.of("True", "False"))) {
                throw new IllegalArgumentException(
                        "Question " + questionNumber
                                + " must have True and False options"
                );
            }

            if (!List.of("True", "False")
                    .contains(question.correctAnswer())) {
                throw new IllegalArgumentException(
                        "Question " + questionNumber
                                + " must have True or False as its answer"
                );
            }
        }
    }
}