package com.github.webhook.service;

import com.github.webhook.model.EventType;
import com.github.webhook.model.LessonMaterial;
import com.github.webhook.model.LessonMaterialUniquePath;
import com.github.webhook.model.MaterialType;
import com.github.webhook.repository.LessonMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LessonMaterialService {

    @Autowired
    private LessonMaterialRepository lessonMaterialRepository;

    @Autowired
    private LessonMaterialUniquePathService uniquePathService;

    /**
     * Получить ID уникального пути или создать новый.
     */
    public Long getOrCreateUniquePathId(Long lessonId, String directoryPath) {
        return uniquePathService.findAllByFilePath(directoryPath).stream()
                .findFirst()
                .map(LessonMaterialUniquePath::getId)
                .orElseGet(() -> uniquePathService.createUniquePath(directoryPath, lessonId).getId());
    }

    /**
     * Сохранить учебный материал с привязкой к уникальному пути.
     */
    public void saveLessonMaterial(Long lessonId, String filePath, String fileName, MaterialType materialType, EventType eventType, LocalDateTime uploadedAt) {
        String directoryPath = extractDirectoryPath(filePath);

        // Получаем или создаем уникальный путь
        Long uniquePathId = getOrCreateUniquePathId(lessonId, directoryPath);

        // Подгружаем объект уникального пути
        LessonMaterialUniquePath uniquePath = uniquePathService.findById(uniquePathId)
                .orElseThrow(() -> new RuntimeException("Unique path not found for ID: " + uniquePathId));

        // Создаем или обновляем материал
        LessonMaterial lessonMaterial = new LessonMaterial();
        lessonMaterial.setEventType(eventType);
        lessonMaterial.setFileName(fileName);
        lessonMaterial.setFilePath(filePath);
        lessonMaterial.setMaterialType(materialType);
        lessonMaterial.setUploadedAt(uploadedAt);
        lessonMaterial.setLessonMaterialUniquePath(uniquePath); // Уникальный путь
        lessonMaterial.setLessonId(lessonId);

        lessonMaterialRepository.save(lessonMaterial);
    }

    /**
     * Вспомогательный метод для извлечения директории из пути файла.
     */
    private String extractDirectoryPath(String filePath) {
        int lastSlashIndex = filePath.lastIndexOf('/');
        return lastSlashIndex == -1 ? "" : filePath.substring(0, lastSlashIndex + 1);
    }
}