package com.eduflow.quiz.entity;

import com.eduflow.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hansı şagird
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Hansı quiz
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // Alınan xal
    private Integer score;

    // Maksimum xal
    private Integer maxScore;

    // Faiz olaraq nəticə
    private Double percentage;

    // Keçdi mi?
    private boolean passed;

    // Neçənci cəhd
    @Column(nullable = false)
    private Integer attemptNumber = 1;

    // Nə vaxt başladı
    @Column(nullable = false)
    private LocalDateTime startedAt;

    // Nə vaxt bitdi
    private LocalDateTime completedAt;

    // Şagirdin cavabları
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SubmissionAnswer> answers = new ArrayList<>();
}
