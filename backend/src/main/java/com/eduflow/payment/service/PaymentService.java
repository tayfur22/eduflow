package com.eduflow.payment.service;

import com.eduflow.course.entity.Course;
import com.eduflow.course.repository.CourseRepository;
import com.eduflow.email.service.EmailService;
import com.eduflow.enrollment.entity.Enrollment;
import com.eduflow.enrollment.enums.EnrollmentStatus;
import com.eduflow.enrollment.repository.EnrollmentRepository;
import com.eduflow.payment.dto.PaymentDtos.*;
import com.eduflow.payment.entity.Payment;
import com.eduflow.payment.enums.PaymentStatus;
import com.eduflow.payment.repository.PaymentRepository;
import com.eduflow.user.entity.User;
import com.eduflow.user.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EmailService emailService;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeSecretKey;
    }

    // ── 1. PaymentIntent yarat (frontend Stripe.js üçün) ──
    @Transactional
    public CreatePaymentIntentResponse createPaymentIntent(Long courseId) throws StripeException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User student = getUser(email);
        Course course = getCourse(courseId);

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new RuntimeException("Bu kursa artıq qeydiyyatlısınız");
        }

        if (course.getPrice() == null || course.getPrice() <= 0) {
            throw new RuntimeException("Bu kurs ödənişli deyil");
        }

        // Qiymət qəpik (cent) olaraq göndərilir
        long amountInCents = Math.round(course.getPrice() * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(course.getCurrency() != null
                        ? course.getCurrency().toLowerCase() : "azn")
                .setDescription("EduFlow: " + course.getTitle())
                .putMetadata("courseId", String.valueOf(courseId))
                .putMetadata("studentId", String.valueOf(student.getId()))
                .putMetadata("studentEmail", student.getEmail())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true).build()
                )
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        return CreatePaymentIntentResponse.builder()
                .clientSecret(intent.getClientSecret())
                .paymentIntentId(intent.getId())
                .amount(course.getPrice())
                .currency(course.getCurrency() != null ? course.getCurrency() : "AZN")
                .courseTitle(course.getTitle())
                .build();
    }

    // ── 2. Stripe Webhook ─────────────────────────────────
    // Stripe Dashboard-da: https://yourapp.com/api/payments/webhook
    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Webhook imzası yanlışdır");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject().orElseThrow();
            processSuccessfulPayment(intent);
        } else if ("payment_intent.payment_failed".equals(event.getType())) {
            PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject().orElseThrow();
            processFailedPayment(intent);
        }
    }

    // ── 3. Mənim ödənişlərim ──────────────────────────────
    public List<PaymentResponse> getMyPayments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User student = getUser(email);
        return paymentRepository.findByStudentId(student.getId())
                .stream().map(p -> toResponse(p, p.getCourse()))
                .collect(Collectors.toList());
    }

    // ── 4. Kurs ödənişləri (teacher üçün) ─────────────────
    public List<PaymentResponse> getCoursePayments(Long courseId) {
        return paymentRepository.findByCourseId(courseId)
                .stream().map(p -> toResponse(p, p.getCourse()))
                .collect(Collectors.toList());
    }

    // ── Köməkçi: uğurlu ödənişi emal et ─────────────────
    private void processSuccessfulPayment(PaymentIntent intent) {
        String courseIdStr = intent.getMetadata().get("courseId");
        String studentIdStr = intent.getMetadata().get("studentId");
        String studentEmail = intent.getMetadata().get("studentEmail");

        if (courseIdStr == null || studentIdStr == null) return;

        Long courseId = Long.parseLong(courseIdStr);
        Long studentId = Long.parseLong(studentIdStr);

        // Artıq emal edilibsə keç
        if (paymentRepository.findByStripePaymentIntentId(intent.getId()).isPresent()) {
            return;
        }

        Course course = courseRepository.findById(courseId).orElse(null);
        User student = userRepository.findById(studentId).orElse(null);
        if (course == null || student == null) return;

        // Payment yarat
        Payment payment = Payment.builder()
                .student(student)
                .course(course)
                .amount(intent.getAmount() / 100.0)
                .currency(intent.getCurrency().toUpperCase())
                .status(PaymentStatus.COMPLETED)
                .stripePaymentIntentId(intent.getId())
                .build();
        paymentRepository.save(payment);

        // Enrollment yarat
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            Enrollment enrollment = Enrollment.builder()
                    .student(student)
                    .course(course)
                    .status(EnrollmentStatus.ACTIVE)
                    .accessMethod("PAID")
                    .build();
            enrollmentRepository.save(enrollment);
        }

        // Qəbz emaili göndər
        String teacherName = course.getTeacher() != null
                ? course.getTeacher().getFullName() : "EduFlow";
        String purchaseDate = payment.getCreatedAt() != null
                ? payment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                : "";

        emailService.sendPaymentReceiptEmail(
                student.getEmail(),
                student.getFullName(),
                course.getTitle(),
                teacherName,
                payment.getAmount(),
                payment.getCurrency(),
                intent.getId(),
                purchaseDate,
                frontendUrl + "/courses/" + courseId
        );

        log.info("Ödəniş uğurlu: student={} course={} amount={}",
                studentId, courseId, payment.getAmount());
    }

    private void processFailedPayment(PaymentIntent intent) {
        String courseIdStr = intent.getMetadata().get("courseId");
        String studentIdStr = intent.getMetadata().get("studentId");
        if (courseIdStr == null || studentIdStr == null) return;

        Course course = courseRepository.findById(Long.parseLong(courseIdStr)).orElse(null);
        User student = userRepository.findById(Long.parseLong(studentIdStr)).orElse(null);
        if (course == null || student == null) return;

        Payment payment = Payment.builder()
                .student(student)
                .course(course)
                .amount(intent.getAmount() / 100.0)
                .currency(intent.getCurrency().toUpperCase())
                .status(PaymentStatus.FAILED)
                .stripePaymentIntentId(intent.getId())
                .build();
        paymentRepository.save(payment);

        log.warn("Ödəniş uğursuz: student={} course={}", student.getId(), course.getId());
    }

    private PaymentResponse toResponse(Payment payment, Course course) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));
    }

    private Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kurs tapılmadı"));
    }
}
