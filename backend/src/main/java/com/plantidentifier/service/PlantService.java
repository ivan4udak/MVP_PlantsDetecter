package com.plantidentifier.service;

import com.plantidentifier.ai.AIAdapter;
import com.plantidentifier.dto.request.PlantAnalyzeRequest;
import com.plantidentifier.dto.response.PlantAnalysisResponse;
import com.plantidentifier.dto.response.PlantHistoryResponse;
import com.plantidentifier.entity.AiUsageStat;
import com.plantidentifier.entity.PlantRawResponse;
import com.plantidentifier.entity.PlantRequest;
import com.plantidentifier.entity.User;
import com.plantidentifier.exception.InvalidTokenException;
import com.plantidentifier.exception.PlantNotFoundException;
import com.plantidentifier.repository.AiUsageStatsRepository;
import com.plantidentifier.repository.PlantRawResponseRepository;
import com.plantidentifier.repository.PlantRequestRepository;
import com.plantidentifier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlantService implements IPlantService {

    private final PlantRequestRepository    plantRequestRepository;
    private final PlantRawResponseRepository rawResponseRepository;
    private final AiUsageStatsRepository    aiUsageStatsRepository;
    private final UserRepository            userRepository;
    private final IRateLimitService         rateLimitService;
    private final AIAdapter                 aiAdapter;

    // ─────────────────────────────────────────────────
    // Analyze — главный метод
    // ─────────────────────────────────────────────────

    @Override
    @Transactional
    public PlantAnalysisResponse analyze(UUID userId,
                                         PlantAnalyzeRequest request) {

        log.info("Plant analysis request: userId={}, imageUrl={}",
                userId, request.imageUrl());

        // 1. Загружаем пользователя
        User user = userRepository
                .findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() ->
                        new InvalidTokenException("Пользователь не найден")
                );

        // 2. Проверяем rate limit
        // Если превышен — бросает RateLimitExceededException
        rateLimitService.checkLimit(user, "/plants/analyze");

        // 3. Дедупликация по хэшу изображения
        // Если то же изображение уже анализировали — возвращаем
        // кэшированный результат без вызова AI
        String imageHash = computeImageHash(request.imageUrl());
        var cached = plantRequestRepository
                .findByImageHashAndIsDeletedFalse(imageHash);

        if (cached.isPresent()) {
            log.info("Cache hit for imageHash={}, returning cached result",
                    imageHash);
            return mapToAnalysisResponse(cached.get());
        }

        // 4. Вызываем AI провайдер
        long startTime = Instant.now().toEpochMilli();

        AIAdapter.AIAnalysisResult aiResult = aiAdapter.analyzeImage(
                request.imageUrl(),
                user.getPreferredLanguage()
        );

        long processingTime = Instant.now().toEpochMilli() - startTime;

        log.info("AI analysis complete: provider={}, model={}, time={}ms",
                aiAdapter.getProviderName(),
                aiAdapter.getModelName(),
                processingTime
        );

        // 5. Сохраняем результат в plant_requests
        PlantRequest plantRequest = buildPlantRequest(
                user, request, aiResult, imageHash, processingTime
        );
        PlantRequest saved = plantRequestRepository.save(plantRequest);

        // 6. Сохраняем сырой ответ AI
        savRawResponse(saved, aiResult);

        // 7. Сохраняем статистику использования AI
        saveAiStats(saved, aiResult);

        log.info("Plant analysis saved: requestId={}, isPlant={}, " +
                        "confidence={}",
                saved.getId(), aiResult.isPlant(), aiResult.confidence()
        );

        return mapToAnalysisResponse(saved, aiResult);
    }

    // ─────────────────────────────────────────────────
    // History
    // ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<PlantHistoryResponse> getHistory(UUID userId,
                                                 Pageable pageable) {
        log.debug("Getting history for userId={}, page={}",
                userId, pageable.getPageNumber());

        return plantRequestRepository
                .findByUserIdAndIsDeletedFalseOrderByCreatedDateDesc(
                        userId, pageable
                )
                .map(this::mapToHistoryResponse);
    }

    // ─────────────────────────────────────────────────
    // Get By ID
    // ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PlantAnalysisResponse getById(UUID requestId, UUID userId) {
        PlantRequest request = plantRequestRepository
                .findByIdAndIsDeletedFalse(requestId)
                .orElseThrow(() ->
                        new PlantNotFoundException(
                                "Запрос не найден: " + requestId
                        )
                );

        // Проверяем что запрос принадлежит этому пользователю
        // (ADMIN может смотреть любые — проверяется через @PreAuthorize)
        if (!request.getUser().getId().equals(userId)) {
            throw new PlantNotFoundException(
                    "Запрос не найден: " + requestId
            );
            // Специально возвращаем NOT_FOUND а не FORBIDDEN —
            // не раскрываем что запрос существует у другого юзера
        }

        return mapToAnalysisResponse(request);
    }

    // ─────────────────────────────────────────────────
    // Delete (soft)
    // ─────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(UUID requestId, UUID userId) {
        PlantRequest request = plantRequestRepository
                .findByIdAndIsDeletedFalse(requestId)
                .orElseThrow(() ->
                        new PlantNotFoundException(
                                "Запрос не найден: " + requestId
                        )
                );

        if (!request.getUser().getId().equals(userId)) {
            throw new PlantNotFoundException(
                    "Запрос не найден: " + requestId
            );
        }

        // Soft delete — просто ставим флаг
        request.setIsDeleted(true);
        request.setUpdatedBy(userId.toString());

        plantRequestRepository.save(request);

        log.info("Plant request soft deleted: requestId={}, userId={}",
                requestId, userId);
    }

    // ─────────────────────────────────────────────────
    // Приватные вспомогательные методы
    // ─────────────────────────────────────────────────

    /**
     * MD5 хэш URL изображения.
     * Используется для дедупликации.
     * MD5 достаточно для этой цели — не для криптографии.
     */
    private String computeImageHash(String imageUrl) {
        return DigestUtils.md5DigestAsHex(imageUrl.getBytes());
    }

    private PlantRequest buildPlantRequest(
            User user,
            PlantAnalyzeRequest request,
            AIAdapter.AIAnalysisResult aiResult,
            String imageHash,
            long processingTime) {

        PlantRequest.PlantRequestBuilder builder = PlantRequest.builder()
                .user(user)
                .imageUrl(request.imageUrl())
                .imageHash(imageHash)
                .isPlant(aiResult.isPlant())
                .confidence(BigDecimal.valueOf(aiResult.confidence()))
                .aiProvider(aiAdapter.getProviderName())
                .modelName(aiAdapter.getModelName())
                .processingTimeMs((int) processingTime)
                .createdBy(user.getId().toString())
                .updatedBy(user.getId().toString());

        // Геолокация — если передали
        if (request.latitude() != null) {
            builder.latitude(request.latitude());
        }
        if (request.longitude() != null) {
            builder.longitude(request.longitude());
        }

        // Данные о растении — если это действительно растение
        if (aiResult.isPlant() && aiResult.primaryResult() != null) {
            AIAdapter.PlantInfo plant = aiResult.primaryResult();
            builder
                    .primaryName(plant.name())
                    .family(plant.family())
                    .rarity(plant.rarity())
                    .habitat(plant.habitat());
        }

        return builder.build();
    }

    private void savRawResponse(PlantRequest saved,
                                AIAdapter.AIAnalysisResult aiResult) {
        try {
            PlantRawResponse rawResponse = PlantRawResponse.builder()
                    .request(saved)
                    .rawJson(aiResult.rawResponse())
                    .createdBy(saved.getUser().getId().toString())
                    .updatedBy(saved.getUser().getId().toString())
                    .build();

            rawResponseRepository.save(rawResponse);
        } catch (Exception e) {
            // Не блокируем основной поток
            log.error("Failed to save raw response: {}", e.getMessage());
        }
    }

    private void saveAiStats(PlantRequest saved,
                             AIAdapter.AIAnalysisResult aiResult) {
        try {
            AiUsageStat stats = AiUsageStat.builder()
                    .request(saved)
                    .provider(aiAdapter.getProviderName())
                    .model(aiAdapter.getModelName())
                    .tokensUsed(aiResult.tokensUsed())
                    .costEstimate(BigDecimal.valueOf(aiResult.costEstimate()))
                    .createdBy(saved.getUser().getId().toString())
                    .updatedBy(saved.getUser().getId().toString())
                    .build();

            aiUsageStatsRepository.save(stats);
        } catch (Exception e) {
            log.error("Failed to save AI stats: {}", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────
    // Маппинг Entity → DTO
    // ─────────────────────────────────────────────────

    /**
     * Маппинг из Entity (из БД) в Response DTO.
     * Используется для cached результатов и getById.
     */
    private PlantAnalysisResponse mapToAnalysisResponse(
            PlantRequest entity) {

        PlantAnalysisResponse.PlantResult primaryResult = null;

        if (Boolean.TRUE.equals(entity.getIsPlant())
                && entity.getPrimaryName() != null) {
            primaryResult = new PlantAnalysisResponse.PlantResult(
                    entity.getPrimaryName(),
                    null,                   // latinName — добавим позже
                    entity.getFamily(),
                    entity.getRarity(),
                    entity.getHabitat(),
                    null                    // facts — из raw response
            );
        }

        return new PlantAnalysisResponse(
                entity.getId(),
                Boolean.TRUE.equals(entity.getIsPlant()),
                entity.getConfidence() != null
                        ? entity.getConfidence().doubleValue() : 0.0,
                primaryResult,
                List.of(),          // alternatives — из raw response
                new PlantAnalysisResponse.ModelInfo(
                        entity.getAiProvider(),
                        entity.getModelName()
                ),
                entity.getProcessingTimeMs() != null
                        ? entity.getProcessingTimeMs() : 0L
        );
    }

    /**
     * Маппинг с полным AI результатом (сразу после анализа).
     */
    private PlantAnalysisResponse mapToAnalysisResponse(
            PlantRequest entity,
            AIAdapter.AIAnalysisResult aiResult) {

        PlantAnalysisResponse.PlantResult primaryResult = null;

        if (aiResult.isPlant() && aiResult.primaryResult() != null) {
            AIAdapter.PlantInfo plant = aiResult.primaryResult();
            primaryResult = new PlantAnalysisResponse.PlantResult(
                    plant.name(),
                    plant.latinName(),
                    plant.family(),
                    plant.rarity(),
                    plant.habitat(),
                    plant.facts()
            );
        }

        List<PlantAnalysisResponse.Alternative> alternatives =
                aiResult.alternatives() == null
                        ? List.of()
                        : aiResult.alternatives().stream()
                        .map(alt -> new PlantAnalysisResponse.Alternative(
                                alt.name(), alt.confidence()
                        ))
                        .toList();

        return new PlantAnalysisResponse(
                entity.getId(),
                aiResult.isPlant(),
                aiResult.confidence(),
                primaryResult,
                alternatives,
                new PlantAnalysisResponse.ModelInfo(
                        aiAdapter.getProviderName(),
                        aiAdapter.getModelName()
                ),
                entity.getProcessingTimeMs()
        );
    }

    private PlantHistoryResponse mapToHistoryResponse(PlantRequest entity) {
        return new PlantHistoryResponse(
                entity.getId(),
                entity.getPrimaryName(),
                Boolean.TRUE.equals(entity.getIsPlant()),
                entity.getConfidence() != null
                        ? entity.getConfidence().doubleValue() : 0.0,
                entity.getAiProvider(),
                entity.getCreatedDate()
        );
    }
}
