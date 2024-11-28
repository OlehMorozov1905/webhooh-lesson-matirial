package com.github.webhook;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter

@Entity
@Table(name = "lesson_materials")
public class LessonMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false)
    private MaterialType materialType;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    @PreUpdate
    private void formatUploadedAt() {
        if (uploadedAt != null) {
            uploadedAt = uploadedAt.truncatedTo(java.time.temporal.ChronoUnit.SECONDS); // Убираем микросекунды
        }
    }
}
