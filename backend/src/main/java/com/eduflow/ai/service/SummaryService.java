package com.eduflow.ai.service;

import com.eduflow.ai.dto.AiDtos.*;
import com.eduflow.course.entity.Lesson;
import com.eduflow.course.repository.LessonRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final AiService aiService;
    private final LessonRepository lessonRepository;
    private final ObjectMapper objectMapper;

    public SummaryResponse summarize(SummaryRequest request) {
        String textToSummarize;

        if (request.getLessonId() != null) {
            Lesson lesson = lessonRepository.findById(request.getLessonId())
                    .orElseThrow(() -> new RuntimeException("Lesson not found"));
            textToSummarize = lesson.getTitle() + "\n" +
                    (lesson.getTextContent() != null ? lesson.getTextContent() : "");
        } else {
            textToSummarize = request.getText();
        }

        String systemPrompt = """
                Sən mətn xülasəsi assistantısan.
                Yalnız JSON formatında cavab ver:
                {
                  "summary": "Qısa xülasə mətni",
                  "keyPoints": ["Əsas məqam 1", "Əsas məqam 2", "Əsas məqam 3"]
                }
                Azərbaycan dilində cavab ver.
                """;

        String jsonResponse = aiService.callOpenAi(systemPrompt, textToSummarize);

        try {
            String cleanJson = jsonResponse.trim()
                    .replaceAll("```json", "").replaceAll("```", "").trim();

            Map<String, Object> parsed = objectMapper.readValue(cleanJson,
                    new TypeReference<Map<String, Object>>() {});

            return SummaryResponse.builder()
                    .summary((String) parsed.get("summary"))
                    .keyPoints((List<String>) parsed.get("keyPoints"))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Summary parse error: " + e.getMessage());
        }
    }
}