package com.plantidentifier.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LanguageUpdateRequest(

        @NotBlank(message = "Язык обязателен")
        @Pattern(
                regexp = "^[a-z]{2}(-[A-Z]{2})?$",
                message = "Неверный формат языка. Примеры: ru, en, zh-CN"
        )
        String preferredLanguage
) {}