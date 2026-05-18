package com.eduflow.progress.controller;

import com.eduflow.progress.dto.ProgressDtos.*;
import com.eduflow.progress.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // Dərsi tamamla
    @PostMapping("/complete/{lessonId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ProgressResponse> completeLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(progressService.completeLesson(lessonId));
    }

    // Kursdakı progress-i gör
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(@PathVariable Long courseId) {
        return ResponseEntity.ok(progressService.getCourseProgress(courseId));
    }

    // Bütün progress
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ProgressResponse>> getMyProgress() {
        return ResponseEntity.ok(progressService.getMyProgress());
    }
}