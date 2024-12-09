package com.github.webhook.service;

import com.github.webhook.model.LessonMaterialUniquePath;
import com.github.webhook.repository.LessonMaterialRepository;
import com.github.webhook.repository.LessonMaterialUniquePathRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LessonMaterialUniquePathService {

    private static final Logger logger = LoggerFactory.getLogger(LessonMaterialUniquePathService.class);

    private final LessonMaterialUniquePathRepository uniquePathRepository;
    private final LessonMaterialRepository lessonMaterialRepository;

    public LessonMaterialUniquePathService(LessonMaterialUniquePathRepository uniquePathRepository,
                                           LessonMaterialRepository lessonMaterialRepository) {
        this.uniquePathRepository = uniquePathRepository;
        this.lessonMaterialRepository = lessonMaterialRepository;
    }

    public Optional<LessonMaterialUniquePath> findById(Long id) {
        return uniquePathRepository.findById(id);
    }

    public Long getOrCreateUniquePathId(Long lessonId, String directoryPath) {
        return uniquePathRepository.findAllByFilePaths(List.of(directoryPath)).stream()
                .findFirst()
                .map(LessonMaterialUniquePath::getId)
                .orElseGet(() -> createUniquePath(directoryPath, lessonId).getId());
    }

    public LessonMaterialUniquePath createUniquePath(String filePath, Long lessonId) {
        LessonMaterialUniquePath newPath = new LessonMaterialUniquePath();
        newPath.setFilePath(filePath);
        newPath.setLessonId(lessonId);
        newPath.setLastModifiedAt(LocalDateTime.now());
        return uniquePathRepository.save(newPath);
    }

    /**
     * Updates the `lastModifiedAt` timestamp for directories when files are added or updated.
     */
    public void updateLastModifiedAtForDirectories(List<String> directories) {
        directories.stream()
                .distinct()
                .forEach(directory -> {
                    Optional<LessonMaterialUniquePath> pathOptional = uniquePathRepository.findByFilePath(directory);
                    if (pathOptional.isPresent()) {
                        LessonMaterialUniquePath path = pathOptional.get();
                        path.setLastModifiedAt(LocalDateTime.now());
                        uniquePathRepository.save(path);
                        logger.info("Updated lastModifiedAt for directory: {}", directory);
                    } else {
                        logger.warn("Directory not found in unique paths: {}", directory);
                    }
                });
    }

    /**
     * Deletes unique paths if no files exist in the corresponding directories.
     * Optimized to minimize database queries.
     */
    public void deleteUniquePathsIfNoFilesExist(List<String> filePaths) {
        List<String> distinctDirectories = filePaths.stream()
                .distinct()
                .toList();

        List<String> nonEmptyDirectories = lessonMaterialRepository.findNonEmptyDirectories(distinctDirectories);

        List<String> emptyDirectories = distinctDirectories.stream()
                .filter(dir -> !nonEmptyDirectories.contains(dir))
                .toList();

        if (!emptyDirectories.isEmpty()) {
            uniquePathRepository.deleteByFilePaths(emptyDirectories);
            emptyDirectories.forEach(dir -> logger.info("Deleted unique path: {}", dir));
        }
    }
}