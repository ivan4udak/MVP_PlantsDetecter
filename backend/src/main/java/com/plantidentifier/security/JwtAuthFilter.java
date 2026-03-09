package com.plantidentifier.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Фильтр JWT аутентификации.
 *
 * Выполняется ОДИН РАЗ на каждый HTTP запрос.
 * OncePerRequestFilter гарантирует это.
 *
 * Цепочка обработки:
 * HTTP Request
 *   → RequestIdFilter      (добавляет X-Request-ID)
 *   → JwtAuthFilter        (проверяет JWT)       ← МЫ ЗДЕСЬ
 *   → SecurityConfig rules (проверяет права)
 *   → Controller           (бизнес логика)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final IJwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    // Заголовок где клиент передаёт токен
    private static final String AUTH_HEADER = "Authorization";
    // Префикс перед токеном: "Bearer eyJhbGc..."
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain   // следующий фильтр в цепочке
    ) throws ServletException, IOException {

        try {
            // 1. Достаём токен из заголовка
            String token = extractTokenFromRequest(request);

            // 2. Если токен есть — обрабатываем
            if (token != null) {
                processToken(token, request);
            }
            // Если токена нет — просто идём дальше.
            // SecurityConfig решит: если endpoint публичный — пустит,
            // если защищённый — вернёт 401

        } catch (Exception e) {
            // Логируем но НЕ бросаем исключение дальше.
            // Просто не аутентифицируем пользователя —
            // SecurityConfig вернёт 401
            log.debug("JWT authentication failed: {}", e.getMessage());
        }

        // 3. Передаём запрос следующему фильтру В ЛЮБОМ случае
        filterChain.doFilter(request, response);
    }

    /**
     * Проверяет токен и помещает пользователя в SecurityContext.
     *
     * SecurityContext — это "сессия" текущего запроса.
     * После этого метода в любом месте кода можно вызвать:
     * SecurityContextHolder.getContext().getAuthentication()
     * и получить текущего пользователя.
     */
    private void processToken(String token, HttpServletRequest request) {

        // Проверяем что это access token (не refresh!)
        if (!jwtService.isAccessToken(token)) {
            log.debug("Refresh token used for request — rejected");
            return;
        }

        // Валидируем подпись и срок действия
        jwtService.validateToken(token);

        // Достаём userId из токена
        String userId = jwtService.extractUserId(token).toString();

        // Проверяем что ещё не аутентифицированы в этом запросе
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            // Загружаем пользователя из БД
            // (нужно для актуальных прав и статуса)
            CustomUserDetails userDetails =
                    (CustomUserDetails) userDetailsService.loadUserByUsername(userId);

            // Создаём объект аутентификации
            // null — пароль не нужен (уже проверили через JWT)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,   // principal — кто это
                            null,          // credentials — пароль
                            userDetails.getAuthorities() // роли
                    );

            // Добавляем детали запроса (IP адрес, session id)
            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            // Помещаем в контекст — теперь Spring Security
            // знает кто делает этот запрос
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            log.debug("Authenticated userId={}, role={}",
                    userId,
                    userDetails.getAuthorities()
            );
        }
    }

    /**
     * Достаёт токен из заголовка Authorization.
     *
     * Формат заголовка: "Bearer eyJhbGciOiJIUzI1NiJ9..."
     * Нам нужна часть после "Bearer "
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTH_HEADER);

        // StringUtils.hasText() — проверяет что строка
        // не null и не пустая
        if (StringUtils.hasText(bearerToken)
                && bearerToken.startsWith(BEARER_PREFIX)) {
            // Обрезаем "Bearer " (7 символов) и берём остаток
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}