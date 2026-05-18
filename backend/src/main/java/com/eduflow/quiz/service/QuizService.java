package com.eduflow.quiz.service;

import com.eduflow.course.entity.Course;
import com.eduflow.course.repository.CourseRepository;
import com.eduflow.quiz.dto.QuizDtos;
import com.eduflow.quiz.entity.*;
import com.eduflow.quiz.enums.QuestionType;
import com.eduflow.quiz.repository.*;
import com.eduflow.user.entity.User;
import com.eduflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    // ── QUIZ YARAT ──
    @Transactional
    public QuizDtos.QuizResponse createQuiz(
            Long courseId, QuizDtos.CreateQuizRequest req, String teacherEmail) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Kurs tapılmadı: " + courseId));

        verifyTeacher(course, teacherEmail);

        Quiz quiz = Quiz.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .timeLimitMinutes(req.getTimeLimitMinutes())
                .passingScore(req.getPassingScore() != null ? req.getPassingScore() : 70)
                .shuffleQuestions(req.isShuffleQuestions())
                .maxAttempts(req.getMaxAttempts())
                .difficultyLevel(req.getDifficultyLevel())
                .course(course)
                .build();

        return toQuizResponse(quizRepository.save(quiz));
    }

    // ── KURSA AID QUİZLƏR ──
    public List<QuizDtos.QuizResponse> getQuizzesByCourse(Long courseId) {
        return quizRepository.findByCourseId(courseId)
                .stream().map(this::toQuizResponse).collect(Collectors.toList());
    }

    // ── SUAL ƏLAVƏ ET ──
    @Transactional
    public QuizDtos.QuestionDetailResponse addQuestion(
            Long quizId, QuizDtos.CreateQuestionRequest req, String teacherEmail) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz tapılmadı: " + quizId));

        verifyTeacher(quiz.getCourse(), teacherEmail);

        int order = req.getOrderIndex() != null
                ? req.getOrderIndex()
                : quiz.getQuestions().size() + 1;

        Question question = Question.builder()
                .questionText(req.getQuestionText())
                .questionType(req.getQuestionType())
                .correctAnswer(req.getCorrectAnswer())
                .explanation(req.getExplanation())
                .points(req.getPoints() != null ? req.getPoints() : 1)
                .orderIndex(order)
                .quiz(quiz)
                .build();

        // MULTIPLE_CHOICE üçün seçimləri əlavə et
        if (req.getQuestionType() == QuestionType.MULTIPLE_CHOICE
                && req.getOptions() != null) {
            List<QuestionOption> options = req.getOptions().stream()
                    .map(o -> QuestionOption.builder()
                            .optionLabel(o.getOptionLabel())
                            .optionText(o.getOptionText())
                            .question(question)
                            .build())
                    .collect(Collectors.toList());
            question.getOptions().addAll(options);
        }

        return toQuestionDetailResponse(questionRepository.save(question));
    }

    // ── QUİZİ ŞAGIRD ÜÇÜN GƏTİR (cavabsız) ──
    public QuizDtos.QuizWithQuestionsResponse getQuizForStudent(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz tapılmadı: " + quizId));

        List<QuizDtos.QuestionResponse> questions = quiz.getQuestions()
                .stream().map(this::toQuestionResponse).collect(Collectors.toList());

        return QuizDtos.QuizWithQuestionsResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .passingScore(quiz.getPassingScore())
                .questions(questions)
                .build();
    }

    // ── QUİZ SUBMIT ET VƏ OTOMATİK QİYMƏTLƏNDİR ──
    @Transactional
    public QuizDtos.SubmissionResponse submitQuiz(
            Long quizId, QuizDtos.SubmitQuizRequest req, String studentEmail) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz tapılmadı: " + quizId));

        User student = getUser(studentEmail);

        // Cəhd sayını yoxla
        if (quiz.getMaxAttempts() != null) {
            long attemptCount = submissionRepository
                    .findByStudentIdAndQuizId(student.getId(), quizId).size();
            if (attemptCount >= quiz.getMaxAttempts()) {
                throw new RuntimeException("Maksimum cəhd sayına çatdınız: " + quiz.getMaxAttempts());
            }
        }

        // Cəhd nömrəsini hesabla
        int attemptNumber = submissionRepository
                .findByStudentIdAndQuizId(student.getId(), quizId).size() + 1;

        // Submission yarat
        Submission submission = Submission.builder()
                .student(student)
                .quiz(quiz)
                .attemptNumber(attemptNumber)
                .startedAt(LocalDateTime.now())
                .build();

        // Hər sualı qiymətləndir
        List<SubmissionAnswer> answers = new ArrayList<>();
        int totalScore = 0;
        int maxScore = 0;

        for (Question question : quiz.getQuestions()) {
            maxScore += question.getPoints();
            String givenAnswer = req.getAnswers().get(question.getId());
            boolean correct = isCorrect(question, givenAnswer);
            int pointsEarned = correct ? question.getPoints() : 0;
            totalScore += pointsEarned;

            answers.add(SubmissionAnswer.builder()
                    .submission(submission)
                    .question(question)
                    .givenAnswer(givenAnswer)
                    .correct(correct)
                    .pointsEarned(pointsEarned)
                    .build());
        }

        // Nəticəni hesabla
        double percentage = maxScore > 0 ? (double) totalScore / maxScore * 100 : 0;
        boolean passed = percentage >= quiz.getPassingScore();

        submission.setScore(totalScore);
        submission.setMaxScore(maxScore);
        submission.setPercentage(Math.round(percentage * 100.0) / 100.0);
        submission.setPassed(passed);
        submission.setCompletedAt(LocalDateTime.now());
        submission.getAnswers().addAll(answers);

        Submission saved = submissionRepository.save(submission);
        return toSubmissionResponse(saved);
    }

    // ── ŞAGİRDİN NƏTİCƏLƏRİ ──
    public List<QuizDtos.SubmissionResponse> getStudentSubmissions(String studentEmail) {
        User student = getUser(studentEmail);
        return submissionRepository.findByStudentId(student.getId())
                .stream().map(this::toSubmissionResponse).collect(Collectors.toList());
    }

    // ── MÜƏLLİMİN QUİZİNƏ GƏLƏN CAVABLAR ──
    public List<QuizDtos.SubmissionResponse> getSubmissionsForQuiz(
            Long quizId, String teacherEmail) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz tapılmadı"));
        verifyTeacher(quiz.getCourse(), teacherEmail);
        return submissionRepository.findByQuizId(quizId)
                .stream().map(this::toSubmissionResponse).collect(Collectors.toList());
    }

    // ── KÖMƏKÇI METODLAR ──

    // Avtomatik qiymətləndirmə məntiqi
    private boolean isCorrect(Question question, String givenAnswer) {
        if (givenAnswer == null) return false;
        return switch (question.getQuestionType()) {
            case MULTIPLE_CHOICE, TRUE_FALSE ->
                    question.getCorrectAnswer().trim().equalsIgnoreCase(givenAnswer.trim());
            case SHORT_ANSWER ->
                    question.getCorrectAnswer().trim().equalsIgnoreCase(givenAnswer.trim());
        };
    }

    private void verifyTeacher(Course course, String teacherEmail) {
        if (!course.getTeacher().getEmail().equals(teacherEmail)) {
            throw new RuntimeException("Bu əməliyyat üçün icazəniz yoxdur");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("İstifadəçi tapılmadı: " + email));
    }

    // ── MAPPER-LƏR ──

    private QuizDtos.QuizResponse toQuizResponse(Quiz q) {
        return QuizDtos.QuizResponse.builder()
                .id(q.getId()).title(q.getTitle()).description(q.getDescription())
                .timeLimitMinutes(q.getTimeLimitMinutes()).passingScore(q.getPassingScore())
                .shuffleQuestions(q.isShuffleQuestions()).maxAttempts(q.getMaxAttempts())
                .difficultyLevel(q.getDifficultyLevel()).courseId(q.getCourse().getId())
                .questionCount(q.getQuestions().size()).createdAt(q.getCreatedAt())
                .build();
    }

    private QuizDtos.QuestionResponse toQuestionResponse(Question q) {
        return QuizDtos.QuestionResponse.builder()
                .id(q.getId()).questionText(q.getQuestionText())
                .questionType(q.getQuestionType()).points(q.getPoints())
                .orderIndex(q.getOrderIndex())
                .options(q.getOptions().stream().map(this::toOptionResponse).collect(Collectors.toList()))
                .build();
    }

    private QuizDtos.QuestionDetailResponse toQuestionDetailResponse(Question q) {
        return QuizDtos.QuestionDetailResponse.builder()
                .id(q.getId()).questionText(q.getQuestionText())
                .questionType(q.getQuestionType()).correctAnswer(q.getCorrectAnswer())
                .explanation(q.getExplanation()).points(q.getPoints())
                .orderIndex(q.getOrderIndex())
                .options(q.getOptions().stream().map(this::toOptionResponse).collect(Collectors.toList()))
                .build();
    }

    private QuizDtos.OptionResponse toOptionResponse(QuestionOption o) {
        return QuizDtos.OptionResponse.builder()
                .id(o.getId()).optionLabel(o.getOptionLabel()).optionText(o.getOptionText())
                .build();
    }

    private QuizDtos.SubmissionResponse toSubmissionResponse(Submission s) {
        List<QuizDtos.AnswerResult> answerResults = s.getAnswers().stream()
                .map(a -> QuizDtos.AnswerResult.builder()
                        .questionId(a.getQuestion().getId())
                        .questionText(a.getQuestion().getQuestionText())
                        .givenAnswer(a.getGivenAnswer())
                        .correctAnswer(a.getQuestion().getCorrectAnswer())
                        .explanation(a.getQuestion().getExplanation())
                        .correct(a.isCorrect())
                        .pointsEarned(a.getPointsEarned())
                        .maxPoints(a.getQuestion().getPoints())
                        .build())
                .collect(Collectors.toList());

        return QuizDtos.SubmissionResponse.builder()
                .id(s.getId()).quizId(s.getQuiz().getId())
                .quizTitle(s.getQuiz().getTitle())
                .score(s.getScore()).maxScore(s.getMaxScore())
                .percentage(s.getPercentage()).passed(s.isPassed())
                .attemptNumber(s.getAttemptNumber())
                .startedAt(s.getStartedAt()).completedAt(s.getCompletedAt())
                .answers(answerResults)
                .build();
    }
}
