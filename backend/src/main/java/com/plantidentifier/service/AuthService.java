package com.plantidentifier.service;

import com.plantidentifier.dto.request.LoginRequest;
import com.plantidentifier.dto.request.RefreshTokenRequest;
import com.plantidentifier.dto.request.RegisterRequest;
import com.plantidentifier.dto.request.UpgradeRequest;
import com.plantidentifier.dto.response.AuthResponse;
import com.plantidentifier.entity.User;
import com.plantidentifier.exception.InvalidTokenException;
import com.plantidentifier.exception.PlantNotFoundException;
import com.plantidentifier.exception.UserAlreadyExistsException;
import com.plantidentifier.repository.UserRepository;
import com.plantidentifier.security.IJwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final UserRepository       userRepository;
    private final IJwtService          jwtService;
    private final PasswordEncoder      passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // ─────────────────────────────────────────────────
    // Register
    // ─────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse.RegisterResponse register(RegisterRequest request) {
        log.info("Registering new user with email={}", request.email());

        // Проверяем что email не занят
        if (userRepository.existsByEmailAndIsDeletedFalse(request.email())) {
            throw new UserAlreadyExistsException(
                    "Пользователь с email " + request.email() + " уже существует"
            );
        }

        // Создаём пользователя
        // passwordEncoder.encode() — BCrypt хэширование
        // "password123" → "$2a$12$randomSalt..."
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .userType(User.UserType.REGISTERED)
                .role(User.Role.ROLE_USER)
                .status(User.UserStatus.ACTIVE)
                .preferredLanguage(request.language())
                .createdBy(request.email())
                .updatedBy(request.email())
                .build();

        User saved = userRepository.save(user);

        log.info("User registered: userId={}", saved.getId());

        return new AuthResponse.RegisterResponse(
                saved.getId(),
                saved.getCreatedDate()
        );
    }

    // ─────────────────────────────────────────────────
    // Login
    // ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthResponse.TokenPair login(LoginRequest request) {
        log.info("Login attempt for email={}", request.email());

        try {
            // AuthenticationManager проверяет email + password
            // Внутри: loadUserByUsername() + passwordEncoder.matches()
            // Если не совпадает — бросает BadCredentialsException
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            // username = email для логина
                            // (CustomUserDetailsService у нас грузит по UUID,
                            //  но AuthService грузит по email напрямую)
                            request.email(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException e) {
            // Специально не говорим: "пароль неверный" или "email не найден"
            // Это security best practice — не раскрываем существование email
            throw new InvalidTokenException("Неверный email или пароль");
        }

        User user = userRepository
                .findByEmailAndIsDeletedFalse(request.email())
                .orElseThrow(() ->
                        new InvalidTokenException("Неверный email или пароль")
                );

        // Проверяем что аккаунт активен
        if (user.getStatus() == User.UserStatus.BLOCKED) {
            throw new InvalidTokenException("Аккаунт заблокирован");
        }

        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("Login successful: userId={}", user.getId());

        return new AuthResponse.TokenPair(
                accessToken,
                refreshToken,
                jwtService.getAccessTokenExpiration() / 1000,
                user.getRole().name(),
                user.getPreferredLanguage()
        );
    }

    // ─────────────────────────────────────────────────
    // Refresh Token
    // ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AuthResponse.AccessTokenResponse refresh(
            RefreshTokenRequest request) {

        log.debug("Refresh token request");

        // Валидируем refresh token
        jwtService.validateToken(request.refreshToken());

        // Убеждаемся что это именно refresh (не access) token
        if (jwtService.isAccessToken(request.refreshToken())) {
            throw new InvalidTokenException(
                    "Ожидался refresh token, получен access token"
            );
        }

        // Достаём userId из токена
        UUID userId = jwtService.extractUserId(request.refreshToken());

        // Загружаем актуального пользователя из БД
        // (права могли измениться с момента выдачи refresh token)
        User user = userRepository
                .findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() ->
                        new InvalidTokenException("Пользователь не найден")
                );

        if (user.getStatus() == User.UserStatus.BLOCKED) {
            throw new InvalidTokenException("Аккаунт заблокирован");
        }

        // Выдаём новый access token
        String newAccessToken = jwtService.generateAccessToken(user);

        log.debug("Token refreshed for userId={}", userId);

        return new AuthResponse.AccessTokenResponse(
                newAccessToken,
                jwtService.getAccessTokenExpiration() / 1000
        );
    }

    // ─────────────────────────────────────────────────
    // Upgrade Guest → User
    // ─────────────────────────────────────────────────

    @Override
    @Transactional
    public void upgrade(UUID guestUserId, UpgradeRequest request) {
        log.info("Upgrading guest userId={} to registered user",
                guestUserId);

        // Проверяем что email не занят другим пользователем
        if (userRepository.existsByEmailAndIsDeletedFalse(request.email())) {
            throw new UserAlreadyExistsException(
                    "Email " + request.email() + " уже используется"
            );
        }

        // Находим гостя
        User guest = userRepository
                .findByIdAndIsDeletedFalse(guestUserId)
                .orElseThrow(() ->
                        new PlantNotFoundException("Пользователь не найден")
                );

        // Убеждаемся что это действительно гость
        if (guest.getUserType() != User.UserType.GUEST) {
            throw new UserAlreadyExistsException(
                    "Пользователь уже зарегистрирован"
            );
        }

        // Апгрейд: меняем тип и роль, добавляем email и пароль
        // ВСЯ ИСТОРИЯ ЗАПРОСОВ ГОСТЯ СОХРАНЯЕТСЯ!
        // (записи в plant_requests привязаны к user.id который не меняется)
        guest.setEmail(request.email());
        guest.setPasswordHash(passwordEncoder.encode(request.password()));
        guest.setUserType(User.UserType.REGISTERED);
        guest.setRole(User.Role.ROLE_USER);
        guest.setGuestExpiresAt(null); // убираем TTL
        guest.setUpdatedBy(request.email());
        guest.setUpdatedDate(LocalDateTime.now());

        userRepository.save(guest);

        log.info("Guest upgraded to user: userId={}, email={}",
                guestUserId, request.email());
    }
}