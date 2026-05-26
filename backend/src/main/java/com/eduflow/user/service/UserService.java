package com.eduflow.user.service;

import com.eduflow.user.dto.UserDtos;
import com.eduflow.user.entity.User;
import com.eduflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDtos.ProfileResponse getProfile(String email) {
        User user = getUser(email);
        return toProfile(user);
    }

    @Transactional
    public UserDtos.ProfileResponse updateProfile(String email, UserDtos.UpdateProfileRequest req) {
        User user = getUser(email);

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            user.setFullName(req.getFullName().trim());
        }
        if (req.getBio() != null) {
            user.setBio(req.getBio().trim());
        }
        if (req.getProfileImage() != null && !req.getProfileImage().isBlank()) {
            user.setProfileImage(req.getProfileImage().trim());
        }

        return toProfile(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String email, UserDtos.ChangePasswordRequest req) {
        User user = getUser(email);

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Cari şifrə yanlışdır");
        }
        if (req.getNewPassword().length() < 6) {
            throw new RuntimeException("Yeni şifrə ən az 6 simvol olmalıdır");
        }
        if (req.getNewPassword().equals(req.getCurrentPassword())) {
            throw new RuntimeException("Yeni şifrə köhnə şifrə ilə eyni ola bilməz");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));
    }

    private UserDtos.ProfileResponse toProfile(User user) {
        return UserDtos.ProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .bio(user.getBio())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();
    }
}
