package com.plantidentifier.security;

import com.plantidentifier.entity.User;
import com.plantidentifier.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

// @Slf4j — Lombok генерирует: private static final Logger log = ...
// Используем: log.info(), log.debug(), log.error()
@Slf4j

// @Service — регистрирует класс как Spring Bean
// Spring создаст один экземпляр и будет передавать везде где нужен
@Service
public class JwtService implements IJwtService {

    // @Value — читает значение из application.properties
    // ${jwt.secret} → берёт значение ключа jwt.secret
    @Value("${jwt.secret}")
    private String secretKey;

    // Access token: 15 минут = 900_000 мс
    private static final long ACCESS_TOKEN_EXPIRATION = 900_000L;

    // Refresh token: 7 дней = 604_800_000 мс
    private static final long REFRESH_TOKEN_EXPIRATION = 604_800_000L;

    // Имена claims внутри JWT payload
    // Claims = данные которые мы кладём в токен
    private static final String CLAIM_ROLE     = "role";
    private static final String CLAIM_LANGUAGE = "lang";
    private static final String CLAIM_TYPE     = "type";

    // Значения типа токена
    private static final String TYPE_ACCESS    = "access";
    private static final String TYPE_REFRESH   = "refresh";

    // ──────────────────────────────────────────────────
    // Генерация токенов
    // ──────────────────────────────────────────────────

    @Override
    public String generateAccessToken(User user) {
        log.debug("Generating access token for userId={}, role={}",
                user.getId(), user.getRole());

        return Jwts.builder()
                // sub (subject) — кому выдан токен
                .subject(user.getId().toString())
                // Наши кастомные claims
                .claim(CLAIM_ROLE,     user.getRole().name())
                .claim(CLAIM_LANGUAGE, user.getPreferredLanguage())
                .claim(CLAIM_TYPE,     TYPE_ACCESS)
                // Когда выдан
                .issuedAt(new Date())
                // Когда истекает: сейчас + 15 минут
                .expiration(new Date(System.currentTimeMillis()
                        + ACCESS_TOKEN_EXPIRATION))
                // Подписываем секретным ключом
                .signWith(getSigningKey())
                // Собираем в строку
                .compact();
    }

    @Override
    public String generateRefreshToken(User user) {
        log.debug("Generating refresh token for userId={}", user.getId());

        return Jwts.builder()
                .subject(user.getId().toString())
                // В refresh токене минимум данных
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()
                        + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    // ──────────────────────────────────────────────────
    // Извлечение данных из токена
    // ──────────────────────────────────────────────────

    @Override
    public UUID extractUserId(String token) {
        // getClaims() парсит токен и возвращает payload
        // getSubject() возвращает поле "sub"
        String subject = getClaims(token).getSubject();
        return UUID.fromString(subject);
    }

    @Override
    public String extractRole(String token) {
        return getClaims(token).get(CLAIM_ROLE, String.class);
    }

    @Override
    public String extractLanguage(String token) {
        return getClaims(token).get(CLAIM_LANGUAGE, String.class);
    }

    @Override
    public boolean isAccessToken(String token) {
        String type = getClaims(token).get(CLAIM_TYPE, String.class);
        return TYPE_ACCESS.equals(type);
    }

    @Override
    public long getAccessTokenExpiration() {
        return ACCESS_TOKEN_EXPIRATION;
    }

    // ──────────────────────────────────────────────────
    // Валидация токена
    // ──────────────────────────────────────────────────

    @Override
    public boolean validateToken(String token) {
        try {
            // parseSignedClaims проверяет:
            // 1. Подпись (не подделан ли токен)
            // 2. Срок действия (не истёк ли)
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;

        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
            throw new InvalidTokenException("Токен истёк");

        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            throw new InvalidTokenException("Невалидная подпись токена");

        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            throw new InvalidTokenException("Неверный формат токена");

        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw new InvalidTokenException("Неподдерживаемый токен");

        } catch (IllegalArgumentException e) {
            log.warn("JWT token is empty: {}", e.getMessage());
            throw new InvalidTokenException("Токен пустой");
        }
    }

    // ──────────────────────────────────────────────────
    // Приватные вспомогательные методы
    // ──────────────────────────────────────────────────

    /**
     * Парсит токен и возвращает claims (payload).
     * Этот метод НЕ проверяет срок действия —
     * используй validateToken() перед вызовом!
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Создаёт криптографический ключ из строки секрета.
     * HMAC-SHA256 требует минимум 256 бит (32 байта).
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}