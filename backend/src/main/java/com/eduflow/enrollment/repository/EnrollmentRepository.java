package com.eduflow.enrollment.repository;

import com.eduflow.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Şagird bu kursda qeydiyyatlıdır?
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    // Şagirdin bütün kursları
    List<Enrollment> findByStudentId(Long studentId);

    // Kursun bütün şagirdləri
    List<Enrollment> findByCourseId(Long courseId);

    // Konkret enrollment
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
}