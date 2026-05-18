package com.eduflow.certificate.dto;

import lombok.*;

import java.time.LocalDateTime;

public class CertificateDtos {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CertificateResponse {
        private Long id;
        private String certificateNumber;
        private String studentName;
        private String courseTitle;
        private String teacherName;       // əlavə — sertifikatda müəllim adı da görünsün
        private LocalDateTime issuedAt;
    }
}
