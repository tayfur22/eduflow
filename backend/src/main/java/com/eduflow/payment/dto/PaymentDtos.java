package com.eduflow.payment.dto;

import com.eduflow.payment.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

public class PaymentDtos {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreatePaymentRequest {
        private Long courseId;
        private Double amount;
        private String currency; // "AZN", "USD"
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentResponse {
        private Long id;
        private Long courseId;
        private String courseTitle;
        private Double amount;
        private String currency;
        private PaymentStatus status;
        private LocalDateTime createdAt;
    }
}