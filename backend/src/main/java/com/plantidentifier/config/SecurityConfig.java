package com.plantidentifier.config;

import com.plantidentifier.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
// @EnableWebSecurity — активирует Spring Security
@EnableWebSecurity
// @EnableMethodSecurity — позволяет использовать
// @PreAuthorize("hasRole('ADMIN')") прямо на методах
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Главное правило безопасности — кто куда имеет доступ.
     *
     * @Bean — говорит Spring: "создай этот объект и управляй им"
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // CSRF отключаем — мы REST API, не веб-форма.
                // CSRF атаки актуальны для браузеров с cookies,
                // у нас JWT в заголовке — не нужно
                .csrf(AbstractHttpConfigurer::disable)

                // Сессии не используем — каждый запрос аутентифицируется
                // заново через JWT. STATELESS = сервер не хранит сессии.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ─── Правила доступа ───────────────────────────────
                .authorizeHttpRequests(auth -> auth

                        // Публичные endpoint-ы — без токена
                        .requestMatchers(HttpMethod.POST,
                                "/session/guest",       // создать гостя
                                "/auth/register",       // регистрация
                                "/auth/login"           // логин
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/auth/refresh"         // обновить токен
                        ).permitAll()

                        // Health check — публичный (для мониторинга)
                        .requestMatchers(
                                "/system/health"
                        ).permitAll()

                        // Admin endpoint-ы — только ROLE_ADMIN
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        // Deep health + системные — ADMIN или SYSTEM
                        .requestMatchers(
                                "/system/health/deep"
                        ).hasAnyRole("ADMIN", "SYSTEM")

                        // Все остальные endpoint-ы — любой
                        // аутентифицированный пользователь
                        // (GUEST, USER, ADMIN, SYSTEM)
                        .anyRequest().authenticated()
                )

                // ─── Обработка ошибок безопасности ────────────────
                .exceptionHandling(ex -> ex
                        // 401 Unauthorized — нет токена или токен невалидный
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            response.getWriter().write("""
                        {
                          "errorCode": "INVALID_TOKEN",
                          "message": "Требуется аутентификация"
                        }
                        """);
                        })
                        // 403 Forbidden — токен есть, но прав не хватает
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write("""
                        {
                          "errorCode": "ACCESS_DENIED",
                          "message": "Недостаточно прав"
                        }
                        """);
                        })
                )

                // ─── Провайдер аутентификации ──────────────────────
                .authenticationProvider(authenticationProvider())

                // ─── Вставляем наш JWT фильтр ──────────────────────
                // BEFORE UsernamePasswordAuthenticationFilter —
                // наш фильтр должен отработать ДО стандартного
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * Провайдер аутентификации — как проверять логин/пароль.
     * Используется в AuthService при логине.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        // Как загружать пользователя
        provider.setUserDetailsService(userDetailsService);
        // Как проверять пароль
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * BCrypt — алгоритм хэширования паролей.
     * Strength=12: 2^12 итераций — медленно для взломщика,
     * незаметно для пользователя (~250мс).
     *
     * Никогда не храним пароль открытым текстом!
     * password → $2a$12$randomSalt+hashedValue
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * AuthenticationManager — нужен в AuthService
     * для явной аутентификации (login endpoint).
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}