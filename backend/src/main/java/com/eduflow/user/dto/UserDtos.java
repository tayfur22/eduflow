package com.eduflow.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class UserDtos {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProfileResponse {
        private Long id;
        private String fullName;
        private String email;
        private String role;
        private String bio;
        private String profileImage;
        private String createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateProfileRequest {
        @Size(min = 2, max = 100, message = "Ad 2-100 simvol arasında olmalıdır")
        private String fullName;

        @Size(max = 500, message = "Bio 500 simvoldan çox ola bilməz")
        private String bio;

        private String profileImage;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotBlank(message = "Cari şifrə boş ola bilməz")
        private String currentPassword;

        @NotBlank(message = "Yeni şifrə boş ola bilməz")
        @Size(min = 6, message = "Yeni şifrə ən az 6 simvol olmalıdır")
        private String newPassword;
    }
}
