package com.eduflow.ai.service;

import com.eduflow.ai.dto.AiDtos.*;
import com.eduflow.course.entity.Lesson;
import com.eduflow.course.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TutorService {

    private final AiService aiService;
    private final LessonRepository lessonRepository;

    public TutorResponse askTutor(TutorRequest request) {
        String lessonContext = "";
        String lessonTitle = null;

        // Əgər lessonId verilib, context əlavə et
        if (request.getLessonId() != null) {
            Lesson lesson = lessonRepository.findById(request.getLessonId())
                    .orElseThrow(() -> new RuntimeException("Lesson not found"));
            lessonTitle = lesson.getTitle();
            lessonContext = "\n\nDərs mövzusu: " + lesson.getTitle() +
                    "\nDərs məzmunu: " + (lesson.getTextContent() != null
                    ? lesson.getTextContent() : "");
        }

        String systemPrompt = """
                Sən bir təhsil assistantısan. Şagirdlərə aydın, sadə dildə izah verirsən.
                Cavabların:
                - Sadə və anlaşıqlı olsun
                - Mümkün olduqda nümunə ver
                - Azərbaycan dilində cavab ver
                - Proqramlaşdırma suallarında kod nümunəsi göstər
                """ + lessonContext;

        String answer = aiService.callOpenAi(systemPrompt, request.getQuestion());

        return TutorResponse.builder()
                .answer(answer)
                .lessonTitle(lessonTitle)
                .build();
    }
}