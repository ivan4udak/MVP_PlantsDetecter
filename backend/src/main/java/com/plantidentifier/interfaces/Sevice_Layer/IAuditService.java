package com.plantidentifier.service;

import com.plantidentifier.entity.ErrorLog;

import java.util.UUID;

/**
 * Контракт для записи в audit схему.
 *
 * Большинство аудита происходит через PostgreSQL триггеры
 * автоматически. Этот сервис — для случаев когда нужно
 * записать аудит явно из Java кода:
 * - ошибки (error_logs)
 * - системные события (system_events)
 */
public interface IAuditService {

    /**
     * Записывает ошибку в audit.error_logs.
     * Вызывается из GlobalExceptionHandler.
     */
    void logError(UUID requestId, String errorCode,
                  String message, String stacktrace);

    /**
     * Записывает системное событие в audit.system_events.
     * Например: старт приложения, смена AI провайдера.
     */
    void logSystemEvent(String eventType, String severity, Object payload);
}