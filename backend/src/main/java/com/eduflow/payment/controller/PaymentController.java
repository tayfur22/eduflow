package com.eduflow.payment.controller;

import com.eduflow.payment.dto.PaymentDtos.*;
import com.eduflow.payment.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ── YENİ: Stripe PaymentIntent yarat ─────────────────
    // POST /api/payments/create-intent
    @PostMapping("/create-intent")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CreatePaymentIntentResponse> createIntent(
            @RequestParam Long courseId) throws StripeException {
        return ResponseEntity.ok(paymentService.createPaymentIntent(courseId));
    }

    // ── YENİ: Stripe Webhook ──────────────────────────────
    // POST /api/payments/webhook  (SecurityConfig-da permitAll lazımdır!)
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }

    // Öz ödənişlərini görür
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<PaymentResponse>> getMyPayments() {
        return ResponseEntity.ok(paymentService.getMyPayments());
    }

    // Teacher kursunun ödənişlərini görür
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<PaymentResponse>> getCoursePayments(@PathVariable Long courseId) {
        return ResponseEntity.ok(paymentService.getCoursePayments(courseId));
    }
}
