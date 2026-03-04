-- =======================================================
-- V2: Таблицы для запросов анализа растений
-- =======================================================

-- Главная таблица: каждый запрос на определение растения
CREATE TABLE app.plant_requests (
                                id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Связь с пользователем
    -- REFERENCES = внешний ключ: значение ДОЛЖНО существовать в users.id
                                user_id             UUID NOT NULL REFERENCES users(id),

    -- URL изображения в S3 хранилище
                                image_url           TEXT,

    -- Хэш изображения (для дедупликации: если то же фото — не тратим AI токены)
                                image_hash          VARCHAR(128),

    -- Геолокация (опционально, для будущих фич)
                                latitude            NUMERIC(9,6),
                                longitude           NUMERIC(9,6),

    -- Результат анализа
                                is_plant            BOOLEAN,          -- Это вообще растение?
                                confidence          NUMERIC(5,4),     -- Уверенность: 0.0000 - 1.0000
                                primary_name        VARCHAR(255),     -- Название растения
                                family              VARCHAR(255),     -- Семейство растения
                                rarity              VARCHAR(100),     -- Редкость: common/rare/endangered
                                habitat             TEXT,             -- Среда обитания

    -- Информация об AI провайдере
                                ai_provider         VARCHAR(100),     -- 'openai', 'yandex', 'gigachat'
                                model_name          VARCHAR(100),     -- 'gpt-4.1-mini', и т.д.
                                processing_time_ms  INTEGER,          -- Сколько миллисекунд занял анализ

    -- Стандартные аудит поля
                                created_by          VARCHAR(100),
                                created_date        TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_by          VARCHAR(100),
                                updated_date        TIMESTAMP NOT NULL DEFAULT NOW(),
                                version             BIGINT    NOT NULL DEFAULT 0,
                                is_deleted          BOOLEAN   NOT NULL DEFAULT FALSE
);

-- Индексы для часто используемых запросов
CREATE INDEX idx_plant_requests_user_id      ON plant_requests(user_id)      WHERE is_deleted = FALSE;
CREATE INDEX idx_plant_requests_image_hash   ON plant_requests(image_hash);
CREATE INDEX idx_plant_requests_created_date ON plant_requests(created_date)  WHERE is_deleted = FALSE;

-- Сырой ответ от AI (храним для отладки и аудита)
CREATE TABLE app.plant_raw_responses (
                                     id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Связь с запросом
                                     request_id  UUID NOT NULL REFERENCES plant_requests(id),

    -- JSONB — бинарный JSON в PostgreSQL (быстрый поиск внутри JSON)
                                     raw_json    JSONB,      -- Оригинальный ответ AI как есть
                                     parsed_json JSONB,      -- Распарсенный структурированный ответ

                                     created_by   VARCHAR(100),
                                     created_date TIMESTAMP NOT NULL DEFAULT NOW(),
                                     updated_by   VARCHAR(100),
                                     updated_date TIMESTAMP NOT NULL DEFAULT NOW(),
                                     version      BIGINT    NOT NULL DEFAULT 0,
                                     is_deleted   BOOLEAN   NOT NULL DEFAULT FALSE
);

-- Статистика использования AI (для подсчёта расходов)
CREATE TABLE analytics.ai_usage_stats (
                                id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                request_id     UUID NOT NULL REFERENCES plant_requests(id),
                                provider       VARCHAR(100),
                                model          VARCHAR(100),
                                tokens_used    INTEGER,
                                cost_estimate  NUMERIC(10,4),   -- Примерная стоимость в USD

                                created_by   VARCHAR(100),
                                created_date TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_by   VARCHAR(100),
                                updated_date TIMESTAMP NOT NULL DEFAULT NOW(),
                                version      BIGINT    NOT NULL DEFAULT 0,
                                is_deleted   BOOLEAN   NOT NULL DEFAULT FALSE
);

-- Лог rate limiting (кто и когда превысил лимиты)
CREATE TABLE analytics.rate_limit_log (
                                id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                user_id       UUID NOT NULL REFERENCES users(id),
                                endpoint      VARCHAR(100),
                                request_count INTEGER,
                                limit_value   INTEGER,
                                blocked       BOOLEAN NOT NULL DEFAULT FALSE,

                                created_by   VARCHAR(100),
                                created_date TIMESTAMP NOT NULL DEFAULT NOW(),
                                updated_by   VARCHAR(100),
                                updated_date TIMESTAMP NOT NULL DEFAULT NOW(),
                                version      BIGINT    NOT NULL DEFAULT 0,
                                is_deleted   BOOLEAN   NOT NULL DEFAULT FALSE
);

-- Системные события (старт, остановка, ошибки системы)
CREATE TABLE audit.system_events (
                               id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               event_type  VARCHAR(100),
                               severity    VARCHAR(20),    -- INFO, WARN, ERROR, CRITICAL
                               payload     JSONB,
                               created_date TIMESTAMP NOT NULL DEFAULT NOW()
    -- Намеренно без аудит полей — это сами и есть аудит
);

-- Лог ошибок
CREATE TABLE audit.error_logs (
                            id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            request_id  UUID,           -- Может быть NULL если ошибка до парсинга запроса
                            error_code  VARCHAR(100),
                            message     TEXT,
                            stacktrace  TEXT,
                            created_date TIMESTAMP NOT NULL DEFAULT NOW()
);