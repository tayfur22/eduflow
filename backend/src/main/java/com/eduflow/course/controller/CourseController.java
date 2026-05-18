package com.eduflow.course.controller;

import com.eduflow.course.dto.CourseDtos;
import com.eduflow.course.service.CourseService;
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
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // ── PUBLIC endpointlər (token lazım deyil) ──

    @GetMapping("/api/courses/public")
    public ResponseEntity<List<CourseDtos.CourseSummary>> getAllPublished() {
        return ResponseEntity.ok(courseService.getAllPublishedCourses());
    }

    @GetMapping("/api/courses/public/{id}")
    public ResponseEntity<CourseDtos.CourseResponse> getPublicCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping("/api/courses/public/search")
    public ResponseEntity<List<CourseDtos.CourseSummary>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(courseService.searchCourses(keyword));
    }

    // ── TEACHER endpointləri ──

    @PostMapping("/api/courses")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CourseDtos.CourseResponse> createCourse(
            @Valid @RequestBody CourseDtos.CreateCourseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.createCourse(request, userDetails.getUsername()));
    }

    @GetMapping("/api/courses/my")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<CourseDtos.CourseSummary>> myCourses(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(courseService.getMyCoursesAsTeacher(userDetails.getUsername()));
    }

    @PutMapping("/api/courses/{id}/publish")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CourseDtos.CourseResponse> publish(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(courseService.publishCourse(id, userDetails.getUsername()));
    }

    @PutMapping("/api/courses/{id}/unpublish")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CourseDtos.CourseResponse> unpublish(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(courseService.unpublishCourse(id, userDetails.getUsername()));
    }

    // ── SECTION ──

    @PostMapping("/api/courses/{courseId}/sections")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CourseDtos.SectionResponse> addSection(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseDtos.CreateSectionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.addSection(courseId, request, userDetails.getUsername()));
    }

    // ── LESSON ──

    @PostMapping("/api/sections/{sectionId}/lessons")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<CourseDtos.LessonResponse> addLesson(
            @PathVariable Long sectionId,
            @Valid @RequestBody CourseDtos.CreateLessonRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.addLesson(sectionId, request, userDetails.getUsername()));
    }
}