package com.plantidentifier.ai;

import java.util.List;

public interface AIAdapter {

    AIAnalysisResult analyzeImage(String imageUrl, String language);
    String getProviderName();
    String getModelName();
    boolean isAvailable();

    record AIAnalysisResult(
            boolean isPlant,
            double confidence,
            PlantInfo primaryResult,
            List<AlternativeResult> alternatives,
            String rawResponse,
            int tokensUsed,
            double costEstimate
    ) {}

    record PlantInfo(
            String name, String latinName, String family,
            String rarity, String habitat, String facts
    ) {}

    record AlternativeResult(String name, double confidence) {}
}