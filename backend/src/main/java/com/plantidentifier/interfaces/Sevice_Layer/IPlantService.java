package com.plantidentifier.service;

import com.plantidentifier.dto.request.PlantAnalyzeRequest;
import com.plantidentifier.dto.response.PlantAnalysisResponse;
import com.plantidentifier.dto.response.PlantHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Контракт для работы с анализом растений.
 */
public interface IPlantService {

    /**
     * Главный метод — анализирует изображение через AI.
     *
     * Логика:
     * 1. Проверяем rate limit пользователя
     * 2. Проверяем дедупликацию по image_hash
     * 3. Вызываем AI провайдер
     * 4. Сохраняем результат в plant_requests
     * 5. Сохраняем сырой ответ в plant_raw_responses
     * 6. Сохраняем статистику в ai_usage_stats
     * 7. Возвращаем структурированный ответ
     *
     * POST /api/v1/plants/analyze
     */
    PlantAnalysisResponse analyze(UUID userId, PlantAnalyzeRequest request);

    /**
     * Возвращает историю запросов пользователя.
     * Поддерживает пагинацию: page=0&size=20
     * Только НЕ удалённые записи (is_deleted = false)
     *
     * GET /api/v1/plants/history
     */
    Page<PlantHistoryResponse> getHistory(UUID userId, Pageable pageable);

    /**
     * Возвращает полный результат анализа по ID.
     * Проверяет что запрос принадлежит этому пользователю
     * (или пользователь — ROLE_ADMIN).
     *
     * GET /api/v1/plants/{id}
     */
    PlantAnalysisResponse getById(UUID requestId, UUID userId);

    /**
     * Soft delete: ставит is_deleted = true.
     * Физически данные остаются в БД и в аудите.
     *
     * DELETE /api/v1/plants/{id}
     */
    void delete(UUID requestId, UUID userId);
}