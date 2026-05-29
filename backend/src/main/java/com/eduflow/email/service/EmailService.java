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

    @Value("${app.mail.from:noreply@eduflow.az}")
    private String fromEmail;

    @Value("${app.mail.from-name:EduFlow}")
    private String fromName;

    // ── application.properties-dən default ilə oxu ───────
    // Bu xətt əvvəlki versiyada crash edirdi:
    //   @Value("${spring.mail.username:placeholder@gmail.com}")
    // Çünki Spring ${MAIL_USERNAME} → placeholder tapmırdı.
    // İndi application.properties-də default var, bu field silinir.
    private boolean isMailConfigured() {
        // Əgər application.properties düzgün set edilibsə,
        // fromEmail "placeholder" deyil, real email olacaq.
        return fromEmail != null
                && !fromEmail.isBlank()
                && !fromEmail.equals("placeholder@gmail.com");
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName,
                                  String role, String dashboardUrl) {
        if (!isMailConfigured()) {
            log.warn("Email konfiqurasiyası yoxdur — salamlama emaili göndərilmədi: {}", toEmail);
            return;
        }
        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "fullName", fullName, "email", toEmail,
                "role", role, "dashboardUrl", dashboardUrl));
        send(toEmail, "EduFlow-a xoş gəldiniz! 🎓", "welcome", ctx);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName,
                                        String resetUrl, int expiryMinutes) {
        if (!isMailConfigured()) {
            log.warn("Email konfiqurasiyası yoxdur — şifrə sıfırlama linki: {}", resetUrl);
            return;
        }
        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "fullName", fullName, "email", toEmail,
                "resetUrl", resetUrl, "expiryMinutes", expiryMinutes));
        send(toEmail, "EduFlow — Şifrə Sıfırlama 🔐", "password-reset", ctx);
    }

    @Async
    public void sendPaymentReceiptEmail(String toEmail, String studentName,
                                         String courseTitle, String teacherName,
                                         Double amount, String currency,
                                         String transactionId, String purchaseDate,
                                         String courseUrl) {
        if (!isMailConfigured()) {
            log.warn("Email konfiqurasiyası yoxdur — qəbz emaili göndərilmədi: {}", toEmail);
            return;
        }
        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "studentName", studentName, "courseTitle", courseTitle,
                "teacherName", teacherName,
                "amount", String.format("%.2f", amount),
                "currency", currency, "transactionId", transactionId,
                "purchaseDate", purchaseDate, "courseUrl", courseUrl));
        send(toEmail, "✅ Ödəniş qəbzi — " + courseTitle, "payment-receipt", ctx);
    }

    @Async
    public void sendCertificateEmail(String toEmail, String fullName,
                                      String courseTitle, String certificateUrl) {
        if (!isMailConfigured()) {
            log.warn("Email konfiqurasiyası yoxdur — sertifikat emaili göndərilmədi: {}", toEmail);
            return;
        }
        Context ctx = new Context();
        ctx.setVariables(Map.of(
                "fullName", fullName, "courseTitle", courseTitle,
                "certificateUrl", certificateUrl));
        send(toEmail, "🏆 Sertifikatınız hazırdır! — " + courseTitle,
                "certificate-ready", ctx);
    }

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
            log.info("✅ Email göndərildi: {} → {}", template, to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("❌ Email göndərilmədi: {} → {} | {}", template, to, e.getMessage());
        } catch (Exception e) {
            log.error("❌ Gözlənilməz email xətası: {} | {}", template, e.getMessage());
        }
    }
}
