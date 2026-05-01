package com.example.pipeline.common.model;

import com.example.pipeline.common.enums.Gender;
import com.example.pipeline.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_analysis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long logId;
    private Long userId;
    private Long courseId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Long amount;
    private String category;
    private String region;
    private String platform;
    private String ipAddress;
    private LocalDateTime eventTime;
    private Integer age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String errorCode;
}
