package com.eduflow.course.service;

import com.eduflow.course.dto.CourseDtos;
import com.eduflow.course.entity.Course;
import com.eduflow.course.entity.Lesson;
import com.eduflow.course.entity.Section;
import com.eduflow.course.repository.CourseRepository;
import com.eduflow.course.repository.LessonRepository;
import com.eduflow.course.repository.SectionRepository;
import com.eduflow.user.entity.User;
import com.eduflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    @Transactional
    public CourseDtos.CourseResponse createCourse(CourseDtos.CreateCourseRequest req, String teacherEmail) {
        User teacher = getUser(teacherEmail);
        Course course = Course.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .thumbnailUrl(req.getThumbnailUrl())
                .accessType(req.getAccessType())
                .price(req.getPrice())
                .published(false)
                .teacher(teacher)
                .build();
        return toCourseResponse(courseRepository.save(course));
    }



    @Transactional
    public CourseDtos.CourseResponse publishCourse(Long courseId, String teacherEmail) {
        Course course = getAndVerify(courseId, teacherEmail);
        course.setPublished(true);
        return toCourseResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseDtos.CourseResponse unpublishCourse(Long courseId, String teacherEmail) {
        Course course = getAndVerify(courseId, teacherEmail);
        course.setPublished(false);
        return toCourseResponse(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourse(Long courseId, String teacherEmail) {
        Course course = getAndVerify(courseId, teacherEmail);
        courseRepository.delete(course);
    }

    public List<CourseDtos.CourseSummary> getMyCoursesAsTeacher(String teacherEmail) {
        User teacher = getUser(teacherEmail);
        return courseRepository.findByTeacherId(teacher.getId())
                .stream().map(this::toCourseSummary).collect(Collectors.toList());
    }

    public List<CourseDtos.CourseSummary> getAllPublishedCourses() {
        return courseRepository.findByPublishedTrue()
                .stream().map(this::toCourseSummary).collect(Collectors.toList());
    }

    public CourseDtos.CourseResponse getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Kurs tapılmadı: " + courseId));
        return toCourseResponse(course);
    }

    public List<CourseDtos.CourseSummary> searchCourses(String keyword) {
        return courseRepository.searchByTitle(keyword)
                .stream().map(this::toCourseSummary).collect(Collectors.toList());
    }

    // ── YENİ: Dərsi ID ilə gətir (learn səhifəsi üçün) ──
    public CourseDtos.LessonDetailResponse getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Dərs tapılmadı: " + lessonId));

        Section section = lesson.getSection();
        Course course = section.getCourse();

        return CourseDtos.LessonDetailResponse.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .lessonType(lesson.getLessonType())
                .contentUrl(lesson.getContentUrl())
                .textContent(lesson.getTextContent())
                .durationMinutes(lesson.getDurationMinutes())
                .orderIndex(lesson.getOrderIndex())
                .freePreview(lesson.isFreePreview())
                .sectionId(section.getId())
                .sectionTitle(section.getTitle())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .build();
    }

    @Transactional
    public CourseDtos.SectionResponse addSection(Long courseId, CourseDtos.CreateSectionRequest req, String teacherEmail) {
        Course course = getAndVerify(courseId, teacherEmail);
        int order = req.getOrderIndex() != null ? req.getOrderIndex() : course.getSections().size() + 1;
        Section section = Section.builder()
                .title(req.getTitle())
                .orderIndex(order)
                .course(course)
                .build();
        return toSectionResponse(sectionRepository.save(section));
    }

    @Transactional
    public CourseDtos.LessonResponse addLesson(Long sectionId, CourseDtos.CreateLessonRequest req, String teacherEmail) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Bölmə tapılmadı: " + sectionId));
        verifyOwner(section.getCourse(), teacherEmail);
        int order = req.getOrderIndex() != null ? req.getOrderIndex() : section.getLessons().size() + 1;
        Lesson lesson = Lesson.builder()
                .title(req.getTitle())
                .lessonType(req.getLessonType())
                .contentUrl(req.getContentUrl())
                .textContent(req.getTextContent())
                .durationMinutes(req.getDurationMinutes())
                .orderIndex(order)
                .freePreview(req.isFreePreview())
                .section(section)
                .build();
        return toLessonResponse(lessonRepository.save(lesson));
    }

    // ── Helpers ──

    private Course getAndVerify(Long courseId, String teacherEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Kurs tapılmadı: " + courseId));
        verifyOwner(course, teacherEmail);
        return course;
    }

    private void verifyOwner(Course course, String teacherEmail) {
        if (!course.getTeacher().getEmail().equals(teacherEmail)) {
            throw new RuntimeException("Bu kursa icazəniz yoxdur");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("İstifadəçi tapılmadı: " + email));
    }

    // ── Mappers ──

    private CourseDtos.CourseResponse toCourseResponse(Course c) {
        return CourseDtos.CourseResponse.builder()
                .id(c.getId()).title(c.getTitle()).description(c.getDescription())
                .thumbnailUrl(c.getThumbnailUrl()).accessType(c.getAccessType())
                .price(c.getPrice()).published(c.isPublished())
                .teacherName(c.getTeacher().getFullName()).teacherId(c.getTeacher().getId())
                .sections(c.getSections().stream().map(this::toSectionResponse).collect(Collectors.toList()))
                .createdAt(c.getCreatedAt()).build();
    }

    private CourseDtos.CourseSummary toCourseSummary(Course c) {
        return CourseDtos.CourseSummary.builder()
                .id(c.getId()).title(c.getTitle()).description(c.getDescription())
                .thumbnailUrl(c.getThumbnailUrl()).accessType(c.getAccessType())
                .price(c.getPrice()).published(c.isPublished())
                .teacherName(c.getTeacher().getFullName()).createdAt(c.getCreatedAt()).build();
    }

    private CourseDtos.SectionResponse toSectionResponse(Section s) {
        return CourseDtos.SectionResponse.builder()
                .id(s.getId()).title(s.getTitle()).orderIndex(s.getOrderIndex())
                .lessons(s.getLessons().stream().map(this::toLessonResponse).collect(Collectors.toList()))
                .build();
    }

    private CourseDtos.LessonResponse toLessonResponse(Lesson l) {
        return CourseDtos.LessonResponse.builder()
                .id(l.getId()).title(l.getTitle()).lessonType(l.getLessonType())
                .contentUrl(l.getContentUrl()).textContent(l.getTextContent())
                .durationMinutes(l.getDurationMinutes()).orderIndex(l.getOrderIndex())
                .freePreview(l.isFreePreview()).build();
    }
}
