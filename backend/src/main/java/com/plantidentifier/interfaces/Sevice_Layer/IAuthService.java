package com.plantidentifier.service;

import com.plantidentifier.dto.request.LoginRequest;
import com.plantidentifier.dto.request.RefreshTokenRequest;
import com.plantidentifier.dto.request.RegisterRequest;
import com.plantidentifier.dto.request.UpgradeRequest;
import com.plantidentifier.dto.response.AuthResponse;

import java.util.UUID;

/**
 * Контракт аутентификации и авторизации.
 */
public interface IAuthService {

    /**
     * Регистрирует нового пользователя.
     * Проверяет что email не занят.
     * Хэширует пароль через BCrypt.
     * Возвращает userId и дату создания.
     *
     * POST /api/v1/auth/register
     */
    AuthResponse.RegisterResponse register(RegisterRequest request);

    /**
     * Аутентифицирует пользователя по email + password.
     * Возвращает пару access + refresh токенов.
     *
     * POST /api/v1/auth/login
     */
    AuthResponse.TokenPair login(LoginRequest request);

    /**
     * Обновляет access token по refresh token.
     * Проверяет что refresh token валидный.
     * Возвращает новый access token.
     *
     * POST /api/v1/auth/refresh
     */
    AuthResponse.AccessTokenResponse refresh(RefreshTokenRequest request);

    /**
     * Апгрейд гостя до зарегистрированного пользователя.
     * Привязывает email и пароль к существующему гостевому аккаунту.
     * Сохраняет историю запросов гостя!
     *
     * POST /api/v1/auth/upgrade
     */
    void upgrade(UUID guestUserId, UpgradeRequest request);
}