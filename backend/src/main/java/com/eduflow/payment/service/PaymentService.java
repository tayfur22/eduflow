package com.eduflow.payment.service;

import com.eduflow.course.entity.Course;
import com.eduflow.course.repository.CourseRepository;
import com.eduflow.enrollment.entity.Enrollment;
import com.eduflow.enrollment.enums.EnrollmentStatus;
import com.eduflow.enrollment.repository.EnrollmentRepository;
import com.eduflow.payment.dto.PaymentDtos.*;
import com.eduflow.payment.entity.Payment;
import com.eduflow.payment.enums.PaymentStatus;
import com.eduflow.payment.repository.PaymentRepository;
import com.eduflow.user.entity.User;
import com.eduflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Artıq qeydiyyatlıdırsa
        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new RuntimeException("Already enrolled in this course");
        }

        // Payment yarat - real Stripe inteqrasiyasında burada Stripe çağırılır
        Payment payment = Payment.builder()
                .student(student)
                .course(course)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.COMPLETED) // MVP: birbaşa completed
                .stripePaymentIntentId("mock_" + System.currentTimeMillis())
                .build();

        paymentRepository.save(payment);

        // Enrollment yarat
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .accessMethod("PAID")
                .build();
        enrollmentRepository.save(enrollment);

        return toResponse(payment, course);
    }

    public List<PaymentResponse> getMyPayments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return paymentRepository.findByStudentId(student.getId())
                .stream()
                .map(p -> toResponse(p, p.getCourse()))
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getCoursePayments(Long courseId) {
        return paymentRepository.findByCourseId(courseId)
                .stream()
                .map(p -> toResponse(p, p.getCourse()))
                .collect(Collectors.toList());
    }

    private PaymentResponse toResponse(Payment payment, Course course) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}