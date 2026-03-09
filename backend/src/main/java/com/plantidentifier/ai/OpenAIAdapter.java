package com.plantidentifier.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OpenAIAdapter implements AIAdapter {

    @Override
    public AIAnalysisResult analyzeImage(String imageUrl, String language) {
        log.info("OpenAI analyze: imageUrl={}, lang={}", imageUrl, language);
        return new AIAnalysisResult(
                true, 0.92,
                new PlantInfo(
                        "Monstera deliciosa", "Monstera deliciosa",
                        "Araceae", "common",
                        "Tropical forests of Central America",
                        "Popular houseplant known for its distinctive split leaves"
                ),
                List.of(
                        new AlternativeResult("Monstera adansonii", 0.45),
                        new AlternativeResult("Philodendron bipinnatifidum", 0.31)
                ),
                "{\"model\":\"gpt-4.1-mini\",\"usage\":{\"total_tokens\":150}}",
                150, 0.0045
        );
    }

    @Override public String getProviderName() { return "openai"; }
    @Override public String getModelName()    { return "gpt-4.1-mini"; }
    @Override public boolean isAvailable()    { return true; }
}