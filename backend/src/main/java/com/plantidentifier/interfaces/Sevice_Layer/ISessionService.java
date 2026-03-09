package com.plantidentifier.service;

import com.plantidentifier.dto.request.GuestSessionRequest;
import com.plantidentifier.dto.response.GuestSessionResponse;

/**
 * Контракт для управления гостевыми сессиями.
 */
public interface ISessionService {

    /**
     * Создаёт гостевого пользователя и возвращает токены.
     *
     * Логика:
     * 1. Если deviceId уже есть в БД — возвращаем существующего гостя
     * 2. Если нет — создаём нового гостя
     * 3. Генерируем JWT с ролью ROLE_GUEST
     * 4. Устанавливаем guest_expires_at = NOW() + 24 часа
     *
     * POST /api/v1/session/guest
     */
    GuestSessionResponse createGuestSession(GuestSessionRequest request);
}