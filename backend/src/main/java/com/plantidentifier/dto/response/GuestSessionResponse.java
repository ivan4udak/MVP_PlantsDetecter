package com.plantidentifier.dto.response;

import java.util.UUID;

public record GuestSessionResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        String role,          // "ROLE_GUEST"
        String language,
        long expiresIn,       // секунды
        int limitPerDay       // 3 для гостей
) {}