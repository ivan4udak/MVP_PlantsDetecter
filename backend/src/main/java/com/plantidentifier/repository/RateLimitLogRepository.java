// repository/RateLimitLogRepository.java
package com.plantidentifier.repository;

import com.plantidentifier.entity.RateLimitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RateLimitLogRepository
        extends JpaRepository<RateLimitLog, UUID> {
}