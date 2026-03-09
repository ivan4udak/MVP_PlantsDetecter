-- =====================================================
-- V2: Таблицы для анализа растений
-- =====================================================

-- ─────────────────────────────────────────────────────
-- Главная таблица: каждый запрос на определение растения
-- ─────────────────────────────────────────────────────
CREATE TABLE app.plant_requests
(
    id                 UUID          NOT NULL DEFAULT uuid_generate_v4(),

    -- Внешний ключ → app.users
    -- ON DELETE RESTRICT: нельзя удалить юзера у которого есть запросы
    user_id            UUID          NOT NULL,

    -- URL изображения в S3 хранилище
    image_url          TEXT,

    -- SHA-256 хэш изображения
    -- Зачем: если клиент шлёт то же фото — не тратим AI токены
    image_hash         VARCHAR(128),

    -- Геолокация (опционально, для будущих фич)
    latitude           NUMERIC(9, 6),
    longitude          NUMERIC(9, 6),

    -- ── Результат AI анализа ────────────────────────

    -- false если на фото не растение (мусор, животное...)
    is_plant           BOOLEAN,

    -- Уверенность: 0.0000 — 1.0000
    confidence         NUMERIC(5, 4),

    -- Основное название растения
    primary_name       VARCHAR(255),

    -- Семейство: Betulaceae, Rosaceae...
    family             VARCHAR(255),

    -- Редкость: common / rare / endangered / extinct
    rarity             VARCHAR(100),

    -- Среда обитания: длинный текст
    habitat            TEXT,

    -- ── Мета-данные AI провайдера ────────────────────
    ai_provider        VARCHAR(100),  -- 'openai' | 'yandex' | 'gigachat'
    model_name         VARCHAR(100),  -- 'gpt-4.1-mini' | ...
    processing_time_ms INTEGER,       -- сколько мс занял анализ

    -- ── Аудит поля ──────────────────────────────────
    created_by         VARCHAR(100),
    created_date       TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_by         VARCHAR(100),
    updated_date       TIMESTAMP     NOT NULL DEFAULT NOW(),
    version            BIGINT        NOT NULL DEFAULT 0,
    is_deleted         BOOLEAN       NOT NULL DEFAULT FALSE,

    -- ── Ограничения ─────────────────────────────────
    CONSTRAINT plant_requests_pkey    PRIMARY KEY (id),
    CONSTRAINT plant_requests_user_fk FOREIGN KEY (user_id)
        REFERENCES app.users (id) ON DELETE RESTRICT,
    CONSTRAINT plant_requests_confidence_check
        CHECK (confidence IS NULL OR (confidence >= 0 AND confidence <= 1))
);

COMMENT ON TABLE  app.plant_requests                    IS 'Запросы на определение растений';
COMMENT ON COLUMN app.plant_requests.image_hash         IS 'SHA-256 для дедупликации: одно фото = один AI запрос';
COMMENT ON COLUMN app.plant_requests.confidence         IS 'Уверенность AI: 0.0-1.0';
COMMENT ON COLUMN app.plant_requests.is_plant           IS 'false если на фото не растение';
COMMENT ON COLUMN app.plant_requests.processing_time_ms IS 'Время ответа AI в миллисекундах';

-- ─────────────────────────────────────────────────────
-- Сырой ответ от AI — храним для отладки и аудита
-- Позволяет перепарсить если изменим логику парсинга
-- ─────────────────────────────────────────────────────
CREATE TABLE app.plant_raw_responses
(
    id           UUID      NOT NULL DEFAULT uuid_generate_v4(),

    -- Один запрос → один сырой ответ (1:1)
    request_id   UUID      NOT NULL,

    -- JSONB: бинарный JSON в PostgreSQL
    -- Быстрее text для поиска внутри JSON (GIN индексы)
    -- raw_json: оригинальный ответ AI как есть (для отладки)
    raw_json     JSONB,

    -- parsed_json: наш структурированный результат
    parsed_json  JSONB,

    created_by   VARCHAR(100),
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by   VARCHAR(100),
    updated_date TIMESTAMP NOT NULL DEFAULT NOW(),
    version      BIGINT    NOT NULL DEFAULT 0,
    is_deleted   BOOLEAN   NOT NULL DEFAULT FALSE,

    CONSTRAINT plant_raw_responses_pkey       PRIMARY KEY (id),
    CONSTRAINT plant_raw_responses_request_fk FOREIGN KEY (request_id)
        REFERENCES app.plant_requests (id) ON DELETE CASCADE,
    -- CASCADE: удаляем сырой ответ если удаляем запрос
    CONSTRAINT plant_raw_responses_unique_request UNIQUE (request_id)
    -- UNIQUE: гарантируем что у одного запроса ровно один raw response
);

COMMENT ON TABLE  app.plant_raw_responses             IS 'Сырые ответы AI — для отладки и повторного парсинга';
COMMENT ON COLUMN app.plant_raw_responses.raw_json    IS 'Оригинальный JSON от AI без изменений';
COMMENT ON COLUMN app.plant_raw_responses.parsed_json IS 'Структурированный результат после парсинга';