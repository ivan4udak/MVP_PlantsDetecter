package com.plantidentifier.service;

import com.plantidentifier.dto.request.LanguageUpdateRequest;

import java.util.UUID;

/**
 * Контракт для управления профилем пользователя.
 */
public interface IUserService {

    /**
     * Обновляет preferred_language пользователя.
     * Язык также кэшируется в JWT (обновится при следующем логине).
     *
     * PATCH /api/v1/users/language
     */
    void updateLanguage(UUID userId, LanguageUpdateRequest request);
}