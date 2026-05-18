package com.eduflow.payment.entity;

import com.eduflow.course.entity.Course;
import com.eduflow.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_codes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccessCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unikal kod — məs: "JAVA2024"
    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Bu kodu yaradan müəllim
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    // Neçə dəfə istifadə edilə bilər (null = limitsiz)
    private Integer maxUsages;

    // Neçə dəfə istifadə edilib
    @Column(nullable = false)
    private Integer currentUsages = 0;

    // Bitmə tarixi (null = limitsiz)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Kod istifadə edilə bilərmi?
    public boolean isValid() {
        if (!active) return false;
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) return false;
        if (maxUsages != null && currentUsages >= maxUsages) return false;
        return true;
    }
}