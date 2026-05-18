package com.eduflow.quiz.entity;

import com.eduflow.quiz.enums.QuestionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;

    // Düzgün cavab — MULTIPLE_CHOICE üçün "A", TRUE_FALSE üçün "true/false"
    @Column(nullable = false)
    private String correctAnswer;

    // Sualın izahı — cavabdan sonra göstərilir
    @Column(columnDefinition = "TEXT")
    private String explanation;

    // Sualın xal dəyəri
    @Column(nullable = false)
    private Integer points = 1;

    // Sıra nömrəsi
    @Column(nullable = false)
    private Integer orderIndex;

    // Bu sual hansı quizə aiddir
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // MULTIPLE_CHOICE üçün seçimlər (A, B, C, D)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("optionLabel ASC")
    @Builder.Default
    private List<QuestionOption> options = new ArrayList<>();
}
