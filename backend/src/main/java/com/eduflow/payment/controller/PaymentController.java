package com.eduflow.payment.controller;

import com.eduflow.payment.dto.PaymentDtos.*;
import com.eduflow.payment.service.PaymentService;
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

    // Student ödəniş edir
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    // Öz ödənişlərini görür
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<PaymentResponse>> getMyPayments() {
        return ResponseEntity.ok(paymentService.getMyPayments());
    }

    // Teacher kursun ödənişlərini görür
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<PaymentResponse>> getCoursePayments(@PathVariable Long courseId) {
        return ResponseEntity.ok(paymentService.getCoursePayments(courseId));
    }
}