package com.example.pipeline.consumer.repository.analysis;

import com.example.pipeline.common.model.SearchAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchAnalysisRepository extends JpaRepository<SearchAnalysis, Long> {
}
