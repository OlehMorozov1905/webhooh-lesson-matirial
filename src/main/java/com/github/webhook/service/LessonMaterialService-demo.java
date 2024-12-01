//package com.github.webhook.service;
//
//import com.github.webhook.model.LessonMaterial;
//import com.github.webhook.model.LessonMaterialFileDetails;
//import com.github.webhook.repository.LessonMaterialFileDetailsRepository;
//import com.github.webhook.repository.LessonMaterialRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Comparator;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Transactional
//@Service
//public class LessonMaterialService {
//
//    @Autowired
//    private LessonMaterialRepository lessonMaterialRepository;
//
//    @Autowired
//    private LessonMaterialFileDetailsRepository lessonMaterialFileDetailsRepository;
//
//    public void updateLessonMaterial() {
//        // Получаем все записи из LessonMaterialFileDetails
//        List<LessonMaterialFileDetails> files = lessonMaterialFileDetailsRepository.findAll();
//
//        // Группируем файлы по пути и идентификатору урока
//        Map<String, Map<Long, List<LessonMaterialFileDetails>>> groupedByPathAndLesson = files.stream()
//                .collect(Collectors.groupingBy(
//                        file -> {
//                            String filePath = file.getFilePath();
//                            int lastSlashIndex = filePath.lastIndexOf('/');
//                            return (lastSlashIndex == -1) ? filePath : filePath.substring(0, lastSlashIndex + 1);
//                        },
//                        Collectors.groupingBy(LessonMaterialFileDetails::getLessonId)
//                ));
//
//        // Обработка сгруппированных данных
//        for (Map.Entry<String, Map<Long, List<LessonMaterialFileDetails>>> pathEntry : groupedByPathAndLesson.entrySet()) {
//            String path = pathEntry.getKey();
//            Map<Long, List<LessonMaterialFileDetails>> lessonIdMap = pathEntry.getValue();
//
//            // Для каждого урока в этой группе
//            for (Map.Entry<Long, List<LessonMaterialFileDetails>> lessonEntry : lessonIdMap.entrySet()) {
//                Long lessonId = lessonEntry.getKey();
//                List<LessonMaterialFileDetails> pathFiles = lessonEntry.getValue();
//
//                // Проверка на существование записи для пути и урока
//                Optional<LessonMaterial> lessonMaterialOptional = lessonMaterialRepository.findByFilePathAndLessonId(path, lessonId);
//
//                LessonMaterial lessonMaterial;
//                if (lessonMaterialOptional.isPresent()) {
//                    lessonMaterial = lessonMaterialOptional.get();
//                    // Обновляем количество повторений и дату последнего изменения
//                    lessonMaterial.setNumberOfRepetitions(pathFiles.size());
//                    lessonMaterial.setLastModifiedAt(pathFiles.stream()
//                            .max(Comparator.comparing(LessonMaterialFileDetails::getUploadedAt))
//                            .get().getUploadedAt());
//                } else {
//                    lessonMaterial = new LessonMaterial();
//                    lessonMaterial.setFilePath(path);
//                    lessonMaterial.setLessonId(lessonId);
//                    lessonMaterial.setNumberOfRepetitions(pathFiles.size());
//                    lessonMaterial.setLastModifiedAt(pathFiles.stream()
//                            .max(Comparator.comparing(LessonMaterialFileDetails::getUploadedAt))
//                            .get().getUploadedAt());
//                }
//
//                // Сохраняем или обновляем запись
//                lessonMaterialRepository.save(lessonMaterial);
//            }
//        }
//    }
//}
