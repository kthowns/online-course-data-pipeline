package com.example.pipeline.consumer.repository.analysis;

import com.example.pipeline.common.model.RawEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RawEventLogRepository extends JpaRepository<RawEventLog, Long> {
}
