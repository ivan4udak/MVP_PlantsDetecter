package com.plantidentifier.controller.v1;

import com.plantidentifier.dto.request.LoginRequest;
import com.plantidentifier.dto.request.RefreshTokenRequest;
import com.plantidentifier.dto.request.RegisterRequest;
import com.plantidentifier.dto.request.UpgradeRequest;
import com.plantidentifier.dto.response.AuthResponse;
import com.plantidentifier.service.IAuthService;
import com.plantidentifier.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    /**
     * POST /api/v1/auth/register
     * Публичный endpoint — без токена
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse.RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        log.debug("POST /auth/register email={}", request.email());

        AuthResponse.RegisterResponse response =
                authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * POST /api/v1/auth/login
     * Публичный endpoint — без токена
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse.TokenPair> login(
            @Valid @RequestBody LoginRequest request) {

        log.debug("POST /auth/login email={}", request.email());

        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /api/v1/auth/refresh
     * Публичный endpoint — нужен только refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse.AccessTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.debug("POST /auth/refresh");

        return ResponseEntity.ok(authService.refresh(request));
    }

    /**
     * POST /api/v1/auth/upgrade
     * Защищённый — нужен действующий токен гостя
     *
     * @PreAuthorize — проверяет роль ДО вызова метода.
     * Только гость может апгрейднуться.
     * Если вызовет ROLE_USER — получит 403 Forbidden.
     */
    @PostMapping("/upgrade")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<Void> upgrade(
            @Valid @RequestBody UpgradeRequest request) {

        // Достаём userId из SecurityContext —
        // не нужно передавать в теле запроса
        var userId = SecurityUtils.getCurrentUserId();

        log.debug("POST /auth/upgrade userId={}", userId);

        authService.upgrade(userId, request);

        // 200 OK — успешно, тела нет
        return ResponseEntity.ok().build();
    }
}