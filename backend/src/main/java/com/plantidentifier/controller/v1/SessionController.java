package com.plantidentifier.controller.v1;

import com.plantidentifier.dto.request.GuestSessionRequest;
import com.plantidentifier.dto.response.GuestSessionResponse;
import com.plantidentifier.service.ISessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @RestController — говорит Spring:
 *   1. Это Bean (создай и управляй)
 *   2. Все методы возвращают JSON (не HTML страницы)
 *   Комбинация @Controller + @ResponseBody
 *
 * @RequestMapping("/session") — базовый путь для всех методов.
 * Полный путь: /api/v1/session (context-path из application.properties)
 */
@Slf4j
@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    // Зависим от ИНТЕРФЕЙСА, не от реализации
    private final ISessionService sessionService;

    /**
     * POST /api/v1/session/guest
     *
     * @Valid — запускает валидацию полей GuestSessionRequest
     * Если @NotBlank не выполнен → GlobalExceptionHandler
     * перехватит MethodArgumentNotValidException → 400 Bad Request
     *
     * ResponseEntity — позволяет контролировать HTTP статус и заголовки
     */
    @PostMapping("/guest")
    public ResponseEntity<GuestSessionResponse> createGuestSession(
            @Valid @RequestBody GuestSessionRequest request) {

        log.debug("POST /session/guest deviceId={}", request.deviceId());

        GuestSessionResponse response =
                sessionService.createGuestSession(request);

        // 201 Created — новый ресурс создан
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}