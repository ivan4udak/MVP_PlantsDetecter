package com.plantidentifier.ai;

import java.util.List;

/**
 * Абстракция над AI провайдерами.
 *
 * Зачем: завтра меняем OpenAI на Yandex —
 * меняем только одну реализацию, весь остальной
 * код (PlantService) не трогаем вообще.
 *
 * Это паттерн "Стратегия" (Strategy Pattern).
 */
public interface AIAdapter {

    /**
     * Анализирует изображение и возвращает результат.
     *
     * @param imageUrl  URL изображения в S3
     * @param language  язык ответа: "ru", "en", "de"
     * @return          результат анализа
     */
    AIAnalysisResult analyzeImage(String imageUrl, String language);

    /**
     * Возвращает имя провайдера для логов и статистики.
     * Например: "openai", "yandex", "gigachat"
     */
    String getProviderName();

    /**
     * Возвращает имя модели.
     * Например: "gpt-4.1-mini"
     */
    String getModelName();

    /**
     * Проверяет что провайдер доступен (health check).
     */
    boolean isAvailable();

    // ─────────────────────────────────────────────────
    // Вложенные классы — структуры данных для AI слоя
    // record = неизменяемый класс данных (Java 16+)
    // Автоматически генерирует: конструктор, геттеры,
    // equals, hashCode, toString
    // ─────────────────────────────────────────────────

    /**
     * Полный результат анализа от AI провайдера.
     */
    record AIAnalysisResult(

            // Это вообще растение?
            boolean isPlant,

            // Уверенность: 0.0 — 1.0
            double confidence,

            // Основной результат
            PlantInfo primaryResult,

            // Альтернативные варианты (если AI не уверен)
            List<AlternativeResult> alternatives,

            // Сырой JSON ответ от провайдера (для аудита)
            String rawResponse,

            // Сколько токенов потрачено
            int tokensUsed,

            // Примерная стоимость в USD
            double costEstimate
    ) {}

    /**
     * Основная информация о растении.
     */
    record PlantInfo(
            String name,        // "Betula pendula"
            String latinName,   // латинское название
            String family,      // "Betulaceae"
            String rarity,      // "common" | "rare" | "endangered"
            String habitat,     // среда обитания
            String facts        // интересные факты
    ) {}

    /**
     * Альтернативный вариант определения.
     */
    record AlternativeResult(
            String name,
            double confidence
    ) {}
}