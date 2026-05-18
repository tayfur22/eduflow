package com.eduflow.course.dto;

import com.eduflow.course.enums.AccessType;
import com.eduflow.course.enums.LessonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class CourseDtos {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateCourseRequest {
        @NotBlank(message = "Kurs adı boş ola bilməz")
        private String title;
        private String description;
        private String thumbnailUrl;
        @NotNull(message = "Giriş tipi seçilməlidir")
        private AccessType accessType;
        private Double price;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateSectionRequest {
        @NotBlank(message = "Bölmə adı boş ola bilməz")
        private String title;
        private Integer orderIndex;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateLessonRequest {
        @NotBlank(message = "Dərs adı boş ola bilməz")
        private String title;
        @NotNull(message = "Dərs tipi seçilməlidir")
        private LessonType lessonType;
        private String contentUrl;
        private String textContent;
        private Integer durationMinutes;
        private Integer orderIndex;
        private boolean freePreview = false;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class LessonResponse {
        private Long id;
        private String title;
        private LessonType lessonType;
        private String contentUrl;
        private String textContent;
        private Integer durationMinutes;
        private Integer orderIndex;
        private boolean freePreview;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SectionResponse {
        private Long id;
        private String title;
        private Integer orderIndex;
        private List<LessonResponse> lessons;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CourseResponse {
        private Long id;
        private String title;
        private String description;
        private String thumbnailUrl;
        private AccessType accessType;
        private Double price;
        private boolean published;
        private String teacherName;
        private Long teacherId;
        private List<SectionResponse> sections;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CourseSummary {
        private Long id;
        private String title;
        private String description;
        private String thumbnailUrl;
        private AccessType accessType;
        private Double price;
        private boolean published;
        private String teacherName;
        private LocalDateTime createdAt;
    }
}