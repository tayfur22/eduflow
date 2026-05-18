package com.eduflow.auth;

import com.eduflow.auth.dto.AuthDtos;
import com.eduflow.user.enums.Role;
import com.eduflow.user.entity.User;
import com.eduflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {

        // Email artıq varsa xəta
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu email artıq qeydiyyatdan keçib: " + request.getEmail());
        }

        // Default role — seçilməyibsə STUDENT
        Role role = request.getRole() != null ? request.getRole() : Role.STUDENT;

        // User yarat
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        // Token yarat
        String token = jwtService.generateToken(savedUser);

        return buildAuthResponse(token, savedUser);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {

        // Spring Security-nin öz mexanizmi ilə yoxla
        // Yanlış şifrə olsa avtomatik BadCredentialsException atır
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Doğrudursa DB-dən gətir
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı"));

        String token = jwtService.generateToken(user);

        return buildAuthResponse(token, user);
    }

    private AuthDtos.AuthResponse buildAuthResponse(String token, User user) {
        return AuthDtos.AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .userId(user.getId())
                .build();
    }
}
