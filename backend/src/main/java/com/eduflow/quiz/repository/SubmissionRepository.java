package com.eduflow.quiz.repository;

import com.eduflow.quiz.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    // Şagirdin bir quizdəki bütün cəhdləri
    List<Submission> findByStudentIdAndQuizId(Long studentId, Long quizId);

    // Şagirdin bütün submissionları
    List<Submission> findByStudentId(Long studentId);

    // Şagirdin bir quizdəki son cəhdi
    Optional<Submission> findTopByStudentIdAndQuizIdOrderByAttemptNumberDesc(
            Long studentId, Long quizId);

    // Bir quizin bütün submissionları (müəllim üçün)
    List<Submission> findByQuizId(Long quizId);
}
