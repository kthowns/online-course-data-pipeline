package com.example.pipeline.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope<T> {
    private String id;
    private String source;
    private String type;
    private String subject;
    private LocalDateTime time;
    private String datacontenttype;
    private T data;
    private Map<String, String> attributes;
}
