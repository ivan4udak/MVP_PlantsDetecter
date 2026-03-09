package com.plantidentifier.util;

import com.plantidentifier.exception.InvalidTokenException;
import com.plantidentifier.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Утилита для получения данных текущего пользователя.
 *
 * После того как JwtAuthFilter отработал — в SecurityContext
 * хранится аутентифицированный пользователь.
 * Эти методы достают его из контекста.
 *
 * Используется в сервисах и контроллерах:
 * UUID userId = SecurityUtils.getCurrentUserId();
 */
public final class SecurityUtils {

    // final класс + приватный конструктор = нельзя создать экземпляр
    // Все методы static — вызываем как SecurityUtils.getCurrentUserId()
    private SecurityUtils() {}

    /**
     * Возвращает CustomUserDetails текущего пользователя.
     * Бросает исключение если пользователь не аутентифицирован.
     */
    public static CustomUserDetails getCurrentUser() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new InvalidTokenException("Пользователь не аутентифицирован");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new InvalidTokenException("Неверный тип principal");
        }

        return userDetails;
    }

    /**
     * Короткий метод — просто UUID текущего пользователя.
     * Используется в 90% случаев.
     */
    public static UUID getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    /**
     * Язык текущего пользователя.
     * Используется при вызове AI — передаём язык для ответа.
     */
    public static String getCurrentUserLanguage() {
        return getCurrentUser().getLanguage();
    }
}