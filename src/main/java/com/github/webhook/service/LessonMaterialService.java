package com.github.webhook.service;

import com.github.webhook.model.EventType;
import com.github.webhook.model.LessonMaterial;
import com.github.webhook.model.MaterialType;
import com.github.webhook.repository.LessonMaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional
@Service
public class LessonMaterialService {

    private final LessonMaterialRepository repository;

    public LessonMaterialService(LessonMaterialRepository repository) {
        this.repository = repository;
    }

    /**
     * Сохраняет материал урока с учетом типа события (ADDED, UPDATED, DELETED)
     * @param lessonId Идентификатор урока
     * @param filePath Путь к файлу
     * @param fileName Имя файла
     * @param materialType Тип материала (CODE или SUPPORTING_FILES)
     * @param eventType Тип события (ADDED, UPDATED, DELETED)
     * @return Сохраненный объект LessonMaterial
     */
    public LessonMaterial saveLessonMaterial(Long lessonId, String filePath, String fileName, MaterialType materialType, EventType eventType) {
        LessonMaterial material = new LessonMaterial();
        material.setLessonId(lessonId);
        material.setFilePath(filePath);
        material.setFileName(fileName);  // Сохраняем имя файла
        material.setMaterialType(materialType); // Сохраняем тип материала
        material.setEventType(eventType); // Сохраняем тип события
        material.setUploadedAt(LocalDateTime.now());
        return repository.save(material);
    }
}