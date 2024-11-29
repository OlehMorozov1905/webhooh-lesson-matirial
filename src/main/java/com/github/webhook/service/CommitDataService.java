package com.github.webhook.service;

import com.github.webhook.model.CommitData;
import com.github.webhook.model.MaterialType;
import com.github.webhook.repository.CommitDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommitDataService {

    private final CommitDataRepository commitDataRepository;
    private final LessonMaterialService lessonMaterialService;

    public CommitDataService(CommitDataRepository commitDataRepository,
                             LessonMaterialService lessonMaterialService) {
        this.commitDataRepository = commitDataRepository;
        this.lessonMaterialService = lessonMaterialService;
    }

    public void processCommitData(JsonNode jsonNode) {
        String repositoryName = jsonNode.path("repository").path("name").asText();
        String repositoryUrl = jsonNode.path("repository").path("url").asText();
        String ref = jsonNode.path("ref").asText();

        jsonNode.path("commits").forEach(commit -> {
            CommitData commitData = createCommitData(commit, repositoryName, repositoryUrl, ref);
            commitDataRepository.save(commitData);

            JsonNode headCommitNode = jsonNode.path("head_commit");
            processCommitFiles(headCommitNode, commitData.getCommitId());
        });
    }

    private CommitData createCommitData(JsonNode commit, String repositoryName, String repositoryUrl, String ref) {
        CommitData commitData = new CommitData();
        commitData.setRepositoryName(repositoryName);
        commitData.setRepositoryUrl(repositoryUrl);
        commitData.setRef(ref);
        commitData.setCommitId(commit.path("id").asText());
        commitData.setCommitMessage(commit.path("message").asText());
        commitData.setReceivedAt(LocalDateTime.now());
        commitData.setAddedFiles(commit.path("added").toString());
        commitData.setModifiedFiles(commit.path("modified").toString());
        commitData.setRemovedFiles(commit.path("removed").toString());
        return commitData;
    }

    private void processCommitFiles(JsonNode headCommitNode, String commitId) {
        saveLessonMaterialsFromFiles(headCommitNode.path("added"), commitId, true);
        saveLessonMaterialsFromFiles(headCommitNode.path("modified"), commitId, false);
    }

    private void saveLessonMaterialsFromFiles(JsonNode files, String commitId, boolean isAdded) {
        files.forEach(file -> {
            String filePath = file.asText();
            Long lessonId = getLessonIdForCommit(commitId);

            MaterialType materialType = getMaterialType(filePath, isAdded);
            if (materialType != null) {
                lessonMaterialService.saveLessonMaterial(lessonId, filePath, materialType);
            }
        });
    }

    public static MaterialType getMaterialType(String filePath, boolean isAdded) {
        if (isAdded) {
            return filePath.toLowerCase().contains("code") ? MaterialType.CODE : MaterialType.SUPPORTING_FILES;
        }
        return null;
    }

    private Long getLessonIdForCommit(String commitId) {
        return 1L; // Пример, адаптируйте под вашу логику
    }
}