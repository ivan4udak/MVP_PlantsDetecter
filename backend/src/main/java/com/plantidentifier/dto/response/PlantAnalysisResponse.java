package com.plantidentifier.dto.response;

import java.util.List;
import java.util.UUID;

public record PlantAnalysisResponse(
        UUID requestId,
        boolean isPlant,
        double confidence,
        PlantResult primaryResult,
        List<Alternative> alternatives,
        ModelInfo modelInfo,
        long processingTimeMs
) {
    public record PlantResult(
            String name,
            String latinName,
            String family,
            String rarity,
            String habitat,
            String facts
    ) {}

    public record Alternative(
            String name,
            double confidence
    ) {}

    public record ModelInfo(
            String provider,
            String model
    ) {}
}