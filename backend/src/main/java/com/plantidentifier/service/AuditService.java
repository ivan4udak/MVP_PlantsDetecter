package com.plantidentifier.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plantidentifier.entity.ErrorLog;
import com.plantidentifier.entity.AuditSystemEvent;
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

    // JdbcTemplate — прямые SQL запросы когда JPA не удобен
    // Используем для system_events (там нет Entity класса)
    private final JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper;

    /**
     * Пишем ошибку в audit.error_logs.
     *
     * @Async — выполняется в отдельном потоке.
     * Зачем: запись ошибки не должна блокировать
     * основной поток и не должна влиять на HTTP ответ.
     *
     * Propagation.REQUIRES_NEW — своя транзакция.
     * Зачем: если основная транзакция откатилась из-за ошибки,
     * запись в error_log всё равно должна сохраниться!
     */
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
                    // Обрезаем stacktrace — в БД лимит TEXT (~1GB)
                    // но нам достаточно первых 5000 символов
                    .stacktrace(truncate(stacktrace, 5000))
                    .build();

            errorLogRepository.save(errorLog);

        } catch (Exception e) {
            // Если не смогли записать ошибку — хотя бы логируем
            // Никогда не бросаем исключение из error handler!
            log.error("Failed to save error log: {}", e.getMessage());
        }
    }

    /**
     * Пишем системное событие в audit.system_events.
     */
    @Async
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSystemEvent(String eventType,
                               String severity,
                               Object payload) {
        try {
            // Конвертируем payload объект в JSON строку
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
        return text.length() > maxLength
                ? text.substring(0, maxLength)
                : text;
    }
}