package com.github.webhook.service;

import com.github.webhook.model.LessonMaterialUniquePath;
import com.github.webhook.repository.LessonMaterialRepository;
import com.github.webhook.repository.LessonMaterialUniquePathRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LessonMaterialUniquePathService {

    @Autowired
    private LessonMaterialUniquePathRepository uniquePathRepository;

    @Autowired
    private LessonMaterialRepository lessonMaterialRepository;

    public List<LessonMaterialUniquePath> findAllByFilePath(String filePath) {
        return uniquePathRepository.findAllByFilePaths(List.of(filePath));
    }

    public Optional<LessonMaterialUniquePath> findById(Long id) {
        return uniquePathRepository.findById(id);
    }

    // Метод для создания уникального пути
    public LessonMaterialUniquePath createUniquePath(String filePath, Long lessonId) {
        LessonMaterialUniquePath newPath = new LessonMaterialUniquePath();
        newPath.setFilePath(filePath);
        newPath.setLessonId(lessonId); // Используем реальный lessonId
        newPath.setLastModifiedAt(java.time.LocalDateTime.now());
        return uniquePathRepository.save(newPath);
    }

    /**
     * Обновить записи для файлов в нескольких путях.
     */
    public void updateLastModifiedAtForDirectories(List<String> filePaths) {

        // Применяем метод экстракта пути для каждого элемента в filePaths
        List<String> directories = filePaths.stream()
                .map(this::extractPathWithoutFile)  // экстрактируем пути без имени файла
                .collect(Collectors.toList());

        // Находим все записи для указанных директорий
        List<LessonMaterialUniquePath> uniquePaths = uniquePathRepository.findAllByFilePaths(directories);

        // Обновляем last_modified_at для всех найденных путей
        uniquePaths.forEach(uniquePath -> uniquePath.setLastModifiedAt(LocalDateTime.now()));

        // Сохраняем обновленные записи
        uniquePathRepository.saveAll(uniquePaths);
    }

    /**
     * Удалить записи для путей, если все файлы из этих путей больше не существуют.
     */
    public void deleteUniquePathsIfNoFilesExist(List<String> filePaths) {
        System.out.println(filePaths);

        // Применяем метод экстракта пути для каждого элемента в filePaths
        List<String> directories = filePaths.stream()
                .map(this::extractPathWithoutFile)
                .collect(Collectors.toList());
        System.out.println(directories);

        // Группируем пути (директории) и считаем количество файлов для каждого
        Map<String, Long> fileCounts = directories.stream()
                .collect(Collectors.toMap(
                        path -> path,
                        path -> lessonMaterialRepository.countByFilePath(path),
                        (existing, replacement) -> existing // Или (existing, replacement) -> replacement, если хотите заменить
                ));

        // Находим все записи для заданных путей
        List<LessonMaterialUniquePath> uniquePaths = uniquePathRepository.findAllByFilePaths(directories);

        // Отбираем пути, для которых файлов нет
        List<LessonMaterialUniquePath> pathsToDelete = uniquePaths.stream()
                .filter(path -> fileCounts.getOrDefault(path.getFilePath(), 0L) == 0)
                .collect(Collectors.toList());

        if (pathsToDelete.isEmpty()) {
            System.out.println("No unique paths to delete.");
            return;
        }

        // Удаляем записи
        uniquePathRepository.deleteAll(pathsToDelete);
    }

    public String extractPathWithoutFile(String filePath) {
        int lastSlashIndex = filePath.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return filePath; // Если нет слэша, возвращаем сам путь
        }
        return filePath.substring(0, lastSlashIndex + 1);
    }
}
