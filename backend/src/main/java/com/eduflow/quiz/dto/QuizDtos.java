package com.eduflow.quiz.dto;

import com.eduflow.quiz.enums.DifficultyLevel;
import com.eduflow.quiz.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class QuizDtos {

    // ── Quiz yaratmaq ──
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateQuizRequest {
        @NotBlank(message = "Quiz adı boş ola bilməz")
        private String title;
        private String description;
        private Integer timeLimitMinutes;
        private Integer passingScore;
        private boolean shuffleQuestions;
        private Integer maxAttempts;
        private DifficultyLevel difficultyLevel;
    }

    // ── Sual seçimi yaratmaq ──
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class OptionRequest {
        @NotBlank
        private String optionLabel;  // "A", "B", "C", "D"
        @NotBlank
        private String optionText;
    }

    // ── Sual yaratmaq ──
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateQuestionRequest {
        @NotBlank(message = "Sual mətni boş ola bilməz")
        private String questionText;

        @NotNull(message = "Sual tipi seçilməlidir")
        private QuestionType questionType;

        @NotBlank(message = "Düzgün cavab boş ola bilməz")
        private String correctAnswer;

        private String explanation;
        private Integer points;
        private Integer orderIndex;

        // Yalnız MULTIPLE_CHOICE üçün
        private List<OptionRequest> options;
    }

    // ── Quiz submit etmək ──
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class SubmitQuizRequest {
        // questionId → verilən cavab
        @NotNull
        private Map<Long, String> answers;
    }

    // ── Option response ──
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OptionResponse {
        private Long id;
        private String optionLabel;
        private String optionText;
    }

    // ── Question response (şagird üçün - düzgün cavab yoxdur) ──
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuestionResponse {
        private Long id;
        private String questionText;
        private QuestionType questionType;
        private Integer points;
        private Integer orderIndex;
        private List<OptionResponse> options;
    }

    // ── Question response (müəllim üçün - cavabla birlikdə) ──
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuestionDetailResponse {
        private Long id;
        private String questionText;
        private QuestionType questionType;
        private String correctAnswer;
        private String explanation;
        private Integer points;
        private Integer orderIndex;
        private List<OptionResponse> options;
    }

    // ── Quiz response ──
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuizResponse {
        private Long id;
        private String title;
        private String description;
        private Integer timeLimitMinutes;
        private Integer passingScore;
        private boolean shuffleQuestions;
        private Integer maxAttempts;
        private DifficultyLevel difficultyLevel;
        private Long courseId;
        private int questionCount;
        private LocalDateTime createdAt;
    }

    // ── Quiz (suallarla birlikdə, şagird üçün) ──
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuizWithQuestionsResponse {
        private Long id;
        private String title;
        private String description;
        private Integer timeLimitMinutes;
        private Integer passingScore;
        private List<QuestionResponse> questions;
    }

    // ── Submission answer nəticəsi ──
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AnswerResult {
        private Long questionId;
        private String questionText;
        private String givenAnswer;
        private String correctAnswer;
        private String explanation;
        private boolean correct;
        private Integer pointsEarned;
        private Integer maxPoints;
    }

    // ── Submission nəticəsi ──
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SubmissionResponse {
        private Long id;
        private Long quizId;
        private String quizTitle;
        private Integer score;
        private Integer maxScore;
        private Double percentage;
        private boolean passed;
        private Integer attemptNumber;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private List<AnswerResult> answers;
    }
}
