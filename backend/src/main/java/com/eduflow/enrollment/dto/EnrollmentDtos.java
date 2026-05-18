package com.eduflow.enrollment.dto;

import com.eduflow.enrollment.enums.EnrollmentStatus;
import lombok.*;

import java.time.LocalDateTime;

public class EnrollmentDtos {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class EnrollWithCodeRequest {
        private String accessCode;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EnrollmentResponse {
        private Long id;
        private Long courseId;
        private String courseTitle;
        private String studentName;
        private EnrollmentStatus status;
        private String accessMethod;
        private LocalDateTime enrolledAt;
    }
}