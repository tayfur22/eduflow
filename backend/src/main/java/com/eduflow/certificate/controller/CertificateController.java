package com.eduflow.certificate.controller;

import com.eduflow.certificate.dto.CertificateDtos.CertificateResponse;
import com.eduflow.certificate.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    // Sertifikat al — kurs 100% tamamlanmalıdır
    @PostMapping("/claim/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CertificateResponse> claimCertificate(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                certificateService.claimCertificate(courseId, userDetails.getUsername()));
    }

    // Öz sertifikatlarım
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CertificateResponse>> getMyCertificates(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                certificateService.getMyCertificates(userDetails.getUsername()));
    }

    // Public — sertifikat nömrəsi ilə yoxla
    @GetMapping("/verify/{certificateNumber}")
    public ResponseEntity<CertificateResponse> verify(
            @PathVariable String certificateNumber) {
        return ResponseEntity.ok(
                certificateService.verifyCertificate(certificateNumber));
    }
}
