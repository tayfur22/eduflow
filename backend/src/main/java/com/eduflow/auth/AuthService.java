package com.eduflow.auth;

import com.eduflow.auth.dto.AuthDtos;
import com.eduflow.user.enums.Role;
import com.eduflow.user.entity.User;
import com.eduflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDetailsService userDetailsService;

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
        return buildFullAuthResponse(savedUser);
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));

        // Köhnə refresh tokenləri ləğv et
        refreshTokenRepository.revokeAllByUserId(user.getId());

        return buildFullAuthResponse(user);
    }

    @Transactional
    public AuthDtos.RefreshResponse refresh(AuthDtos.RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token tapılmadı"));

        if (!stored.isValid()) {
            // Təhlükəsizlik: bütün tokenləri ləğv et
            refreshTokenRepository.revokeAllByUserId(stored.getUser().getId());
            throw new RuntimeException("Refresh token etibarsızdır. Yenidən daxil olun.");
        }

        User user = stored.getUser();

        // Köhnəni ləğv et
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        // Yenilərini yarat
        String newAccessToken = jwtService.generateToken(
                (org.springframework.security.core.userdetails.UserDetails)
                        userDetailsService.loadUserByUsername(user.getEmail())
        );
        String newRefreshValue = jwtService.generateRefreshTokenValue();
        saveRefreshToken(newRefreshValue, user);

        return AuthDtos.RefreshResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshValue)
                .build();
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    // ── Helper metodlar ──

    private AuthDtos.AuthResponse buildFullAuthResponse(User user) {
        String accessToken = jwtService.generateToken(
                (org.springframework.security.core.userdetails.UserDetails)
                        userDetailsService.loadUserByUsername(user.getEmail())
        );
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
