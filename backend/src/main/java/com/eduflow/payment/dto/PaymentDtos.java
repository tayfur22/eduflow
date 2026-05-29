package com.eduflow.payment.dto;

import com.eduflow.payment.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

public class PaymentDtos {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreatePaymentRequest {
        private Long courseId;
        private Double amount;
        private String currency;
    }

    // ── YENİ: Frontend Stripe.js üçün clientSecret lazımdır ──
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreatePaymentIntentResponse {
        private String clientSecret;       // Stripe.js-ə verilir
        private String paymentIntentId;    // İzləmək üçün
        private Double amount;
        private String currency;
        private String courseTitle;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentResponse {
        private Long id;
        private Long courseId;
        private String courseTitle;
        private Double amount;
        private String currency;
        private PaymentStatus status;
        private String stripePaymentIntentId;
        private LocalDateTime createdAt;
    }
}
