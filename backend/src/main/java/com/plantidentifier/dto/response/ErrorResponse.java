package com.plantidentifier.dto.response;

import java.time.LocalDateTime;

/**
 * Единая модель ошибок для ВСЕХ endpoint-ов.
 *
 * Клиент всегда получает одинаковую структуру —
 * не важно что пошло не так.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        String requestId,     // X-Request-ID из заголовка
        String errorCode,     // "RATE_LIMIT_EXCEEDED", "PLANT_NOT_FOUND"...
        String message,       // человекочитаемое сообщение
        Object details        // доп. информация (null в продакшене)
) {
    // Коды ошибок — константы чтобы не опечататься
    public static final class ErrorCodes {
        public static final String RATE_LIMIT_EXCEEDED   = "RATE_LIMIT_EXCEEDED";
        public static final String PLANT_NOT_FOUND       = "PLANT_NOT_FOUND";
        public static final String USER_ALREADY_EXISTS   = "USER_ALREADY_EXISTS";
        public static final String INVALID_TOKEN         = "INVALID_TOKEN";
        public static final String INVALID_CREDENTIALS   = "INVALID_CREDENTIALS";
        public static final String ACCESS_DENIED         = "ACCESS_DENIED";
        public static final String VALIDATION_ERROR      = "VALIDATION_ERROR";
        public static final String AI_PROVIDER_ERROR     = "AI_PROVIDER_ERROR";
        public static final String INTERNAL_ERROR        = "INTERNAL_ERROR";
    }
}
