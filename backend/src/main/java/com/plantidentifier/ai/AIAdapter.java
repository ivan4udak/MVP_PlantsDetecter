package com.plantidentifier.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Заглушка OpenAI адаптера.
 *
 * Сейчас возвращает фиктивные данные.
 * В следующем спринте — реальные вызовы OpenAI API.
 *
 * @Component — регистрирует как Bean.
 * Spring автоматически подставит туда где нужен AIAdapter.
 */
@Slf4j
@Component
public class OpenAIAdapter implements AIAdapter {

    @Override
    public AIAnalysisResult analyzeImage(String imageUrl, String language) {

        log.info("OpenAI analyze: imageUrl={}, lang={}", imageUrl, language);

        // TODO: реальный вызов OpenAI API (Sprint 2)
        // Пока возвращаем фиктивные данные для тестирования

        return new AIAnalysisResult(
                true,
                0.92,
                new PlantInfo(
                        "Monstera deliciosa",
                        "Monstera deliciosa",
                        "Araceae",
                        "common",
                        "Tropical forests of Central America",
                        "Popular houseplant known for its distinctive split leaves"
                ),
                List.of(
                        new AlternativeResult("Monstera adansonii", 0.45),
                        new AlternativeResult("Philodendron bipinnatifidum", 0.31)
                ),
                // rawResponse — симулируем JSON ответ от OpenAI
                """
                {
                  "model": "gpt-4.1-mini",
                  "usage": {"total_tokens": 150},
                  "plant": "Monstera deliciosa",
                  "confidence": 0.92
                }
                """,
                150,     // tokensUsed
                0.0045   // costEstimate в USD
        );
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public String getModelName() {
        return "gpt-4.1-mini";
    }

    @Override
    public boolean isAvailable() {
        // TODO: реальная проверка через ping к OpenAI API
        return true;
    }
}