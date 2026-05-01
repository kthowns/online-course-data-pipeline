package com.example.pipeline.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchData {
    private String keyword;
    private Integer resultCount;
    private Long viewedCourseId;
}
