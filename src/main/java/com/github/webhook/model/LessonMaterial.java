package com.github.webhook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lesson_materials")
public class LessonMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_name", nullable = false)
    private String fileName; // новое поле для имени файла

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false)
    private MaterialType materialType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType; // новое поле для типа события

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
