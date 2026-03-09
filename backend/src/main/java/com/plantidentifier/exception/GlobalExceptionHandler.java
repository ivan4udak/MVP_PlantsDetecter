// exception/GlobalExceptionHandler.java
package com.plantidentifier.exception;

import com.plantidentifier.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Централизованная обработка ВСЕХ исключений.
 *
 * @RestControllerAdvice — перехватывает исключения
 * из ВСЕХ контроллеров и преобразует в HTTP ответ.
 *
 * Без этого класса Spring вернул бы HTML страницу с ошибкой.
 * С ним — всегда возвращаем наш ErrorResponse JSON.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * Достаём X-Request-ID из запроса для трассировки.
     */
    private String getRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        return requestId != null ? requestId : "unknown";
    }

    /**
     * Вспомогательный метод — собирает ErrorResponse.
     */
    private ResponseEntity<ErrorResponse> buildError(
            HttpStatus status,
            String errorCode,
            String message,
            Object details,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                getRequestId(request),
                errorCode,
                message,
                details
        );
        return ResponseEntity.status(status).body(error);
    }

    // ─────────────────────────────────────────────────
    // Наши кастомные исключения
    // ─────────────────────────────────────────────────

    @ExceptionHandler(PlantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlantNotFound(
            PlantNotFoundException ex, HttpServletRequest request) {

        log.debug("Plant not found: {}", ex.getMessage());
        return buildError(
                HttpStatus.NOT_FOUND,
                ErrorResponse.ErrorCodes.PLANT_NOT_FOUND,
                ex.getMessage(),
                null, request
        );
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex, HttpServletRequest request) {

        log.debug("User already exists: {}", ex.getMessage());
        return buildError(
                HttpStatus.CONFLICT,
                ErrorResponse.ErrorCodes.USER_ALREADY_EXISTS,
                ex.getMessage(),
                null, request
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(
            InvalidTokenException ex, HttpServletRequest request) {

        log.debug("Invalid token: {}", ex.getMessage());
        return buildError(
                HttpStatus.UNAUTHORIZED,
                ErrorResponse.ErrorCodes.INVALID_TOKEN,
                ex.getMessage(),
                null, request
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(
            RateLimitExceededException ex, HttpServletRequest request) {

        log.info("Rate limit exceeded for requestId={}",
                getRequestId(request));

        // Добавляем заголовок Retry-After — стандарт RFC 6585
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After",
                        String.valueOf(ex.getRetryAfterSeconds()))
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        getRequestId(request),
                        ErrorResponse.ErrorCodes.RATE_LIMIT_EXCEEDED,
                        ex.getMessage(),
                        Map.of("retryAfterSeconds", ex.getRetryAfterSeconds())
                ));
    }

    // ─────────────────────────────────────────────────
    // Spring & Security исключения
    // ─────────────────────────────────────────────────

    /**
     * Ошибки валидации @Valid.
     * Собираем ВСЕ ошибки полей в один ответ.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Собираем ошибки: { "email": "Неверный формат", ... }
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String field = ((FieldError) error).getField();
                    String msg   = error.getDefaultMessage();
                    fieldErrors.put(field, msg);
                });

        log.debug("Validation error: {}", fieldErrors);
        return buildError(
                HttpStatus.BAD_REQUEST,
                ErrorResponse.ErrorCodes.VALIDATION_ERROR,
                "Ошибка валидации входных данных",
                fieldErrors, request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("Access denied for requestId={}", getRequestId(request));
        return buildError(
                HttpStatus.FORBIDDEN,
                ErrorResponse.ErrorCodes.ACCESS_DENIED,
                "Недостаточно прав для выполнения операции",
                null, request
        );
    }

    /**
     * Всё остальное — 500 Internal Server Error.
     * Логируем полный stacktrace, клиенту даём
     * безопасное сообщение без деталей реализации.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception for requestId={}",
                getRequestId(request), ex);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorResponse.ErrorCodes.INTERNAL_ERROR,
                "Внутренняя ошибка сервера",
                null, request
        );
    }
}