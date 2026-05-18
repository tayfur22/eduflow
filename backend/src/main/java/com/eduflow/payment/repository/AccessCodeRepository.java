package com.eduflow.payment.repository;

import com.eduflow.payment.entity.AccessCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccessCodeRepository extends JpaRepository<AccessCode, Long> {
    Optional<AccessCode> findByCode(String code);
    boolean existsByCode(String code);
    List<AccessCode> findByTeacherId(Long teacherId);
    List<AccessCode> findByCourseId(Long courseId);
}