package com.eduflow.quiz.controller;

import com.eduflow.quiz.dto.QuizDtos;
import com.eduflow.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // ── TEACHER endpointləri ──

    // Kurs üçün quiz yarat
    @PostMapping("/courses/{courseId}/quizzes")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<QuizDtos.QuizResponse> createQuiz(
            @PathVariable Long courseId,
            @Valid @RequestBody QuizDtos.CreateQuizRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quizService.createQuiz(courseId, request, userDetails.getUsername()));
    }

    // Quizə sual əlavə et
    @PostMapping("/quizzes/{quizId}/questions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<QuizDtos.QuestionDetailResponse> addQuestion(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizDtos.CreateQuestionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quizService.addQuestion(quizId, request, userDetails.getUsername()));
    }

    // Quizin bütün cavablarını gör
    @GetMapping("/quizzes/{quizId}/submissions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<QuizDtos.SubmissionResponse>> getSubmissions(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                quizService.getSubmissionsForQuiz(quizId, userDetails.getUsername()));
    }

    // ── STUDENT + TEACHER endpointləri ──

    // Kursun quizlərini gör
    @GetMapping("/courses/{courseId}/quizzes")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    public ResponseEntity<List<QuizDtos.QuizResponse>> getCourseQuizzes(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(quizService.getQuizzesByCourse(courseId));
    }

    // Quizi başla (şagird üçün - cavabsız)
    @GetMapping("/quizzes/{quizId}/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizDtos.QuizWithQuestionsResponse> startQuiz(
            @PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizForStudent(quizId));
    }

    // Quizi təhvil ver
    @PostMapping("/quizzes/{quizId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizDtos.SubmissionResponse> submitQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizDtos.SubmitQuizRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                quizService.submitQuiz(quizId, request, userDetails.getUsername()));
    }

    // Öz nəticələrimi gör
    @GetMapping("/submissions/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<QuizDtos.SubmissionResponse>> mySubmissions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(quizService.getStudentSubmissions(userDetails.getUsername()));
    }
}
