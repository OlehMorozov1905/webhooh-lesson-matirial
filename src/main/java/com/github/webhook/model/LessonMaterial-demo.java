//package com.github.webhook.model;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "lesson_material")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//public class LessonMaterial {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "file_path", nullable = false)
//    private String filePath;
//
//    @Column(name = "lesson_id", nullable = false)
//    private Long lessonId;
//
//    @Column(name = "number_of_repetitions", nullable = false)
//    private Integer numberOfRepetitions;
//
//    @Column(name = "last_modified_at", nullable = false)
//    private LocalDateTime lastModifiedAt;
//
//}
