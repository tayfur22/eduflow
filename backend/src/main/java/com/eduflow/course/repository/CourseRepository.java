package com.eduflow.course.repository;

import com.eduflow.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByTeacherId(Long teacherId);

    List<Course> findByPublishedTrue();

    List<Course> findByTeacherIdAndPublishedTrue(Long teacherId);

    @Query("SELECT c FROM Course c WHERE c.published = true AND " +
            "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Course> searchByTitle(@Param("keyword") String keyword);
}