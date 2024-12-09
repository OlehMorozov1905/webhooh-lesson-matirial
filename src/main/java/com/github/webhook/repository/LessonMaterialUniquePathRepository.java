package com.github.webhook.repository;

import com.github.webhook.model.LessonMaterialUniquePath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonMaterialUniquePathRepository extends JpaRepository<LessonMaterialUniquePath, Long> {

    @Query("""
    SELECT l FROM LessonMaterialUniquePath l
    WHERE l.filePath IN :filePaths
""")
    List<LessonMaterialUniquePath> findAllByFilePaths(@Param("filePaths") List<String> filePaths);

    Optional<LessonMaterialUniquePath> findById(Long id);

    @Modifying
    @Query("DELETE FROM LessonMaterialUniquePath lmp WHERE lmp.filePath IN :filePaths")
    void deleteByFilePaths(@Param("filePaths") List<String> filePaths);

    Optional<LessonMaterialUniquePath> findByFilePath(String filePath);

}
