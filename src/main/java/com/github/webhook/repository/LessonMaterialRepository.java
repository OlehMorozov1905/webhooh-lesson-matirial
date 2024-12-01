package com.github.webhook.repository;

import com.github.webhook.model.LessonMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LessonMaterialRepository extends JpaRepository<LessonMaterial, Long> {

    // Найдем материал по полному пути и имени файла
    Optional<LessonMaterial> findByFilePathAndFileName(String filePath, String fileName);

    // Удалим материал по полному пути
    void deleteByFilePathAndFileName(String filePath, String fileName);
}
