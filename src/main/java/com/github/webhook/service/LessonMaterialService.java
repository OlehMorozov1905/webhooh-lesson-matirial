package com.github.webhook.service;

import com.github.webhook.model.EventType;
import com.github.webhook.model.LessonMaterial;
import com.github.webhook.model.MaterialType;
import com.github.webhook.repository.LessonMaterialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LessonMaterialService {

    private static final Logger logger = LoggerFactory.getLogger(LessonMaterialService.class);

    private final LessonMaterialRepository lessonMaterialRepository;
    private final LessonMaterialUniquePathService uniquePathService;

    public LessonMaterialService(LessonMaterialRepository lessonMaterialRepository,
                                 LessonMaterialUniquePathService uniquePathService) {
        this.lessonMaterialRepository = lessonMaterialRepository;
        this.uniquePathService = uniquePathService;
    }

    public void processFile(String filePath, EventType eventType) {
        String directoryPath = extractDirectoryPath(filePath);
        String fileName = extractFileName(filePath);
        Long lessonId = extractLessonIdFromPath(filePath);
        MaterialType materialType = determineMaterialType(filePath);

        if (materialType == null) {
            logger.warn("Unable to determine material type for file: {}", filePath);
            return;
        }

        Optional<LessonMaterial> existingMaterial = lessonMaterialRepository.findByFilePathAndFileName(directoryPath, fileName);
        if (existingMaterial.isPresent()) {
            updateExistingMaterial(existingMaterial.get(), eventType);
        } else {
            saveNewMaterial(lessonId, directoryPath, fileName, materialType, eventType);
        }
    }

    public void removeFile(String filePath) {
        String directoryPath = extractDirectoryPath(filePath);
        String fileName = extractFileName(filePath);

        lessonMaterialRepository.findByFilePathAndFileName(directoryPath, fileName).ifPresent(material -> {
            lessonMaterialRepository.delete(material);
            logger.info("Deleted material: {} in {}", fileName, directoryPath);

            // Check if the directory is now empty and remove it if necessary
            uniquePathService.deleteUniquePathsIfNoFilesExist(List.of(directoryPath));
        });
    }

    private void updateExistingMaterial(LessonMaterial material, EventType eventType) {
        material.setEventType(eventType);
        material.setUploadedAt(LocalDateTime.now());
        lessonMaterialRepository.save(material);
        logger.info("Updated material: {} in {}", material.getFileName(), material.getFilePath());
    }

    private void saveNewMaterial(Long lessonId, String directoryPath, String fileName, MaterialType materialType, EventType eventType) {
        Long uniquePathId = uniquePathService.getOrCreateUniquePathId(lessonId, directoryPath);

        LessonMaterial newMaterial = new LessonMaterial();
        newMaterial.setEventType(eventType);
        newMaterial.setFileName(fileName);
        newMaterial.setFilePath(directoryPath);
        newMaterial.setMaterialType(materialType);
        newMaterial.setLessonId(lessonId);
        newMaterial.setUploadedAt(LocalDateTime.now());
        newMaterial.setLessonMaterialUniquePath(uniquePathService.findById(uniquePathId)
                .orElseThrow(() -> new IllegalStateException("Unique path not found for ID: " + uniquePathId)));

        lessonMaterialRepository.save(newMaterial);
        logger.info("Saved new material: {} in {}", fileName, directoryPath);
    }

    public String extractFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf('/') + 1);
    }

    public String extractDirectoryPath(String filePath) {
        return filePath.substring(0, filePath.lastIndexOf('/') + 1);
    }

    private Long extractLessonIdFromPath(String filePath) {
        return 1L; // TODO: Replace with actual logic to extract lesson ID if required
    }

    private MaterialType determineMaterialType(String filePath) {
        String lowerCasePath = filePath.toLowerCase();

        if (lowerCasePath.matches(".*(?:/.*lesson.*)(?=/).*") && lowerCasePath.matches(".*(?:/.*code.*)(?=/).*")) {
            return MaterialType.LESSON_CODE;
        } else if (lowerCasePath.matches(".*(?:/.*homework.*)(?=/).*") && lowerCasePath.matches(".*(?:/.*code.*)(?=/).*")) {
            return MaterialType.HOMEWORK_CODE;
        } else if (lowerCasePath.matches(".*(?:/.*consultation.*)(?=/).*") && lowerCasePath.matches(".*(?:/.*code.*)(?=/).*")) {
            return MaterialType.CONSULTATION_CODE;
        } else if (lowerCasePath.matches(".*(?:/.*plan.*\\.md).*")) {
            return MaterialType.PLAN_MD;
        } else if (lowerCasePath.matches(".*(?:/.*theory.*\\.md).*")) {
            return MaterialType.THEORY_MD;
        } else if (lowerCasePath.matches(".*(?:/.*homework.*\\.md).*")) {
            return MaterialType.HOMEWORK_MD;
        } else {
            return MaterialType.SUPPORTING_FILES;
        }
    }
}