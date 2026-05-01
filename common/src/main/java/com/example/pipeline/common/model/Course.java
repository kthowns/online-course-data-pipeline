package com.example.pipeline.common.model;

import com.example.pipeline.common.enums.Level;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teacher;

    @Enumerated(EnumType.STRING)
    private Level level;

    private String category;
    private Long price;
    private Integer totalDuration;
    private LocalDateTime createdAt;
}
