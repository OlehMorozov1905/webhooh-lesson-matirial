package com.github.webhook.service;

import com.github.webhook.model.EventType;
import com.github.webhook.model.LessonMaterial;
import com.github.webhook.model.MaterialType;
import com.github.webhook.repository.LessonMaterialRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CommitDataService {

    private final LessonMaterialRepository lessonMaterialRepository;
    private final LessonMaterialService lessonMaterialService;
    private final LessonMaterialUniquePathService uniquePathService;

    public CommitDataService(LessonMaterialRepository lessonMaterialRepository,
                             LessonMaterialService lessonMaterialService,
                             LessonMaterialUniquePathService uniquePathService) {
        this.lessonMaterialRepository = lessonMaterialRepository;
        this.lessonMaterialService = lessonMaterialService;
        this.uniquePathService = uniquePathService;
    }

    public void processCommitData(JsonNode jsonNode) {
        String repositoryName = jsonNode.path("repository").path("name").asText();
        String repositoryUrl = jsonNode.path("repository").path("url").asText();
        String ref = jsonNode.path("ref").asText();

        jsonNode.path("commits").forEach(commit -> {
            JsonNode headCommitNode = jsonNode.path("head_commit");
            processCommitFiles(headCommitNode);
        });
    }

    private void processCommitFiles(JsonNode headCommitNode) {
        List<String> addedPaths = new ArrayList<>();
        List<String> modifiedPaths = new ArrayList<>();
        List<String> removedPaths = new ArrayList<>();

        // Обрабатываем добавленные файлы
        headCommitNode.path("added").forEach(file -> {
            String filePath = file.asText();
            EventType eventType = EventType.ADDED;
            saveLessonMaterialsFromFiles(filePath, eventType);
            addedPaths.add(filePath);
        });
        headCommitNode.path("modified").forEach(file -> {
            String filePath = file.asText();
            EventType eventType = EventType.UPDATED;
            saveLessonMaterialsFromFiles(filePath, eventType);
            modifiedPaths.add(filePath);
        });

        // Обрабатываем удаленные файлы
        headCommitNode.path("removed").forEach(file -> {
            String filePath = file.asText();
            deleteLessonMaterialFromDb(filePath);
            removedPaths.add(filePath);
        });

        // Обновляем уникальные пути для измененных файлов
        if (!modifiedPaths.isEmpty()) {
            uniquePathService.updateLastModifiedAtForDirectories(modifiedPaths);
        }

        // Удаляем пути, если для них больше нет файлов
        if (!removedPaths.isEmpty()) {
            uniquePathService.deleteUniquePathsIfNoFilesExist(removedPaths);
        }
    }

    private void deleteLessonMaterialFromDb(String filePath) {
        String fileName = extractFileName(filePath);
        Optional<LessonMaterial> lessonMaterialOptional = lessonMaterialRepository.findByFilePathAndFileName(filePath, fileName);
        lessonMaterialOptional.ifPresent(lessonMaterial -> {
            lessonMaterialRepository.delete(lessonMaterial);
        });
    }

    private void saveLessonMaterialsFromFiles(String filePath, EventType eventType) {
        Long lessonId = extractLessonIdFromPath(filePath);
        MaterialType materialType = getMaterialType(filePath);

        if (materialType != null) {
            String fileName = extractFileName(filePath);

            Optional<LessonMaterial> existingMaterial = lessonMaterialRepository.findByFilePathAndFileName(filePath, fileName);

            if (existingMaterial.isPresent()) {
                LessonMaterial lessonMaterial = existingMaterial.get();
                lessonMaterial.setEventType(eventType);
                lessonMaterial.setUploadedAt(LocalDateTime.now());
                lessonMaterialRepository.save(lessonMaterial);
            } else {
                lessonMaterialService.saveLessonMaterial(lessonId, filePath, fileName, materialType, eventType, LocalDateTime.now());
            }
        }
    }

    public static MaterialType getMaterialType(String filePath) {
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

    private String extractFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    private Long extractLessonIdFromPath(String filePath) {
        return 1L; // Пример, адаптируйте под вашу логику
    }
}
