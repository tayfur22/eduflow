package com.eduflow.auth;

import com.eduflow.auth.dto.AuthDtos;
import com.eduflow.email.service.EmailService;
import com.eduflow.user.enums.Role;
import com.eduflow.user.entity.User;
import com.eduflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.password-reset.expiry-minutes:30}")
    private int passwordResetExpiryMinutes;

    // ── Qeydiyyat ──────────────────────────────────────────
    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu email artıq qeydiyyatdan keçib: " + request.getEmail());
        }

        Role role = request.getRole() != null ? request.getRole() : Role.STUDENT;

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        // Salamlama emaili göndər (async — gözləmir)
        String dashboardUrl = frontendUrl + (role == Role.TEACHER
                ? "/dashboard/teacher"
                : "/dashboard/student");
        emailService.sendWelcomeEmail(
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole().name(),
                dashboardUrl
        );

        return buildFullAuthResponse(savedUser);
    }

    // ── Giriş ──────────────────────────────────────────────
    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));

        refreshTokenRepository.revokeAllByUserId(user.getId());
        return buildFullAuthResponse(user);
    }

    // ── Refresh token ──────────────────────────────────────
    @Transactional
    public AuthDtos.RefreshResponse refresh(AuthDtos.RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token tapılmadı"));

        if (!stored.isValid()) {
            refreshTokenRepository.revokeAllByUserId(stored.getUser().getId());
            throw new RuntimeException("Refresh token etibarsızdır. Yenidən daxil olun.");
        }

        User user = stored.getUser();
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String newAccess = jwtService.generateToken(
                (org.springframework.security.core.userdetails.UserDetails)
                        userDetailsService.loadUserByUsername(user.getEmail()));
        String newRefresh = jwtService.generateRefreshTokenValue();
        saveRefreshToken(newRefresh, user);

        return AuthDtos.RefreshResponse.builder()
                .token(newAccess)
                .refreshToken(newRefresh)
                .build();
    }

    // ── Çıxış ──────────────────────────────────────────────
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    // ── Şifrə sıfırlama tələbi ────────────────────────────
    @Transactional
    public void forgotPassword(String email) {
        // Emailin mövcud olub-olmadığını açıqlamırıq (təhlükəsizlik)
        userRepository.findByEmail(email).ifPresent(user -> {
            // Köhnə tokenləri sil
            passwordResetTokenRepository.deleteAllByUserId(user.getId());

            // Yeni token yarat
            String tokenValue = UUID.randomUUID().toString().replace("-", "")
                    + UUID.randomUUID().toString().replace("-", "");

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(tokenValue)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(passwordResetExpiryMinutes))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            String resetUrl = frontendUrl + "/reset-password?token=" + tokenValue;
            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getFullName(),
                    resetUrl,
                    passwordResetExpiryMinutes
            );
        });
    }

    // ── Şifrəni sıfırla ───────────────────────────────────
    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new RuntimeException("Token tapılmadı"));

        if (!resetToken.isValid()) {
            throw new RuntimeException("Token etibarsız və ya müddəti keçib");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Şifrə ən az 6 simvol olmalıdır");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Tokeni işlənmiş kimi işarələ
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Bütün refresh tokenləri ləğv et (yeni şifrə → yeni giriş lazım)
        refreshTokenRepository.revokeAllByUserId(user.getId());

        log.info("Şifrə sıfırlandı: {}", user.getEmail());
    }

    // ── Köməkçi metodlar ───────────────────────────────────
    private AuthDtos.AuthResponse buildFullAuthResponse(User user) {
        String accessToken = jwtService.generateToken(
                (org.springframework.security.core.userdetails.UserDetails)
                        userDetailsService.loadUserByUsername(user.getEmail()));
        String refreshValue = jwtService.generateRefreshTokenValue();
        saveRefreshToken(refreshValue, user);

        return AuthDtos.AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshValue)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .userId(user.getId())
                .profileImage(user.getProfileImage())
                .bio(user.getBio())
                .build();
    }

    private void saveRefreshToken(String value, User user) {
        RefreshToken rt = RefreshToken.builder()
                .token(value)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);
    }
}
