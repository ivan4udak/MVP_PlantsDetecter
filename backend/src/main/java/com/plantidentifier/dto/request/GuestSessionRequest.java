package com.plantidentifier.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// record — неизменяемый DTO (Java 16+)
// Автоматически: конструктор, геттеры, equals, toString
public record GuestSessionRequest(

        @NotBlank(message = "deviceId обязателен")
        @Size(max = 255)
        String deviceId,

        // Язык: только строчные буквы, 2-5 символов
        // Примеры: "ru", "en", "de", "zh-CN"
        @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$",
                message = "Неверный формат языка. Примеры: ru, en, zh-CN")
        String preferredLanguage
) {
    // Дефолтный язык если не передали
    public GuestSessionRequest {
        if (preferredLanguage == null || preferredLanguage.isBlank()) {
            preferredLanguage = "en";
        }
    }
}