package com.github.webhook;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonMaterialRepository extends JpaRepository<LessonMaterial, Long> {
    List<LessonMaterial> findByLessonIdAndMaterialType(Long lessonId, MaterialType materialType);
}
