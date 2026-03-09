package com.plantidentifier.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpgradeRequest(

        @NotBlank(message = "Email обязателен")
        @Email(message = "Неверный формат email")
        String email,

        @NotBlank(message = "Пароль обязателен")
        @Size(min = 8, max = 100)
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "Пароль должен содержать буквы и цифры"
        )
        String password
) {}