package com.plantidentifier.security;

import com.plantidentifier.entity.User;

import java.util.UUID;

/**
 * Контракт для работы с JWT токенами.
 *
 * Access token:  короткоживущий (15 мин) — для запросов
 * Refresh token: долгоживущий (7 дней) — только для обновления
 */
public interface IJwtService {

    /**
     * Создаёт access token для пользователя.
     * Внутри токена (claims):
     *   - sub: userId
     *   - role: ROLE_USER / ROLE_ADMIN / ...
     *   - lang: preferred_language
     *   - type: "access"
     */
    String generateAccessToken(User user);

    /**
     * Создаёт refresh token.
     * Refresh token содержит минимум данных —
     * только userId и тип.
     */
    String generateRefreshToken(User user);

    /**
     * Извлекает userId из токена.
     * Не проверяет подпись — используй validateToken() сначала!
     */
    UUID extractUserId(String token);

    /**
     * Извлекает роль из токена.
     * Например: "ROLE_USER"
     */
    String extractRole(String token);

    /**
     * Извлекает язык из токена.
     * Например: "ru"
     */
    String extractLanguage(String token);

    /**
     * Проверяет подпись и срок действия токена.
     *
     * @return true если токен валидный
     * @throws InvalidTokenException если токен невалидный
     */
    boolean validateToken(String token);

    /**
     * Проверяет что токен является access token.
     * Refresh token нельзя использовать для запросов!
     */
    boolean isAccessToken(String token);

    /**
     * Возвращает время жизни access token в миллисекундах.
     */
    long getAccessTokenExpiration();
}