package com.plantidentifier.service;

import com.plantidentifier.entity.RateLimitLog;
import com.plantidentifier.entity.User;
import com.plantidentifier.exception.RateLimitExceededException;
import com.plantidentifier.repository.PlantRequestRepository;
import com.plantidentifier.repository.RateLimitLogRepository;
import com.plantidentifier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService implements IRateLimitService {

    private final PlantRequestRepository plantRequestRepository;
    private final RateLimitLogRepository rateLimitLogRepository;
    private final UserRepository userRepository;

    // Лимиты по ролям
    private static final int LIMIT_GUEST = 3;
    private static final int LIMIT_USER  = 30;
    // ADMIN и SYSTEM — без лимита

    @Override
    @Transactional
    public void checkLimit(User user, String endpoint) {

        // ADMIN и SYSTEM не ограничены
        if (user.getRole() == User.Role.ROLE_ADMIN
                || user.getRole() == User.Role.ROLE_SYSTEM) {
            return;
        }

        int limit = getLimit(user);

        // Считаем сколько запросов сделано СЕГОДНЯ
        // "Сегодня" = с 00:00:00 текущего дня UTC
        LocalDateTime startOfDay = LocalDate.now(ZoneOffset.UTC)
                .atTime(LocalTime.MIDNIGHT);

        long requestsToday = plantRequestRepository
                .countByUserIdSince(user.getId(), startOfDay);

        log.debug("Rate limit check: userId={}, today={}, limit={}",
                user.getId(), requestsToday, limit);

        if (requestsToday >= limit) {

            // Логируем срабатывание rate limit
            saveRateLimitLog(user, endpoint,
                    (int) requestsToday, limit, true);

            // Считаем сколько секунд до следующего дня (сброса лимита)
            LocalDateTime tomorrowMidnight = LocalDate
                    .now(ZoneOffset.UTC)
                    .plusDays(1)
                    .atTime(LocalTime.MIDNIGHT);

            long secondsUntilReset = LocalDateTime
                    .now(ZoneOffset.UTC)
                    .until(tomorrowMidnight,
                            java.time.temporal.ChronoUnit.SECONDS);

            throw new RateLimitExceededException(
                    String.format(
                            "Превышен дневной лимит запросов (%d). " +
                                    "Лимит сбросится через %d минут.",
                            limit, secondsUntilReset / 60
                    ),
                    secondsUntilReset
            );
        }

        // Логируем успешный запрос (не заблокирован)
        saveRateLimitLog(user, endpoint,
                (int) requestsToday + 1, limit, false);
    }

    @Override
    @Transactional(readOnly = true)
    public int getRemainingRequests(UUID userId) {
        User user = userRepository
                .findByIdAndIsDeletedFalse(userId)
                .orElseThrow();

        // ADMIN — возвращаем -1 (без ограничений)
        if (user.getRole() == User.Role.ROLE_ADMIN
                || user.getRole() == User.Role.ROLE_SYSTEM) {
            return -1;
        }

        int limit = getLimit(user);

        LocalDateTime startOfDay = LocalDate.now(ZoneOffset.UTC)
                .atTime(LocalTime.MIDNIGHT);

        long used = plantRequestRepository
                .countByUserIdSince(userId, startOfDay);

        return Math.max(0, limit - (int) used);
    }

    @Override
    @Transactional
    public void resetLimit(UUID userId) {
        // В нашей реализации лимит считается по записям в plant_requests.
        // "Сброс" = помечаем все записи за сегодня как is_deleted.
        // Но это удалит историю! Лучше — просто логируем сброс.
        // В будущем с Redis: DEL rate_limit:{userId}

        log.info("Rate limit reset for userId={}", userId);

        // Пишем системное событие о сбросе
        RateLimitLog resetLog = RateLimitLog.builder()
                .user(userRepository.findByIdAndIsDeletedFalse(userId)
                        .orElseThrow())
                .endpoint("MANUAL_RESET")
                .requestCount(0)
                .limitValue(0)
                .blocked(false)
                .createdBy("admin")
                .build();

        rateLimitLogRepository.save(resetLog);
    }

    // ─────────────────────────────────────────────────
    // Приватные методы
    // ─────────────────────────────────────────────────

    private int getLimit(User user) {
        return switch (user.getRole()) {
            case ROLE_GUEST -> LIMIT_GUEST;
            case ROLE_USER  -> LIMIT_USER;
            // ADMIN и SYSTEM не должны сюда попасть (проверяем выше)
            default -> Integer.MAX_VALUE;
        };
    }

    private void saveRateLimitLog(User user, String endpoint,
                                  int count, int limit,
                                  boolean blocked) {
        try {
            RateLimitLog logEntry = RateLimitLog.builder()
                    .user(user)
                    .endpoint(endpoint)
                    .requestCount(count)
                    .limitValue(limit)
                    .blocked(blocked)
                    .createdBy(user.getId().toString())
                    .build();

            rateLimitLogRepository.save(logEntry);
        } catch (Exception e) {
            // Не блокируем основной поток если лог не записался
            log.error("Failed to save rate limit log: {}", e.getMessage());
        }
    }
}