package com.eduflow.progress.dto;

import lombok.*;
import java.time.LocalDateTime;

public class ProgressDtos {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProgressResponse {
        private Long lessonId;
        private String lessonTitle;
        private boolean completed;
        private LocalDateTime completedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CourseProgressResponse {
        private Long courseId;
        private String courseTitle;
        private int totalLessons;
        private int completedLessons;
        private double percentage;
        private boolean certificateEarned;
    }
}