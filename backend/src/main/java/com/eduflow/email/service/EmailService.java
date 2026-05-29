package com.eduflow.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    // ── Salamlama emaili ──────────────────────────────────
    @Async
    public void sendWelcomeEmail(String toEmail, String fullName, String role, String dashboardUrl) {
        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "fullName", fullName,
                "email", toEmail,
                "role", role,
                "dashboardUrl", dashboardUrl
        ));
        send(toEmail, "EduFlow-a xoş gəldiniz! 🎓", "welcome", ctx);
    }

    // ── Şifrə sıfırlama emaili ────────────────────────────
    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName,
                                       String resetUrl, int expiryMinutes) {
        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "fullName", fullName,
                "email", toEmail,
                "resetUrl", resetUrl,
                "expiryMinutes", expiryMinutes
        ));
        send(toEmail, "EduFlow — Şifrə Sıfırlama 🔐", "password-reset", ctx);
    }

    // ── Ödəniş qəbzi emaili ───────────────────────────────
    @Async
    public void sendPaymentReceiptEmail(String toEmail, String studentName,
                                        String courseTitle, String teacherName,
                                        Double amount, String currency,
                                        String transactionId, String purchaseDate,
                                        String courseUrl) {
        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "studentName", studentName,
                "courseTitle", courseTitle,
                "teacherName", teacherName,
                "amount", String.format("%.2f", amount),
                "currency", currency,
                "transactionId", transactionId,
                "purchaseDate", purchaseDate,
                "courseUrl", courseUrl
        ));
        send(toEmail, "✅ Ödəniş qəbzi — " + courseTitle, "payment-receipt", ctx);
    }

    // ── Sertifikat hazır emaili ───────────────────────────
    @Async
    public void sendCertificateEmail(String toEmail, String fullName,
                                     String courseTitle, String certificateUrl) {
        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "fullName", fullName,
                "courseTitle", courseTitle,
                "certificateUrl", certificateUrl
        ));
        send(toEmail, "🏆 Sertifikatınız hazırdır! — " + courseTitle, "certificate-ready", ctx);
    }

    // ── Ümumi göndərici ───────────────────────────────────
    private void send(String to, String subject, String template, Context ctx) {
        try {
            String html = templateEngine.process("emails/" + template, ctx);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email göndərildi: {} → {}", template, to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Email göndərilmədi: {} → {} | {}", template, to, e.getMessage());
        }
    }
}
