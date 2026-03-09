package com.plantidentifier.controller.v1;

import com.plantidentifier.repository.AiUsageStatsRepository;
import com.plantidentifier.repository.ErrorLogRepository;
import com.plantidentifier.repository.PlantRequestRepository;
import com.plantidentifier.service.IRateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Все методы требуют ROLE_ADMIN.
 * @PreAuthorize на уровне класса — применяется ко ВСЕМ методам.
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final IRateLimitService      rateLimitService;
    private final AiUsageStatsRepository aiUsageStatsRepository;
    private final PlantRequestRepository plantRequestRepository;
    private final ErrorLogRepository     errorLogRepository;

    /**
     * GET /api/v1/admin/stats/usage
     * Статистика использования системы.
     */
    @GetMapping("/stats/usage")
    public ResponseEntity<Map<String, Object>> getUsageStats() {

        log.debug("GET /admin/stats/usage");

        Map<String, Object> stats = new LinkedHashMap<>();

        // Всего запросов за сегодня
        long todayRequests = plantRequestRepository
                .countByUserIdSince(
                        null,  // все пользователи
                        LocalDateTime.now().toLocalDate().atStartOfDay()
                );

        stats.put("todayRequests", todayRequests);
        stats.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/v1/admin/stats/ai-cost
     * Стоимость AI запросов за текущий месяц.
     */
    @GetMapping("/stats/ai-cost")
    public ResponseEntity<Map<String, Object>> getAiCost() {

        log.debug("GET /admin/stats/ai-cost");

        // Начало текущего месяца
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);

        BigDecimal cost = aiUsageStatsRepository
                .sumCostSince(startOfMonth);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("costUSD",   cost);
        result.put("since",     startOfMonth.toString());
        result.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/admin/rate-limit/{userId}
     * Сколько запросов осталось у пользователя.
     */
    @GetMapping("/rate-limit/{userId}")
    public ResponseEntity<Map<String, Object>> getRateLimit(
            @PathVariable UUID userId) {

        log.debug("GET /admin/rate-limit/{}", userId);

        int remaining = rateLimitService.getRemainingRequests(userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId",    userId);
        result.put("remaining", remaining);
        // -1 означает "без ограничений" (ADMIN/SYSTEM)
        result.put("unlimited", remaining == -1);

        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/admin/rate-limit/reset/{userId}
     * Сброс лимита пользователя.
     */
    @PostMapping("/rate-limit/reset/{userId}")
    public ResponseEntity<Void> resetRateLimit(
            @PathVariable UUID userId) {

        log.info("POST /admin/rate-limit/reset/{}", userId);

        rateLimitService.resetLimit(userId);

        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/v1/admin/errors
     * Последние ошибки из error_logs.
     */
    @GetMapping("/errors")
    public ResponseEntity<Map<String, Object>> getErrors() {

        log.debug("GET /admin/errors");

        // Последние 50 ошибок
        var errors = errorLogRepository.findAll(
                org.springframework.data.domain.PageRequest.of(
                        0, 50,
                        org.springframework.data.domain.Sort
                                .by("createdDate").descending()
                )
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("errors",    errors.getContent());
        result.put("total",     errors.getTotalElements());
        result.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(result);
    }
}