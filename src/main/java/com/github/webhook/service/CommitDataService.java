package com.github.webhook.service;

import com.github.webhook.model.CommitData;
import com.github.webhook.model.EventType;
import com.github.webhook.model.LessonMaterial;
import com.github.webhook.model.MaterialType;
import com.github.webhook.repository.CommitDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.webhook.repository.LessonMaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Transactional
@Service
public class CommitDataService {

    private final LessonMaterialRepository lessonMaterialRepository;
    private final CommitDataRepository commitDataRepository;
    private final LessonMaterialService lessonMaterialService;


    public CommitDataService(LessonMaterialRepository lessonMaterialRepository, CommitDataRepository commitDataRepository,
                             LessonMaterialService lessonMaterialService) {
        this.lessonMaterialRepository = lessonMaterialRepository;
        this.commitDataRepository = commitDataRepository;
        this.lessonMaterialService = lessonMaterialService;
    }

    /**
     * Обрабатывает данные коммита, полученные через webhook
     * @param jsonNode JSON-данные из webhook
     */
    public void processCommitData(JsonNode jsonNode) {
        String repositoryName = jsonNode.path("repository").path("name").asText();
        String repositoryUrl = jsonNode.path("repository").path("url").asText();
        String ref = jsonNode.path("ref").asText();

        // Обработка коммитов
        jsonNode.path("commits").forEach(commit -> {
            CommitData commitData = createCommitData(commit, repositoryName, repositoryUrl, ref);
            commitDataRepository.save(commitData);

            JsonNode headCommitNode = jsonNode.path("head_commit");
            processCommitFiles(headCommitNode, commitData.getCommitId());
        });
    }

    /**
     * Создает объект CommitData из данных коммита
     * @param commit Данные коммита
     * @param repositoryName Имя репозитория
     * @param repositoryUrl URL репозитория
     * @param ref Ссылка на ветку
     * @return CommitData
     */
    private CommitData createCommitData(JsonNode commit, String repositoryName, String repositoryUrl, String ref) {
        CommitData commitData = new CommitData();
        commitData.setRepositoryName(repositoryName);
        commitData.setRepositoryUrl(repositoryUrl);
        commitData.setRef(ref);
        commitData.setCommitId(commit.path("id").asText());
        commitData.setCommitMessage(commit.path("message").asText());
        commitData.setReceivedAt(LocalDateTime.now());
        commitData.setAddedFiles(commit.path("added").toString());
        commitData.setModifiedFiles(commit.path("modified").toString());
        commitData.setRemovedFiles(commit.path("removed").toString());
        return commitData;
    }

    /**
     * Обрабатывает файлы в коммите (добавленные, измененные, удаленные)
     * @param headCommitNode Данные о коммите
     * @param commitId Идентификатор коммита
     */
    // Метод обработки файлов в коммите
    private void processCommitFiles(JsonNode headCommitNode, String commitId) {
        // Обрабатываем добавленные файлы
        headCommitNode.path("added").forEach(file -> {
            String filePath = file.asText();
            EventType eventType = EventType.ADDED;
            saveLessonMaterialsFromFiles(filePath, commitId, eventType);
        });

        // Обрабатываем измененные файлы
        headCommitNode.path("modified").forEach(file -> {
            String filePath = file.asText();
            EventType eventType = EventType.UPDATED;
            saveLessonMaterialsFromFiles(filePath, commitId, eventType);
        });

        // Обрабатываем удаленные файлы
        headCommitNode.path("removed").forEach(file -> {
            String filePath = file.asText();
            deleteLessonMaterialFromDb(filePath);  // Удаляем файл из базы данных
        });
    }

    // Метод для удаления материала из базы данных
    private void deleteLessonMaterialFromDb(String filePath) {
        String fileName = extractFileName(filePath);
        // Найдем и удалим материал урока по полному пути и имени файла
        Optional<LessonMaterial> lessonMaterialOptional = lessonMaterialRepository.findByFilePathAndFileName(filePath, fileName);
        lessonMaterialOptional.ifPresent(lessonMaterial -> {
            lessonMaterialRepository.deleteByFilePathAndFileName(filePath, fileName);  // Удаление материала из базы данных
        });
    }

    /**
     * Сохраняет материал урока на основе данных о файле
     * @param filePath Путь к файлу
     * @param commitId Идентификатор коммита
     * @param eventType Тип события (ADDED, UPDATED, DELETED)
     */
    private void saveLessonMaterialsFromFiles(String filePath, String commitId, EventType eventType) {
        Long lessonId = getLessonIdForCommit(commitId);
        MaterialType materialType = getMaterialType(filePath);

        if (materialType != null) {
            String fileName = extractFileName(filePath);

            // Если это обновление файла, проверяем, была ли уже добавлена запись с таким файлом
            if (eventType == EventType.UPDATED) {
                // Ищем существующую запись с типом ADDED
                Optional<LessonMaterial> existingAddedMaterial = lessonMaterialRepository.findByFilePathAndFileNameAndEventType(filePath, fileName, EventType.ADDED);

                // Если запись с типом ADDED найдена, удаляем её
                existingAddedMaterial.ifPresent(lessonMaterial -> {
                    lessonMaterialRepository.delete(lessonMaterial);  // Удаляем старую запись с типом ADDED
                });
            }

            // Сохраняем новую запись с типом UPDATED (если файл был изменен)
            lessonMaterialService.saveLessonMaterial(lessonId, filePath, fileName, materialType, eventType);
        }
    }


    /**
     * Определяет тип материала файла по пути.
     *
     * @param filePath Путь к файлу
     * @return Тип материала (LESSON_CODE, HOMEWORK_CODE, CONSULTATION_CODE, PLAN_MD, THEORY_MD, HOMEWORK_MD или SUPPORTING_FILES)
     */
    public static MaterialType getMaterialType(String filePath) {
        String lowerCasePath = filePath.toLowerCase();

        // Проверяем наличие "lesson" в имени директории и "code" в пути
        if (lowerCasePath.matches(".*(?:/.*lesson.*)(?=/).*") && lowerCasePath.matches(".*(?:/.*code.*)(?=/).*")) {
            return MaterialType.LESSON_CODE;
        }
        // Проверяем наличие "homework" в имени директории и "code" в пути
        else if (lowerCasePath.matches(".*(?:/.*homework.*)(?=/).*") && lowerCasePath.matches(".*(?:/.*code.*)(?=/).*")) {
            return MaterialType.HOMEWORK_CODE;
        }
        // Проверяем наличие "consultation" в имени директории и "code" в пути
        else if (lowerCasePath.matches(".*(?:/.*consultation.*)(?=/).*") && lowerCasePath.matches(".*(?:/.*code.*)(?=/).*")) {
            return MaterialType.CONSULTATION_CODE;
        }
        // Проверка на наличие файла "plan.md"
        else if (lowerCasePath.matches(".*(?:/.*plan.*\\.md).*")) {
            return MaterialType.PLAN_MD;
        }
        // Проверка на наличие файла "theory.md"
        else if (lowerCasePath.matches(".*(?:/.*theory.*\\.md).*")) {
            return MaterialType.THEORY_MD;
        }
        // Проверка на наличие файла "homework.md"
        else if (lowerCasePath.matches(".*(?:/.*homework.*\\.md).*")) {
            return MaterialType.HOMEWORK_MD;
        }
        // Если не нашли подходящих условий, возвращаем SUPPORTING_FILES
        else {
            return MaterialType.SUPPORTING_FILES;
        }
    }

    /**
     * Извлекает имя файла из пути
     * @param filePath Путь к файлу
     * @return Имя файла
     */
    private String extractFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    /**
     * Возвращает идентификатор урока для коммита
     * @param commitId Идентификатор коммита
     * @return Идентификатор урока
     */
    private Long getLessonIdForCommit(String commitId) {
        return 1L; // Пример, адаптируйте под вашу логику
    }
}
