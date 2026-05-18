package com.eduflow.progress.service;

import com.eduflow.course.entity.Course;
import com.eduflow.course.repository.CourseRepository;
import com.eduflow.course.entity.Lesson;
import com.eduflow.course.repository.LessonRepository;
import com.eduflow.progress.dto.ProgressDtos.*;
import com.eduflow.progress.entity.Progress;
import com.eduflow.progress.repository.ProgressRepository;
import com.eduflow.user.entity.User;
import com.eduflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    // Dərsi tamamlandı kimi işarələ
    @Transactional
    public ProgressResponse completeLesson(Long lessonId) {
        User student = getCurrentUser();
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        Progress progress = progressRepository
                .findByStudentIdAndLessonId(student.getId(), lessonId)
                .orElse(Progress.builder()
                        .student(student)
                        .lesson(lesson)
                        .build());

        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepository.save(progress);

        return toResponse(progress);
    }

    // Kursda progress statistikası
    public CourseProgressResponse getCourseProgress(Long courseId, String studentEmail) {

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        long totalLessons = progressRepository.countLessonsByCourse(courseId);

        long completedLessons = progressRepository
                .findCompletedByStudentAndCourse(student.getId(), courseId)
                .size();

        double percentage = totalLessons == 0 ? 0 :
                (double) completedLessons / totalLessons * 100;

        return CourseProgressResponse.builder()
                .courseId(courseId)
                .courseTitle(course.getTitle())
                .totalLessons((int) totalLessons)
                .completedLessons((int) completedLessons)
                .percentage(Math.round(percentage * 10.0) / 10.0)
                .certificateEarned(percentage == 100.0)
                .build();
    }

    public CourseProgressResponse getCourseProgress(Long courseId) {
        User student = getCurrentUser();
        return getCourseProgress(courseId, student.getEmail());
    }

    // Bütün dərslərin progress-i
    public List<ProgressResponse> getMyProgress() {
        User student = getCurrentUser();
        return progressRepository.findByStudentId(student.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private ProgressResponse toResponse(Progress progress) {
        return ProgressResponse.builder()
                .lessonId(progress.getLesson().getId())
                .lessonTitle(progress.getLesson().getTitle())
                .completed(progress.isCompleted())
                .completedAt(progress.getCompletedAt())
                .build();
    }
}