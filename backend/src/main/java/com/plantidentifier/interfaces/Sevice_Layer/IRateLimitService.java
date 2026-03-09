package com.plantidentifier.service;

import com.plantidentifier.entity.User;

import java.util.UUID;

/**
 * Контракт для проверки и управления rate limiting.
 *
 * Текущая реализация: подсчёт в PostgreSQL.
 * Будущая реализация: Redis sliding window.
 * Благодаря интерфейсу — меняем реализацию без изменения PlantService!
 */
public interface IRateLimitService {

    /**
     * Проверяет не превысил ли пользователь лимит.
     * Если превысил — бросает RateLimitExceededException.
     *
     * Лимиты:
     *   ROLE_GUEST: 3 запроса в день
     *   ROLE_USER:  30 запросов в день (configurable)
     *   ROLE_ADMIN: без лимита
     */
    void checkLimit(User user, String endpoint);

    /**
     * Возвращает сколько запросов осталось у пользователя сегодня.
     */
    int getRemainingRequests(UUID userId);

    /**
     * Сбрасывает лимит пользователя (только ROLE_ADMIN).
     *
     * POST /api/v1/admin/rate-limit/reset/{userId}
     */
    void resetLimit(UUID userId);
}