package com.plantidentifier.controller.v1;

import com.plantidentifier.dto.request.PlantAnalyzeRequest;
import com.plantidentifier.dto.response.PlantAnalysisResponse;
import com.plantidentifier.dto.response.PlantHistoryResponse;
import com.plantidentifier.service.IPlantService;
import com.plantidentifier.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/plants")
@RequiredArgsConstructor
public class PlantController {

    private final IPlantService plantService;

    /**
     * POST /api/v1/plants/analyze
     *
     * Требует аутентификации — любая роль кроме анонимного.
     * GUEST, USER, ADMIN — все могут анализировать.
     * Ограничение для GUEST — через RateLimitService.
     */
    @PostMapping("/analyze")
    public ResponseEntity<PlantAnalysisResponse> analyze(
            @Valid @RequestBody PlantAnalyzeRequest request) {

        var userId = SecurityUtils.getCurrentUserId();

        log.debug("POST /plants/analyze userId={}", userId);

        PlantAnalysisResponse response =
                plantService.analyze(userId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/plants/history?page=0&size=20
     *
     * Только зарегистрированные — гости не имеют истории.
     * @PreAuthorize("hasAnyRole('USER','ADMIN')") — только USER и ADMIN.
     *
     * Pageable: page — номер страницы (с 0),
     *           size — количество записей (макс 100)
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<PlantHistoryResponse>> getHistory(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        // Ограничиваем максимальный размер страницы
        size = Math.min(size, 100);

        var userId = SecurityUtils.getCurrentUserId();

        log.debug("GET /plants/history userId={}, page={}, size={}",
                userId, page, size);

        // PageRequest — параметры пагинации для Spring Data
        // Sort.by(...).descending() — сначала новые
        Page<PlantHistoryResponse> history = plantService.getHistory(
                userId,
                PageRequest.of(page, size,
                        Sort.by("createdDate").descending())
        );

        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/v1/plants/{id}
     * Полный результат анализа по ID.
     *
     * @PathVariable — достаёт {id} из URL
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlantAnalysisResponse> getById(
            @PathVariable UUID id) {

        var userId = SecurityUtils.getCurrentUserId();

        log.debug("GET /plants/{} userId={}", id, userId);

        return ResponseEntity.ok(plantService.getById(id, userId));
    }

    /**
     * DELETE /api/v1/plants/{id}
     * Soft delete — is_deleted = true.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        var userId = SecurityUtils.getCurrentUserId();

        log.debug("DELETE /plants/{} userId={}", id, userId);

        plantService.delete(id, userId);

        // 204 No Content — успешно удалено, тела нет
        return ResponseEntity.noContent().build();
    }
}