package com.example.pipeline.common.model;

import com.example.pipeline.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentData {
    private Long userId;
    private Long courseId;
    private PaymentStatus status;
    private Long amount;
    private String category;
    private String errorCode;
}
