package com.eduflow.enrollment.controller;

import com.eduflow.enrollment.dto.EnrollmentDtos.*;
import com.eduflow.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // Pulsuz kursa qoşul
    @PostMapping("/free/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentResponse> enrollFree(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.enrollFree(courseId));
    }

    // Kod ilə qoşul
    @PostMapping("/code")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentResponse> enrollWithCode(@RequestBody EnrollWithCodeRequest request) {
        return ResponseEntity.ok(enrollmentService.enrollWithCode(request));
    }

    // Öz kurslarım
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments() {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments());
    }

    // Teacher - kursun şagirdlərini gör
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<EnrollmentResponse>> getCourseEnrollments(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getCourseEnrollments(courseId));
    }
}