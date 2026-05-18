package com.eduflow.quiz.entity;

import com.eduflow.course.entity.Course;
import com.eduflow.quiz.enums.DifficultyLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Neçə dəqiqə vaxt verilir (null = limitsiz)
    private Integer timeLimitMinutes;

    // Keçmək üçün minimum faiz (məs: 70 = 70%)
    @Column(nullable = false)
    private Integer passingScore = 70;

    // Suallar qarışdırılsın?
    @Column(nullable = false)
    private boolean shuffleQuestions = false;

    // Neçə dəfə cəhd etmək olar (null = limitsiz)
    private Integer maxAttempts;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    // Bu quiz hansı kursa aiddir
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Quizin sualları
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
