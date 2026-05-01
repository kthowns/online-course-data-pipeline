package com.example.pipeline.consumer;

import com.example.pipeline.common.enums.EventType;
import com.example.pipeline.common.enums.Gender;
import com.example.pipeline.common.model.*;
import com.example.pipeline.consumer.repository.analysis.PaymentAnalysisRepository;
import com.example.pipeline.consumer.repository.analysis.RawEventLogRepository;
import com.example.pipeline.consumer.repository.analysis.SearchAnalysisRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventConsumerService {
    private final ObjectMapper objectMapper;
    private final RawEventLogRepository rawEventLogRepository;
    private final PaymentAnalysisRepository paymentAnalysisRepository;
    private final SearchAnalysisRepository searchAnalysisRepository;

    @Transactional("analysisTransactionManager")
    public void processEvent(String json) {
        try {
            // 1. Extract & Raw Save (Analysis DB)
            RawEventLog rawLog = RawEventLog.builder()
                    .data(json)
                    .createdAt(LocalDateTime.now())
                    .build();
            rawLog = rawEventLogRepository.save(rawLog);

            // 2. Transform
            EventEnvelope<Object> envelope = objectMapper.readValue(json, new TypeReference<>() {});
            String type = envelope.getType();

            if (type.equals(EventType.SEARCH.name())) {
                processSearch(envelope, rawLog.getId());
            } else {
                processPayment(envelope, rawLog.getId());
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to process event", e);
        }
    }

    private void processSearch(EventEnvelope<Object> envelope, Long logId) {
        SearchData data = objectMapper.convertValue(envelope.getData(), SearchData.class);
        
        SearchAnalysis analysis = SearchAnalysis.builder()
                .logId(logId)
                .userId(0L) // Simplified
                .keyword(data.getKeyword())
                .resultCount(data.getResultCount())
                .viewedCourseId(data.getViewedCourseId())
                .age(Integer.parseInt(envelope.getAttributes().get("age")))
                .gender(Gender.valueOf(envelope.getAttributes().get("gender")))
                .eventTime(envelope.getTime())
                .build();
        
        searchAnalysisRepository.save(analysis);
        log.info("Saved search analysis to analysis_db for log {}", logId);
    }

    private void processPayment(EventEnvelope<Object> envelope, Long logId) {
        PaymentData data = objectMapper.convertValue(envelope.getData(), PaymentData.class);

        PaymentAnalysis analysis = PaymentAnalysis.builder()
                .logId(logId)
                .userId(data.getUserId())
                .courseId(data.getCourseId())
                .status(data.getStatus())
                .amount(data.getAmount())
                .category(data.getCategory())
                .region(envelope.getAttributes().get("region"))
                .platform(envelope.getAttributes().get("platform"))
                .ipAddress(envelope.getAttributes().get("ip"))
                .eventTime(envelope.getTime())
                .age(Integer.parseInt(envelope.getAttributes().get("age")))
                .gender(Gender.valueOf(envelope.getAttributes().get("gender")))
                .errorCode(data.getErrorCode())
                .build();

        paymentAnalysisRepository.save(analysis);
        log.info("Saved payment analysis to analysis_db for log {}", logId);
    }
}
