package com.example.pipeline.common.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_chapters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseChapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long courseId;
    private Integer orderIndex;
    private String title;
    private Integer durationSeconds;
}
