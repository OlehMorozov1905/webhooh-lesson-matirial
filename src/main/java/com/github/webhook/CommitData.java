package com.github.webhook;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


@Setter
@Getter

@Entity
public class CommitData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repository_name")
    private String repositoryName;

    @Column(name = "repository_url")
    private String repositoryUrl;

    @Column(name = "ref")
    private String ref;

    @Column(name = "commit_id")
    private String commitId;

    @Column(name = "commit_message")
    private String commitMessage;

    @Column(name = "added_files")
    private String addedFiles;

    @Column(name = "modified_files")
    private String modifiedFiles;

    @Column(name = "removed_files")
    private String removedFiles;

    @Column(name = "received_at")
    private LocalDateTime receivedAt = LocalDateTime.now();

    // Метод, который обрезает микросекунды до секунд перед сохранением в базу данных
    @PrePersist
    @PreUpdate
    private void formatReceivedAt() {
        if (receivedAt != null) {
            receivedAt = receivedAt.truncatedTo(ChronoUnit.SECONDS); // Убираем микросекунды
        }
    }

}
