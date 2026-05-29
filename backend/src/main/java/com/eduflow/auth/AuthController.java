package com.eduflow.auth;

import com.eduflow.auth.dto.AuthDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthDtos.AuthResponse> register(
            @Valid @RequestBody AuthDtos.RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponse> login(
            @Valid @RequestBody AuthDtos.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthDtos.RefreshResponse> refresh(
            @Valid @RequestBody AuthDtos.RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody AuthDtos.RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    // ── YENİ: Şifrə sıfırlama tələbi ───────────────────────
    // POST /api/auth/forgot-password
    // Frontend-də forgot-password səhifəsi artıq bu endpointi çağırır
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @RequestBody AuthDtos.ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        // Həmişə 200 qaytar — emailin mövcud olub-olmadığını açıqlamırıq
        return ResponseEntity.ok().build();
    }

    // ── YENİ: Şifrəni sıfırla ──────────────────────────────
    // POST /api/auth/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<String> me() {
        return ResponseEntity.ok("Token etibarlıdır!");
    }
}
