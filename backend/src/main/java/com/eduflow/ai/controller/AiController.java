package com.eduflow.ai.controller;

import com.eduflow.ai.dto.AiDtos.*;
import com.eduflow.ai.service.QuizGeneratorService;
import com.eduflow.ai.service.SummaryService;
import com.eduflow.ai.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final TutorService tutorService;
    private final QuizGeneratorService quizGeneratorService;
    private final SummaryService summaryService;

    // Şagird sual verir
    @PostMapping("/tutor")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<TutorResponse> askTutor(@RequestBody TutorRequest request) {
        return ResponseEntity.ok(tutorService.askTutor(request));
    }

    // Müəllim quiz generasiya edir
    @PostMapping("/quiz/generate")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<QuizGenerateResponse> generateQuiz(
            @RequestBody QuizGenerateRequest request) {
        return ResponseEntity.ok(quizGeneratorService.generateQuiz(request));
    }

    // Dərs xülasəsi
    @PostMapping("/summary")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<SummaryResponse> summarize(@RequestBody SummaryRequest request) {
        return ResponseEntity.ok(summaryService.summarize(request));
    }
}