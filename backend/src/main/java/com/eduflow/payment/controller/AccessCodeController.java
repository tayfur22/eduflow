package com.eduflow.payment.controller;

import com.eduflow.payment.dto.AccessCodeDtos.*;
import com.eduflow.payment.service.AccessCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/access-codes")
@RequiredArgsConstructor
public class AccessCodeController {

    private final AccessCodeService accessCodeService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AccessCodeResponse> createCode(@RequestBody CreateAccessCodeRequest request) {
        return ResponseEntity.ok(accessCodeService.createCode(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<AccessCodeResponse>> getMyCodes() {
        return ResponseEntity.ok(accessCodeService.getMyCodes());
    }

    @PatchMapping("/{codeId}/deactivate")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AccessCodeResponse> deactivate(@PathVariable Long codeId) {
        return ResponseEntity.ok(accessCodeService.deactivateCode(codeId));
    }
}