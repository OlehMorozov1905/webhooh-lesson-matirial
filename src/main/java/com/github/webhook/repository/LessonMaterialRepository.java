package com.github.webhook.repository;


import com.github.webhook.model.LessonMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonMaterialRepository extends JpaRepository<LessonMaterial, Long> {

    // Найдем материал по полному пути и имени файла
    Optional<LessonMaterial> findByFilePathAndFileName(String filePath, String fileName);

    @Query("SELECT DISTINCT lm.filePath FROM LessonMaterial lm WHERE lm.filePath IN :directories")
    List<String> findNonEmptyDirectories(@Param("directories") List<String> directories);


}

