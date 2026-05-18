package com.eduflow.quiz.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "submission_answers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SubmissionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // Şagirdin verdiyi cavab
    private String givenAnswer;

    // Düzgündür?
    private boolean correct;

    // Qazandığı xal
    private Integer pointsEarned;
}
