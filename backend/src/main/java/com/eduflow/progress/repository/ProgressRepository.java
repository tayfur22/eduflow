package com.eduflow.progress.repository;

import com.eduflow.progress.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    Optional<Progress> findByStudentIdAndLessonId(Long studentId, Long lessonId);

    List<Progress> findByStudentId(Long studentId);

    // Kursun tamamlanmış dərsləri
    @Query("""
        SELECT p FROM Progress p
        JOIN p.lesson l
        JOIN l.section s
        WHERE p.student.id = :studentId
        AND s.course.id = :courseId
        AND p.completed = true
    """)
    List<Progress> findCompletedByStudentAndCourse(Long studentId, Long courseId);

    // Kursun ümumi dərs sayı
    @Query("""
        SELECT COUNT(l) FROM Lesson l
        JOIN l.section s
        WHERE s.course.id = :courseId
    """)
    long countLessonsByCourse(Long courseId);
}