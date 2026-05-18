package com.eduflow.enrollment.service;

import com.eduflow.course.entity.Course;
import com.eduflow.course.enums.AccessType;
import com.eduflow.course.repository.CourseRepository;
import com.eduflow.enrollment.dto.EnrollmentDtos.*;
import com.eduflow.enrollment.entity.Enrollment;
import com.eduflow.enrollment.enums.EnrollmentStatus;
import com.eduflow.enrollment.repository.EnrollmentRepository;
import com.eduflow.payment.entity.AccessCode;
import com.eduflow.payment.service.AccessCodeService;
import com.eduflow.user.entity.User;
import com.eduflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AccessCodeService accessCodeService;

    // Pulsuz kursa qoşul
    @Transactional
    public EnrollmentResponse enrollFree(Long courseId) {
        User student = getCurrentUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // isFree() yox, AccessType.FREE yoxlanır
        if (course.getAccessType() != AccessType.FREE) {
            throw new RuntimeException("This course is not free");
        }

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new RuntimeException("Already enrolled");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .accessMethod("FREE")
                .build();

        return toResponse(enrollmentRepository.save(enrollment));
    }
    // Access code ilə qoşul
    @Transactional
    public EnrollmentResponse enrollWithCode(EnrollWithCodeRequest request) {
        User student = getCurrentUser();

        // Kodu yoxla və istifadə et
        AccessCode accessCode = accessCodeService.validateAndUse(request.getAccessCode());
        Course course = accessCode.getCourse();

        if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new RuntimeException("Already enrolled");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .accessMethod("CODE")
                .build();

        return toResponse(enrollmentRepository.save(enrollment));
    }

    // Öz kurslarım
    public List<EnrollmentResponse> getMyEnrollments() {
        User student = getCurrentUser();
        return enrollmentRepository.findByStudentId(student.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Teacher - kursun şagirdləri
    public List<EnrollmentResponse> getCourseEnrollments(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .studentName(enrollment.getStudent().getFullName())
                .status(enrollment.getStatus())
                .accessMethod(enrollment.getAccessMethod())
                .enrolledAt(enrollment.getEnrolledAt())
                .build();
    }
}