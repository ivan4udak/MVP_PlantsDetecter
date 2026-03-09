// repository/AiUsageStatsRepository.java
package com.plantidentifier.repository;

import com.plantidentifier.entity.AiUsageStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AiUsageStatsRepository
        extends JpaRepository<AiUsageStat, UUID> {

    // Общая стоимость за период (для admin дашборда)
    @Query("""
        SELECT COALESCE(SUM(s.costEstimate), 0)
        FROM AiUsageStat s
        WHERE s.createdDate >= :since
        """)
    BigDecimal sumCostSince(@Param("since") LocalDateTime since);
}