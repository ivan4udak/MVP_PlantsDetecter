package com.plantidentifier.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Email обязателен")
        @Email(message = "Неверный формат email")
        @Size(max = 255)
        String email,

        // Минимум 8 символов, хотя бы одна цифра и одна буква
        @NotBlank(message = "Пароль обязателен")
        @Size(min = 8, max = 100, message = "Пароль: от 8 до 100 символов")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Пароль должен содержать буквы и цифры"
        )
        String password,

        @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$",
                message = "Неверный формат языка")
        String language
) {
    public RegisterRequest {
        if (language == null || language.isBlank()) {
            language = "en";
        }
    }
}