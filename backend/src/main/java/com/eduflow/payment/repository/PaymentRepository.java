package com.eduflow.payment.repository;

import com.eduflow.payment.entity.Payment;
import com.eduflow.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByStudentId(Long studentId);

    List<Payment> findByCourseId(Long courseId);

    Optional<Payment> findByStudentIdAndCourseIdAndStatus(
            Long studentId, Long courseId, PaymentStatus status);

    // ── YENİ: webhook idempotency üçün ──
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}
