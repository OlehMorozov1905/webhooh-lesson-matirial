package com.github.webhook.service;

import com.github.webhook.model.LessonMaterial;
import com.github.webhook.model.MaterialType;
import com.github.webhook.repository.LessonMaterialRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LessonMaterialService {

    private final LessonMaterialRepository repository;

    public LessonMaterialService(LessonMaterialRepository repository) {
        this.repository = repository;
    }

    public LessonMaterial saveLessonMaterial(Long lessonId, String filePath, MaterialType materialType) {
        LessonMaterial material = createLessonMaterial(lessonId, filePath, materialType);
        return repository.save(material);
    }

    private LessonMaterial createLessonMaterial(Long lessonId, String filePath, MaterialType materialType) {
        LessonMaterial material = new LessonMaterial();
        material.setLessonId(lessonId);
        material.setFilePath(filePath);
        material.setMaterialType(materialType);
        material.setUploadedAt(LocalDateTime.now());
        return material;
    }
}