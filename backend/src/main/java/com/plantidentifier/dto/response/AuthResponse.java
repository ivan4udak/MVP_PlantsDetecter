package com.plantidentifier.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Все ответы Auth эндпоинтов в одном файле.
 * Используем вложенные record-ы для организации.
 */
public class AuthResponse {

    // POST /auth/login и результат refresh
    public record TokenPair(
            String accessToken,
            String refreshToken,
            long expiresIn,      // секунды до истечения access token
            String role,
            String language
    ) {}

    // POST /auth/refresh
    public record AccessTokenResponse(
            String accessToken,
            long expiresIn
    ) {}

    // POST /auth/register
    public record RegisterResponse(
            UUID userId,
            LocalDateTime createdDate
    ) {}
}