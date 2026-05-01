package com.example.pipeline.common.model;

import com.example.pipeline.common.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_analysis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long logId;
    private Long userId;
    private String keyword;
    private Integer resultCount;
    private Long viewedCourseId;
    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDateTime eventTime;
}
