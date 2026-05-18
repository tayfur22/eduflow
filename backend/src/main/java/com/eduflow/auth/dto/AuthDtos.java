package com.eduflow.auth.dto;

import com.eduflow.user.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ── Register request ──
// Bu class-ı bir fayl içində yazırıq - sadəlik üçün
public class AuthDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {

        @NotBlank(message = "Ad-soyad boş ola bilməz")
        private String fullName;

        @Email(message = "Düzgün email daxil edin")
        @NotBlank(message = "Email boş ola bilməz")
        private String email;

        @NotBlank(message = "Şifrə boş ola bilməz")
        @Size(min = 6, message = "Şifrə ən az 6 simvol olmalıdır")
        private String password;

        // STUDENT və ya TEACHER seçəcək
        private Role role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {

        @Email
        @NotBlank(message = "Email boş ola bilməz")
        private String email;

        @NotBlank(message = "Şifrə boş ola bilməz")
        private String password;
    }

    // Hər iki əməliyyat eyni response qaytarır
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String email;
        private String fullName;
        private String role;
        private Long userId;
    }
}
