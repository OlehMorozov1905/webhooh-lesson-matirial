package com.github.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final String SECRET_KEY = "your_secret_key"; // Ваш секретный ключ
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class); // Логгер

    private final CommitDataRepository commitDataRepository;
    private final LessonMaterialService lessonMaterialService; // Сервис для работы с материалами уроков

    public WebhookController(CommitDataRepository commitDataRepository, LessonMaterialService lessonMaterialService) {
        this.commitDataRepository = commitDataRepository;
        this.lessonMaterialService = lessonMaterialService;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {
        // Проверка подписи
        if (signature == null || !isValidSignature(payload, signature)) {
            logger.error("Invalid or missing signature");
            return new ResponseEntity<>("Invalid signature", HttpStatus.FORBIDDEN);
        }

        try {
            // Преобразование payload в JsonNode
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(payload);

            // Поле repository (информация о репозитории)
            JsonNode repositoryNode = jsonNode.path("repository");
            String repositoryName = repositoryNode.path("name").asText();
            String repositoryUrl = repositoryNode.path("url").asText();
            logger.info("Repository: " + repositoryName);
            logger.info("Repository URL: " + repositoryUrl);

            // Поле ref (ссылка на ветку)
            String ref = jsonNode.path("ref").asText();
            logger.info("Ref: " + ref);

            // Поле commits (информация о коммитах)
            JsonNode commitsNode = jsonNode.path("commits");
            for (JsonNode commit : commitsNode) {
                String commitId = commit.path("id").asText();
                String message = commit.path("message").asText();
                logger.info("Commit ID: " + commitId + ", Message: " + message);

                // Создаем объект для сохранения в базе данных
                CommitData commitData = new CommitData();
                commitData.setRepositoryName(repositoryName);
                commitData.setRepositoryUrl(repositoryUrl);
                commitData.setRef(ref);
                commitData.setCommitId(commitId);
                commitData.setCommitMessage(message);

                // Сохраняем добавленные, измененные и удаленные файлы
                JsonNode headCommitNode = jsonNode.path("head_commit");
                JsonNode addedFiles = headCommitNode.path("added");
                JsonNode modifiedFiles = headCommitNode.path("modified");
                JsonNode removedFiles = headCommitNode.path("removed");

                commitData.setAddedFiles(addedFiles.toString());
                commitData.setModifiedFiles(modifiedFiles.toString());
                commitData.setRemovedFiles(removedFiles.toString());

                // Сохраняем в базу данных
                commitDataRepository.save(commitData);

                // Обрабатываем файлы для добавления или изменения LessonMaterial
                saveLessonMaterialsFromFiles(addedFiles, commitId, true);  // true для добавленных файлов
                saveLessonMaterialsFromFiles(modifiedFiles, commitId, false);  // false для измененных файлов
            }

        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return new ResponseEntity<>("Error processing webhook", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Webhook received", HttpStatus.OK);
    }

    private void saveLessonMaterialsFromFiles(JsonNode files, String commitId, boolean isAdded) {
        files.forEach(file -> {
            String filePath = file.asText();

            // Пример ID урока, в реальности это может быть извлечено из других данных вебхука или вашей логики
            Long lessonId = getLessonIdForCommit(commitId);

            // Определяем тип материала
            MaterialType materialType = getMaterialType(filePath, isAdded);

            // Если файл добавлен, сохраняем материал
            if (materialType != null) {
                lessonMaterialService.saveLessonMaterial(lessonId, filePath, materialType);
            }
        });
    }


    private Long getLessonIdForCommit(String commitId) {
        // Логика для получения ID урока, например, по commitId
        // В данном случае это может быть просто временный пример:
        return 1L; // Пример, замените реальной логикой
    }

    private boolean isValidSignature(String payload, String signature) {
        try {
            // Генерация подписи с использованием секретного ключа
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] calculatedHash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = "sha256=" + Hex.encodeHexString(calculatedHash);

            // Сравнение с подписью из заголовка
            return signature.equals(calculatedSignature);
        } catch (Exception e) {
            logger.error("Error validating signature", e);
            return false;
        }
    }

    // Метод для определения типа материала
    private MaterialType getMaterialType(String filePath, boolean isAdded) {
        if (isAdded) {
            // Если файл добавлен, проверяем его путь
            if (filePath.toLowerCase().contains("code")) {
                return MaterialType.CODE;
            }
            return MaterialType.SUPPORTING_FILES;
        }
        // Не обрабатываем изменения и удаления файлов
        return null; // Не возвращаем тип материала для изменений
    }

}





















//package com.github.webhook;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.commons.codec.binary.Hex;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//
//@RestController
//@RequestMapping("/webhook")
//public class WebhookController {
//
//    private static final String SECRET_KEY = "your_secret_key"; // Ваш секретный ключ
//    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class); // Логгер
//
//    private final CommitDataRepository commitDataRepository;
//    private final LessonMaterialService lessonMaterialService; // Сервис для работы с материалами уроков
//
//    public WebhookController(CommitDataRepository commitDataRepository, LessonMaterialService lessonMaterialService) {
//        this.commitDataRepository = commitDataRepository;
//        this.lessonMaterialService = lessonMaterialService;
//    }
//
//    @PostMapping
//    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
//                                                @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {
//        // Проверка подписи
//        if (signature == null || !isValidSignature(payload, signature)) {
//            logger.error("Invalid or missing signature");
//            return new ResponseEntity<>("Invalid signature", HttpStatus.FORBIDDEN);
//        }
//
//        try {
//            // Преобразование payload в JsonNode
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode jsonNode = objectMapper.readTree(payload);
//
//            // Поле repository (информация о репозитории)
//            JsonNode repositoryNode = jsonNode.path("repository");
//            String repositoryName = repositoryNode.path("name").asText();
//            String repositoryUrl = repositoryNode.path("url").asText();
//            logger.info("Repository: " + repositoryName);
//            logger.info("Repository URL: " + repositoryUrl);
//
//            // Поле ref (ссылка на ветку)
//            String ref = jsonNode.path("ref").asText();
//            logger.info("Ref: " + ref);
//
//            // Поле commits (информация о коммитах)
//            JsonNode commitsNode = jsonNode.path("commits");
//            for (JsonNode commit : commitsNode) {
//                String commitId = commit.path("id").asText();
//                String message = commit.path("message").asText();
//                logger.info("Commit ID: " + commitId + ", Message: " + message);
//
//                // Создаем объект для сохранения в базе данных
//                CommitData commitData = new CommitData();
//                commitData.setRepositoryName(repositoryName);
//                commitData.setRepositoryUrl(repositoryUrl);
//                commitData.setRef(ref);
//                commitData.setCommitId(commitId);
//                commitData.setCommitMessage(message);
//
//                // Сохраняем добавленные, измененные и удаленные файлы
//                JsonNode headCommitNode = jsonNode.path("head_commit");
//                JsonNode addedFiles = headCommitNode.path("added");
//                JsonNode modifiedFiles = headCommitNode.path("modified");
//                JsonNode removedFiles = headCommitNode.path("removed");
//
//                commitData.setAddedFiles(addedFiles.toString());
//                commitData.setModifiedFiles(modifiedFiles.toString());
//                commitData.setRemovedFiles(removedFiles.toString());
//
//                // Сохраняем в базу данных
//                commitDataRepository.save(commitData);
//
//                // Обрабатываем файлы для добавления или изменения LessonMaterial
//                saveLessonMaterialsFromFiles(addedFiles, commitId, MaterialType.CODE);
//                saveLessonMaterialsFromFiles(modifiedFiles, commitId, MaterialType.SUPPORTING_FILES);
//            }
//
//        } catch (Exception e) {
//            logger.error("Error processing webhook", e);
//            return new ResponseEntity<>("Error processing webhook", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//
//        return new ResponseEntity<>("Webhook received", HttpStatus.OK);
//    }
//
//    private void saveLessonMaterialsFromFiles(JsonNode files, String commitId, MaterialType materialType) {
//        files.forEach(file -> {
//            String filePath = file.asText();
//
//            // Пример ID урока, в реальности это может быть извлечено из других данных вебхука или вашей логики
//            Long lessonId = getLessonIdForCommit(commitId);
//
//            // Сохраняем материал
//            lessonMaterialService.saveLessonMaterial(lessonId, filePath, materialType);
//        });
//    }
//
//    private Long getLessonIdForCommit(String commitId) {
//        // Логика для получения ID урока, например, по commitId
//        // В данном случае это может быть просто временный пример:
//        return 1L; // Пример, замените реальной логикой
//    }
//
//    private boolean isValidSignature(String payload, String signature) {
//        try {
//            // Генерация подписи с использованием секретного ключа
//            Mac mac = Mac.getInstance("HmacSHA256");
//            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
//            mac.init(secretKeySpec);
//            byte[] calculatedHash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
//            String calculatedSignature = "sha256=" + Hex.encodeHexString(calculatedHash);
//
//            // Сравнение с подписью из заголовка
//            return signature.equals(calculatedSignature);
//        } catch (Exception e) {
//            logger.error("Error validating signature", e);
//            return false;
//        }
//    }
//}
