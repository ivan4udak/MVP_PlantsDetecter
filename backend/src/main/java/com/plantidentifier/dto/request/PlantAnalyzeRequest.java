package com.plantidentifier.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record PlantAnalyzeRequest(

        @NotBlank(message = "imageUrl обязателен")
        String imageUrl,

        // Геолокация — опциональная
        @DecimalMin(value = "-90.0",  message = "Широта: от -90 до 90")
        @DecimalMax(value = "90.0",   message = "Широта: от -90 до 90")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Долгота: от -180 до 180")
        @DecimalMax(value = "180.0",  message = "Долгота: от -180 до 180")
        BigDecimal longitude
) {}