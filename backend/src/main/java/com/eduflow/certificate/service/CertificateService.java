package com.eduflow.certificate.service;

import com.eduflow.certificate.dto.CertificateDtos.CertificateResponse;
import com.eduflow.certificate.entity.Certificate;
import com.eduflow.certificate.repository.CertificateRepository;
import com.eduflow.course.entity.Course;
import com.eduflow.course.repository.CourseRepository;
import com.eduflow.progress.dto.ProgressDtos.CourseProgressResponse;
import com.eduflow.progress.service.ProgressService;
import com.eduflow.user.entity.User;
import com.eduflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ProgressService progressService;

    // ── SERTİFİKAT AL ──
    @Transactional
    public CertificateResponse claimCertificate(Long courseId, String studentEmail) {

        User student = getUser(studentEmail);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Kurs tapılmadı: " + courseId));

        // Artıq sertifikat varsa — eyni sertifikatı qaytar
        if (certificateRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            return certificateRepository
                    .findByStudentIdAndCourseId(student.getId(), courseId)
                    .map(this::toResponse)
                    .orElseThrow();
        }

        // Progress yoxla — 100% tamamlanmalıdır
        CourseProgressResponse progress = progressService.getCourseProgress(courseId, studentEmail);
        if (progress.getPercentage() < 100.0) {
            throw new RuntimeException(
                    "Kurs hələ tamamlanmayıb. Cari progress: " + progress.getPercentage() + "%");
        }

        Certificate certificate = Certificate.builder()
                .student(student)
                .course(course)
                .build();

        return toResponse(certificateRepository.save(certificate));
    }

    // ── ÖZ SERTİFİKATLARIM ──
    public List<CertificateResponse> getMyCertificates(String studentEmail) {
        User student = getUser(studentEmail);
        return certificateRepository.findByStudentId(student.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── SERTİFİKAT YOXLA (public) ──
    public CertificateResponse verifyCertificate(String certificateNumber) {
        return certificateRepository.findByCertificateNumber(certificateNumber)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Sertifikat tapılmadı: " + certificateNumber));
    }

    // ── KÖMƏKÇI ──
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı: " + email));
    }

    private CertificateResponse toResponse(Certificate cert) {
        return CertificateResponse.builder()
                .id(cert.getId())
                .certificateNumber(cert.getCertificateNumber())
                .studentName(cert.getStudent().getFullName())
                .courseTitle(cert.getCourse().getTitle())
                .teacherName(cert.getCourse().getTeacher().getFullName())
                .issuedAt(cert.getIssuedAt())
                .build();
    }
}
