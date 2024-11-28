package com.github.webhook;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LessonMaterialService {
    
    private final LessonMaterialRepository repository;

    public LessonMaterialService(LessonMaterialRepository repository) {
        this.repository = repository;
    }

    public LessonMaterial saveLessonMaterial(Long lessonId, String filePath, MaterialType materialType) {
        LessonMaterial material = new LessonMaterial();
        material.setLessonId(lessonId);
        material.setFilePath(filePath);
        material.setMaterialType(materialType);
        material.setUploadedAt(LocalDateTime.now());
        return repository.save(material);
    }

    public List<LessonMaterial> getMaterialsByLessonIdAndType(Long lessonId, MaterialType materialType) {
        return repository.findByLessonIdAndMaterialType(lessonId, materialType);
    }

    public boolean existsByLessonId(Long lessonId) {
        return repository.existsById(lessonId);
    }

    public LessonMaterial updateFilePathAndTimestamp(Long id, String newPath) {
        LessonMaterial material = repository.findById(id).orElseThrow(() -> new RuntimeException("Material not found"));
        material.setFilePath(newPath);
        material.setUploadedAt(LocalDateTime.now());
        return repository.save(material);
    }
}
