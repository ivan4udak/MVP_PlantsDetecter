package com.plantidentifier.controller.v1;

import com.plantidentifier.ai.AIAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final JdbcTemplate jdbcTemplate;
    private final AIAdapter    aiAdapter;

    /**
     * GET /api/v1/system/health
     * Публичный — для балансировщика и мониторинга.
     *
     * Возвращает базовый статус: приложение живо?
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status",    "UP");
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("service",   "plant-identifier");

        return ResponseEntity.ok(status);
    }

    /**
     * GET /api/v1/system/health/deep
     * Только ADMIN и SYSTEM.
     *
     * Проверяет все компоненты:
     * - подключение к БД
     * - доступность AI провайдера
     */
    @GetMapping("/health/deep")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<Map<String, Object>> deepHealth() {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().toString());

        // ── Проверка БД ──────────────────────────────
        Map<String, Object> db = new LinkedHashMap<>();
        try {
            // SELECT 1 — минимальный запрос для проверки соединения
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            db.put("status", "UP");
        } catch (Exception e) {
            db.put("status", "DOWN");
            db.put("error",  e.getMessage());
            log.error("DB health check failed: {}", e.getMessage());
        }
        result.put("database", db);

        // ── Проверка AI провайдера ───────────────────
        Map<String, Object> ai = new LinkedHashMap<>();
        try {
            boolean available = aiAdapter.isAvailable();
            ai.put("status",   available ? "UP" : "DOWN");
            ai.put("provider", aiAdapter.getProviderName());
            ai.put("model",    aiAdapter.getModelName());
        } catch (Exception e) {
            ai.put("status", "DOWN");
            ai.put("error",  e.getMessage());
        }
        result.put("aiProvider", ai);

        // Общий статус: UP только если все компоненты UP
        boolean allUp = "UP".equals(db.get("status"))
                && "UP".equals(ai.get("status"));

        result.put("status", allUp ? "UP" : "DEGRADED");

        return ResponseEntity
                .status(allUp ? 200 : 503)
                .body(result);
    }
}