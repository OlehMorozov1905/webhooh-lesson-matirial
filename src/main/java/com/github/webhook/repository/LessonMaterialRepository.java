package com.github.webhook.repository;

import com.github.webhook.model.LessonMaterial;
import com.github.webhook.model.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonMaterialRepository extends JpaRepository<LessonMaterial, Long> {
    List<LessonMaterial> findByLessonIdAndMaterialType(Long lessonId, MaterialType materialType);
}