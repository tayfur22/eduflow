package com.eduflow.ai.service;

import com.eduflow.ai.dto.AiDtos.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizGeneratorService {

    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public QuizGenerateResponse generateQuiz(QuizGenerateRequest request) {
        String systemPrompt = """
        Sən quiz generatoru assistantısan.
        Yalnız JSON formatında cavab ver, başqa heç nə yazma.
        JSON-da sual mətnlərinin içində dırnaq işarəsi (\") işlətmə.
        JSON strukturu belə olmalıdır:
        [
          {
            "questionText": "Sual mətni burada",
            "options": ["A seçim", "B seçim", "C seçim", "D seçim"],
            "correctAnswer": "A seçim",
            "explanation": "Izah burada"
          }
        ]
        Mətn içində sinif adlarını tək dırnaqla yaz: NəqliyyatVasitəsi sinifi.
        Yalnız JSON array qaytar, heç bir əlavə mətn yazma.
        """;

        String userMessage = String.format(
                "Mövzu: %s\nSual sayı: %d\nÇətinlik: %s\n" +
                        "Azərbaycan dilində quiz yarat.\n" +
                        "VACIB: JSON tam bağlanmış olmalıdır. Hər sual tam yazılmalıdır.",
                request.getTopic(),
                Math.min(request.getQuestionCount(), 5), // maksimum 5
                request.getDifficulty()
        );
        String jsonResponse = aiService.callOpenAi(systemPrompt, userMessage);

        try {
            String cleanJson = jsonResponse.trim();
            // Markdown code block təmizlə
            cleanJson = cleanJson.replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();
            // İlk [ dan axırıncı ] a qədər al
            int start = cleanJson.indexOf('[');
            int end = cleanJson.lastIndexOf(']');
            if (start != -1 && end != -1) {
                cleanJson = cleanJson.substring(start, end + 1);
            }

            List<GeneratedQuestion> questions = objectMapper.readValue(
                    cleanJson,
                    new TypeReference<List<GeneratedQuestion>>() {}
            );

            return QuizGenerateResponse.builder()
                    .topic(request.getTopic())
                    .questions(questions)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Quiz parse error: " + e.getMessage());
        }
    }
}