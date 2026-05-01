package com.example.pipeline.producer;

import com.example.pipeline.common.enums.EventType;
import com.example.pipeline.common.enums.Gender;
import com.example.pipeline.common.enums.PaymentStatus;
import com.example.pipeline.common.model.EventEnvelope;
import com.example.pipeline.common.model.PaymentData;
import com.example.pipeline.common.model.SearchData;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventGenerator {
    private final StreamPublisher streamPublisher;
    private final Random random = new Random();
    private static final String STREAM_KEY = "lecture-events";

    @Scheduled(fixedRate = 2000)
    public void generateEvent() {
        int choice = random.nextInt(5);
        if (choice == 0) {
            publishSearchEvent();
        } else {
            publishPaymentEvent();
        }
    }

    private void publishSearchEvent() {
        SearchData data = SearchData.builder()
                .keyword("Java Spring")
                .resultCount(random.nextInt(100))
                .viewedCourseId((long) random.nextInt(10) + 1)
                .build();

        EventEnvelope<SearchData> envelope = createEnvelope(EventType.SEARCH.name(), data);
        streamPublisher.publish(STREAM_KEY, envelope);
    }

    private void publishPaymentEvent() {
        PaymentStatus[] statuses = PaymentStatus.values();
        PaymentStatus status = statuses[random.nextInt(statuses.length)];
        
        PaymentData data = PaymentData.builder()
                .userId((long) random.nextInt(100) + 1)
                .courseId((long) random.nextInt(10) + 1)
                .status(status)
                .amount(50000L + random.nextInt(100000))
                .category("Programming")
                .errorCode(status == PaymentStatus.ERROR ? "ERR_100" : null)
                .build();

        EventType type = switch (status) {
            case ADD_TO_CART -> EventType.ADD_TO_CART;
            case INITIATE_CHECKOUT -> EventType.INITIATE_CHECKOUT;
            case PURCHASE -> EventType.PURCHASE_COMPLETED;
            case ERROR -> EventType.PURCHASE_ERRORED;
        };

        EventEnvelope<PaymentData> envelope = createEnvelope(type.name(), data);
        streamPublisher.publish(STREAM_KEY, envelope);
    }

    private <T> EventEnvelope<T> createEnvelope(String type, T data) {
        return EventEnvelope.<T>builder()
                .id(UUID.randomUUID().toString())
                .source("producer-app")
                .type(type)
                .time(LocalDateTime.now())
                .datacontenttype("application/json")
                .data(data)
                .attributes(Map.of(
                        "region", "KR",
                        "platform", random.nextBoolean() ? "MOBILE" : "WEB",
                        "ip", "127.0.0.1",
                        "age", String.valueOf(20 + random.nextInt(30)),
                        "gender", random.nextBoolean() ? Gender.MALE.name() : Gender.FEMALE.name()
                ))
                .build();
    }
}
