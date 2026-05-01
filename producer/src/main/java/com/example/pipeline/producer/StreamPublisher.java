package com.example.pipeline.producer;

import com.example.pipeline.common.model.EventEnvelope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamPublisher {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String streamKey, EventEnvelope<?> event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            
            ObjectRecord<String, String> record = StreamRecords.newRecord()
                    .in(streamKey)
                    .ofObject(json);

            redisTemplate.opsForStream().add(record);
            log.info("Published event to stream {}: {}", streamKey, event.getType());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event", e);
        }
    }
}
