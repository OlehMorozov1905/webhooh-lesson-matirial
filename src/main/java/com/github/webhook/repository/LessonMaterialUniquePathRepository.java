package com.github.webhook.repository;

import com.github.webhook.model.LessonMaterialUniquePath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonMaterialUniquePathRepository extends JpaRepository<LessonMaterialUniquePath, Long> {

    // Найти запись по пути и ID урока
    Optional<LessonMaterialUniquePath> findByFilePathAndLessonId(String filePath, Long lessonId);

    // Удалить запись по пути и ID урока
    void deleteByFilePathAndLessonId(String filePath, Long lessonId);

    // Получаем все уникальные пути для конкретного урока
    List<LessonMaterialUniquePath> findAllByLessonId(Long lessonId);

    Optional<LessonMaterialUniquePath> findByFilePathAndId(String filePath, Long id);

//    @Query("SELECT l FROM LessonMaterialUniquePath l WHERE :filePath LIKE CONCAT(l.filePath, '%')")
//    Optional<LessonMaterialUniquePath> findByFilePath(@Param("filePath") String filePath);

//    @Query("SELECT l FROM LessonMaterialUniquePath l WHERE l.filePath LIKE CONCAT(:filePath, '%')")
//    Optional<LessonMaterialUniquePath> findByFilePath(@Param("filePath") String filePath);

    @Query("""
    SELECT l FROM LessonMaterialUniquePath l
    WHERE l.filePath IN :filePaths
""")
    List<LessonMaterialUniquePath> findAllByFilePaths(@Param("filePaths") List<String> filePaths);

    Optional<LessonMaterialUniquePath> findById(Long id);
}
