package com.plantidentifier.controller.v1;

import com.plantidentifier.dto.request.LanguageUpdateRequest;
import com.plantidentifier.service.IUserService;
import com.plantidentifier.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * PATCH /api/v1/users/language
     *
     * PATCH — частичное обновление ресурса.
     * Меняем только один атрибут — язык.
     * (PUT — полная замена ресурса)
     */
    @PatchMapping("/language")
    public ResponseEntity<Void> updateLanguage(
            @Valid @RequestBody LanguageUpdateRequest request) {

        var userId = SecurityUtils.getCurrentUserId();

        log.debug("PATCH /users/language userId={}, lang={}",
                userId, request.preferredLanguage());

        userService.updateLanguage(userId, request);

        // 204 No Content — обновлено, тела нет (по спецификации)
        return ResponseEntity.noContent().build();
    }
}