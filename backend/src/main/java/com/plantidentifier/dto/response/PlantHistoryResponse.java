package com.plantidentifier.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PlantHistoryResponse(
        UUID requestId,
        String primaryName,
        boolean isPlant,
        double confidence,
        String aiProvider,
        LocalDateTime createdDate
) {}