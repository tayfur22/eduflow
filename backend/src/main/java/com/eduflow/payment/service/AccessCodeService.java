package com.eduflow.payment.service;

import com.eduflow.course.entity.Course;
import com.eduflow.course.repository.CourseRepository;
import com.eduflow.enrollment.entity.Enrollment;
import com.eduflow.enrollment.enums.EnrollmentStatus;
import com.eduflow.enrollment.repository.EnrollmentRepository;
import com.eduflow.payment.dto.AccessCodeDtos.*;
import com.eduflow.payment.entity.AccessCode;
import com.eduflow.payment.repository.AccessCodeRepository;
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
public class AccessCodeService {

    private final AccessCodeRepository accessCodeRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    // Teacher kod yaradır
    @Transactional
    public AccessCodeResponse createCode(CreateAccessCodeRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (accessCodeRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("This code already exists");
        }

        AccessCode accessCode = AccessCode.builder()
                .code(request.getCode().toUpperCase())
                .course(course)
                .teacher(teacher)
                .maxUsages(request.getMaxUsages())
                .currentUsages(0)
                .expiresAt(request.getExpiresAt())
                .active(true)
                .build();

        accessCodeRepository.save(accessCode);
        return toResponse(accessCode);
    }

    // Student kod ilə qoşulur — EnrollmentService çağırır bunu
    @Transactional
    public AccessCode validateAndUse(String code) {
        AccessCode accessCode = accessCodeRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Invalid access code"));

        if (!accessCode.isValid()) {
            throw new RuntimeException("Access code is expired or limit reached");
        }

        accessCode.setCurrentUsages(accessCode.getCurrentUsages() + 1);
        return accessCodeRepository.save(accessCode);
    }

    // Teacher öz kodlarını görür
    public List<AccessCodeResponse> getMyCodes() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return accessCodeRepository.findByTeacherId(teacher.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Kodu deaktiv edir
    @Transactional
    public AccessCodeResponse deactivateCode(Long codeId) {
        AccessCode code = accessCodeRepository.findById(codeId)
                .orElseThrow(() -> new RuntimeException("Code not found"));
        code.setActive(false);
        return toResponse(accessCodeRepository.save(code));
    }

    private AccessCodeResponse toResponse(AccessCode code) {
        return AccessCodeResponse.builder()
                .id(code.getId())
                .code(code.getCode())
                .courseId(code.getCourse().getId())
                .courseTitle(code.getCourse().getTitle())
                .maxUsages(code.getMaxUsages())
                .currentUsages(code.getCurrentUsages())
                .expiresAt(code.getExpiresAt())
                .active(code.isActive())
                .createdAt(code.getCreatedAt())
                .build();
    }
}