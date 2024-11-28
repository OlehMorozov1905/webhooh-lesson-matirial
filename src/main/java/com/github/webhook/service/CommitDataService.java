package com.github.webhook.service;

import com.github.webhook.model.CommitData;
import com.github.webhook.model.LessonMaterial;
import com.github.webhook.model.MaterialType;
import com.github.webhook.repository.CommitDataRepository;
import com.github.webhook.repository.LessonMaterialRepository;
import com.github.webhook.util.WebhookUtils;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

@Service
public class CommitDataService {

    private final CommitDataRepository commitDataRepository;
    private final LessonMaterialService lessonMaterialService;

    public CommitDataService(CommitDataRepository commitDataRepository,
                             LessonMaterialRepository lessonMaterialRepository,
                             LessonMaterialService lessonMaterialService) {
        this.commitDataRepository = commitDataRepository;
        this.lessonMaterialService = lessonMaterialService;
    }

    public void processCommitData(JsonNode jsonNode) {
        // Извлекаем информацию о репозитории
        String repositoryName = jsonNode.path("repository").path("name").asText();
        String repositoryUrl = jsonNode.path("repository").path("url").asText();
        String ref = jsonNode.path("ref").asText();

        // Обрабатываем каждый коммит
        JsonNode commitsNode = jsonNode.path("commits");
        commitsNode.forEach(commit -> {
            String commitId = commit.path("id").asText();
            String commitMessage = commit.path("message").asText();

            // Создаем и заполняем объект CommitData
            CommitData commitData = new CommitData();
            commitData.setRepositoryName(repositoryName);
            commitData.setRepositoryUrl(repositoryUrl);
            commitData.setRef(ref);
            commitData.setCommitId(commitId);
            commitData.setCommitMessage(commitMessage);
            commitData.setReceivedAt(LocalDateTime.now()); // Устанавливаем текущее время

            // Сохраняем добавленные, измененные и удаленные файлы
            JsonNode headCommitNode = jsonNode.path("head_commit");
            JsonNode addedFiles = headCommitNode.path("added");
            JsonNode modifiedFiles = headCommitNode.path("modified");
            JsonNode removedFiles = headCommitNode.path("removed");

            commitData.setAddedFiles(addedFiles.toString());
            commitData.setModifiedFiles(modifiedFiles.toString());
            commitData.setRemovedFiles(removedFiles.toString());

            // Сохраняем в базу данных
            commitDataRepository.save(commitData);

            // Сохраняем материалы для добавленных, измененных и удаленных файлов
            saveLessonMaterialsFromFiles(addedFiles, commitId, true);   // true для добавленных файлов
            saveLessonMaterialsFromFiles(modifiedFiles, commitId, false); // false для измененных файлов
        });
    }

    private void saveLessonMaterialsFromFiles(JsonNode files, String commitId, boolean isAdded) {
        files.forEach(file -> {
            String filePath = file.asText();
            Long lessonId = getLessonIdForCommit(commitId);

            // Определяем тип материала (добавленный/измененный/удаленный)
            MaterialType materialType = WebhookUtils.getMaterialType(filePath, isAdded);
            if (materialType != null) {
                lessonMaterialService.saveLessonMaterial(lessonId, filePath, materialType);
            }
        });
    }

    private Long getLessonIdForCommit(String commitId) {
        // Логика для получения ID урока
        return 1L; // Пример, нужно адаптировать под вашу логику
    }
}
