package com.plantidentifier.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Email обязателен")
        @Email(message = "Неверный формат email")
        String email,

        @NotBlank(message = "Пароль обязателен")
        String password
) {}