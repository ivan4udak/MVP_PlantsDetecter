package com.plantidentifier.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plantidentifier.entity.ErrorLog;
import com.plantidentifier.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService implements IAuditService {

    private final ErrorLogRepository errorLogRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Async
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logError(UUID requestId, String errorCode,
                         String message, String stacktrace) {
        try {
            ErrorLog errorLog = ErrorLog.builder()
                    .requestId(requestId)
                    .errorCode(errorCode)
                    .message(message)
                    .stacktrace(truncate(stacktrace, 5000))
                    .build();
            errorLogRepository.save(errorLog);
        } catch (Exception e) {
            log.error("Failed to save error log: {}", e.getMessage());
        }
    }

    @Async
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSystemEvent(String eventType, String severity, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            jdbcTemplate.update("""
                INSERT INTO audit.system_events
                    (event_type, severity, payload, created_date)
                VALUES (?, ?::varchar, ?::jsonb, NOW())
                """,
                    eventType, severity, payloadJson
            );
        } catch (Exception e) {
            log.error("Failed to save system event: {}", e.getMessage());
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return null;
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}