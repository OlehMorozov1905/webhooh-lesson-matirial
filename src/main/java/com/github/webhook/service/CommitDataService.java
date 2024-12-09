package com.github.webhook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.webhook.model.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CommitDataService {

    private static final Logger logger = LoggerFactory.getLogger(CommitDataService.class);

    private final LessonMaterialService lessonMaterialService;
    private final LessonMaterialUniquePathService uniquePathService;

    public CommitDataService(LessonMaterialService lessonMaterialService,
                             LessonMaterialUniquePathService uniquePathService) {
        this.lessonMaterialService = lessonMaterialService;
        this.uniquePathService = uniquePathService;
    }

    public void processCommitData(JsonNode jsonNode) {
        logger.info("Processing commit data for repository: {}", jsonNode.path("repository").path("name").asText());
        jsonNode.path("commits").forEach(commit -> {
            JsonNode headCommitNode = jsonNode.path("head_commit");
            processCommitFiles(headCommitNode);
        });
    }

    private void processCommitFiles(JsonNode headCommitNode) {
        logger.info("Processing commit files for commit: {}", headCommitNode.path("id").asText());

        // Collect directories for updating timestamps
        List<String> directoriesToUpdate = new ArrayList<>();

        // Process added files
        headCommitNode.path("added").forEach(fileNode -> {
            String filePath = fileNode.asText();
            try {
                lessonMaterialService.processFile(filePath, EventType.ADDED);
                directoriesToUpdate.add(lessonMaterialService.extractDirectoryPath(filePath));
            } catch (Exception e) {
                logger.error("Error processing added file '{}': {}", filePath, e.getMessage(), e);
            }
        });

        // Process modified files
        headCommitNode.path("modified").forEach(fileNode -> {
            String filePath = fileNode.asText();
            try {
                lessonMaterialService.processFile(filePath, EventType.UPDATED);
                directoriesToUpdate.add(lessonMaterialService.extractDirectoryPath(filePath));
            } catch (Exception e) {
                logger.error("Error processing modified file '{}': {}", filePath, e.getMessage(), e);
            }
        });

        // Update directories timestamps
        if (!directoriesToUpdate.isEmpty()) {
            uniquePathService.updateLastModifiedAtForDirectories(directoriesToUpdate);
        }

        // Process removed files
        headCommitNode.path("removed").forEach(fileNode -> {
            String filePath = fileNode.asText();
            try {
                lessonMaterialService.removeFile(filePath);
            } catch (Exception e) {
                logger.error("Error processing removed file '{}': {}", filePath, e.getMessage(), e);
            }
        });
    }
}