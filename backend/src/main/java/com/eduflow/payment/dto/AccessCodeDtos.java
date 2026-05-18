package com.eduflow.payment.dto;

import lombok.*;
import java.time.LocalDateTime;

public class AccessCodeDtos {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateAccessCodeRequest {
        private Long courseId;
        private String code;           // "JAVA2024"
        private Integer maxUsages;     // null = limitsiz
        private LocalDateTime expiresAt; // null = limitsiz
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AccessCodeResponse {
        private Long id;
        private String code;
        private Long courseId;
        private String courseTitle;
        private Integer maxUsages;
        private Integer currentUsages;
        private LocalDateTime expiresAt;
        private boolean active;
        private LocalDateTime createdAt;
    }
}