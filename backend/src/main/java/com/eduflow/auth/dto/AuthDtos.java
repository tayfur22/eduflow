package com.eduflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.eduflow.user.enums.Role;

public class AuthDtos {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Ad-soyad boş ola bilməz")
        private String fullName;

        @Email(message = "Düzgün email daxil edin")
        @NotBlank(message = "Email boş ola bilməz")
        private String email;

        @NotBlank(message = "Şifrə boş ola bilməz")
        @Size(min = 6, message = "Şifrə ən az 6 simvol olmalıdır")
        private String password;

        private Role role;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @Email @NotBlank(message = "Email boş ola bilməz")
        private String email;

        @NotBlank(message = "Şifrə boş ola bilməz")
        private String password;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String refreshToken;
        private String email;
        private String fullName;
        private String role;
        private Long userId;
        private String profileImage;
        private String bio;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RefreshRequest {
        @NotBlank(message = "Refresh token boş ola bilməz")
        private String refreshToken;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RefreshResponse {
        private String token;
        private String refreshToken;
    }

    // ── YENİ ──────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ForgotPasswordRequest {
        @Email(message = "Düzgün email daxil edin")
        private String email;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank(message = "Token boş ola bilməz")
        private String token;

        @NotBlank(message = "Yeni şifrə boş ola bilməz")
        @Size(min = 6, message = "Şifrə ən az 6 simvol olmalıdır")
        private String newPassword;
    }
}
