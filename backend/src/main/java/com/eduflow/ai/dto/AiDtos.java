package com.eduflow.ai.dto;

import lombok.*;
import java.util.List;

public class AiDtos {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TutorRequest {
        private Long lessonId;    // Hansı dərslə bağlıdır (optional)
        private String question;  // Şagirdin sualı
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TutorResponse {
        private String answer;
        private String lessonTitle; // null ola bilər
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class QuizGenerateRequest {
        private String topic;          // "Java OOP"
        private int questionCount;     // 5, 10
        private String difficulty;     // "EASY", "MEDIUM", "HARD"
        private Long courseId;         // Hansı kursla bağlıdır
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class GeneratedQuestion {
        private String questionText;
        private List<String> options;   // A, B, C, D
        private String correctAnswer;
        private String explanation;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class QuizGenerateResponse {
        private String topic;
        private List<GeneratedQuestion> questions;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class SummaryRequest {
        private Long lessonId;   // Lesson-un description/content-i summary edilir
        private String text;     // Ya da birbaşa text göndərilir
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SummaryResponse {
        private String summary;
        private List<String> keyPoints;
    }
}