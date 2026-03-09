package com.plantidentifier.service;

import com.plantidentifier.dto.request.GuestSessionRequest;
import com.plantidentifier.dto.response.GuestSessionResponse;
import com.plantidentifier.entity.User;
import com.plantidentifier.repository.UserRepository;
import com.plantidentifier.security.IJwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService implements ISessionService {

    private final UserRepository userRepository;
    private final IJwtService jwtService;

    // Гостевая сессия живёт 24 часа
    private static final int GUEST_DAILY_LIMIT    = 3;
    private static final long GUEST_TTL_HOURS     = 24L;

    @Override
    @Transactional
    public GuestSessionResponse createGuestSession(GuestSessionRequest request) {

        log.info("Creating guest session for deviceId={}",
                request.deviceId());

        // Ищем существующего гостя по deviceId.
        // Зачем: если приложение переустановили — не создаём
        // дублирующего гостя, возвращаем существующего
        User user = userRepository
                .findByDeviceIdAndIsDeletedFalse(request.deviceId())
                .map(existingGuest -> refreshGuestExpiry(existingGuest))
                .orElseGet(() -> createNewGuest(request));

        // Генерируем токены
        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("Guest session created: userId={}, deviceId={}",
                user.getId(), request.deviceId());

        return new GuestSessionResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getRole().name(),
                user.getPreferredLanguage(),
                // expiresIn в секундах
                jwtService.getAccessTokenExpiration() / 1000,
                GUEST_DAILY_LIMIT
        );
    }

    // ─────────────────────────────────────────────────
    // Приватные методы
    // ─────────────────────────────────────────────────

    private User createNewGuest(GuestSessionRequest request) {
        log.debug("Creating new guest for deviceId={}", request.deviceId());

        User guest = User.builder()
                .deviceId(request.deviceId())
                .userType(User.UserType.GUEST)
                .role(User.Role.ROLE_GUEST)
                .preferredLanguage(request.preferredLanguage())
                .status(User.UserStatus.ACTIVE)
                // Гостевая сессия истекает через 24 часа
                .guestExpiresAt(LocalDateTime.now().plusHours(GUEST_TTL_HOURS))
                .createdBy("system")
                .updatedBy("system")
                .build();

        return userRepository.save(guest);
    }

    /**
     * Обновляем TTL существующего гостя —
     * продлеваем сессию ещё на 24 часа с момента
     * последнего обращения
     */
    private User refreshGuestExpiry(User existingGuest) {
        log.debug("Refreshing guest session for userId={}",
                existingGuest.getId());

        existingGuest.setGuestExpiresAt(
                LocalDateTime.now().plusHours(GUEST_TTL_HOURS)
        );
        existingGuest.setUpdatedBy("system");

        return userRepository.save(existingGuest);
    }
}