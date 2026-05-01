package com.example.pipeline.consumer.repository.analysis;

import com.example.pipeline.common.model.PaymentAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentAnalysisRepository extends JpaRepository<PaymentAnalysis, Long> {
}
