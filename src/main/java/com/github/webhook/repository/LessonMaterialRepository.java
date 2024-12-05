package com.github.webhook.repository;

import com.github.webhook.model.EventType;
import com.github.webhook.model.LessonMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonMaterialRepository extends JpaRepository<LessonMaterial, Long> {

    // Найдем материал по полному пути и имени файла
    Optional<LessonMaterial> findByFilePathAndFileName(String filePath, String fileName);

    // Удалим материал по полному пути
    void deleteByFilePathAndFileName(String filePath, String fileName);

    // Найдем материал по пути, имени файла и типу события (например, ADDED)
    Optional<LessonMaterial> findByFilePathAndFileNameAndEventTypeIn(String filePath, String fileName, List<EventType> eventTypes);

    boolean existsByFilePathAndLessonId(String filePath, Long lessonId);

    @Query("SELECT DISTINCT lm.filePath FROM LessonMaterial lm WHERE lm.lessonId = :lessonId")
    List<String> findDistinctFilePathsByLessonId(@Param("lessonId") Long lessonId);

    // Проверка существования файлов по префиксу пути и идентификатору урока
    @Query("SELECT COUNT(lm) > 0 FROM LessonMaterial lm WHERE lm.filePath LIKE CONCAT(:filePathPrefix, '%') AND lm.lessonId = :lessonId")
    boolean existsByFilePathStartingWithAndLessonId(@Param("filePathPrefix") String filePathPrefix, @Param("lessonId") Long lessonId);

    // Новый метод для подсчета количества файлов с данным путем
    long countByFilePath(String filePath);

}

