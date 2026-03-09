-- =====================================================
-- V3: Analytics и Audit таблицы
-- =====================================================

-- ─────────────────────────────────────────────────────
-- ANALYTICS SCHEMA
-- Для бизнес-аналитики и мониторинга расходов
-- ─────────────────────────────────────────────────────

-- Статистика использования AI (подсчёт токенов и стоимости)
CREATE TABLE analytics.ai_usage_stats
(
    id            UUID          NOT NULL DEFAULT uuid_generate_v4(),

    -- Связь с запросом
    request_id    UUID          NOT NULL,

    provider      VARCHAR(100),          -- 'openai' | 'yandex' | 'gigachat'
    model         VARCHAR(100),          -- 'gpt-4.1-mini' | ...
    tokens_used   INTEGER,               -- сколько токенов потрачено
    -- NUMERIC(10,4): до $9999.9999 за запрос — хватит надолго
    cost_estimate NUMERIC(10, 4),        -- примерная стоимость в USD

    created_by    VARCHAR(100),
    created_date  TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_by    VARCHAR(100),
    updated_date  TIMESTAMP     NOT NULL DEFAULT NOW(),
    version       BIGINT        NOT NULL DEFAULT 0,
    is_deleted    BOOLEAN       NOT NULL DEFAULT FALSE,

    CONSTRAINT ai_usage_stats_pkey       PRIMARY KEY (id),
    CONSTRAINT ai_usage_stats_request_fk FOREIGN KEY (request_id)
        REFERENCES app.plant_requests (id) ON DELETE RESTRICT
);

COMMENT ON TABLE  analytics.ai_usage_stats              IS 'Статистика использования AI: токены и стоимость';
COMMENT ON COLUMN analytics.ai_usage_stats.cost_estimate IS 'Стоимость в USD для трекинга расходов';

-- Лог превышений rate limit
CREATE TABLE analytics.rate_limit_log
(
    id            UUID      NOT NULL DEFAULT uuid_generate_v4(),

    user_id       UUID      NOT NULL,
    endpoint      VARCHAR(100),          -- '/api/v1/plants/analyze'
    request_count INTEGER,               -- сколько запросов сделал
    limit_value   INTEGER,               -- какой был лимит
    blocked       BOOLEAN   NOT NULL DEFAULT FALSE, -- был ли заблокирован

    created_by    VARCHAR(100),
    created_date  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_by    VARCHAR(100),
    updated_date  TIMESTAMP NOT NULL DEFAULT NOW(),
    version       BIGINT    NOT NULL DEFAULT 0,
    is_deleted    BOOLEAN   NOT NULL DEFAULT FALSE,

    CONSTRAINT rate_limit_log_pkey    PRIMARY KEY (id),
    CONSTRAINT rate_limit_log_user_fk FOREIGN KEY (user_id)
        REFERENCES app.users (id) ON DELETE RESTRICT
);

COMMENT ON TABLE analytics.rate_limit_log IS 'Лог срабатываний rate limiter — для анализа злоупотреблений';

-- ─────────────────────────────────────────────────────
-- AUDIT SCHEMA
-- Иммутабельные таблицы — только триггеры пишут сюда!
-- Приложение НЕ должно делать UPDATE/DELETE в этих таблицах
-- ─────────────────────────────────────────────────────

-- Теневая таблица для users
-- Пишется автоматически триггером при любом изменении users
CREATE TABLE audit.users_log
(
    log_id         UUID        NOT NULL DEFAULT uuid_generate_v4(),

    -- id записи в app.users (не FK! запись может быть удалена)
    entity_id      UUID        NOT NULL,

    -- Что произошло: INSERT / UPDATE / DELETE
    operation_type VARCHAR(20) NOT NULL,

    -- Полный снапшот строки ДО изменения (NULL для INSERT)
    old_data       JSONB,

    -- Полный снапшот строки ПОСЛЕ изменения (NULL для DELETE)
    new_data       JSONB,

    -- Кто инициировал изменение
    changed_by     VARCHAR(100),
    changed_date   TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT users_log_pkey            PRIMARY KEY (log_id),
    CONSTRAINT users_log_operation_check
        CHECK (operation_type IN ('INSERT', 'UPDATE', 'DELETE'))
);

COMMENT ON TABLE  audit.users_log                IS 'Иммутабельный аудит изменений таблицы users';
COMMENT ON COLUMN audit.users_log.old_data       IS 'Снапшот строки ДО изменения';
COMMENT ON COLUMN audit.users_log.new_data       IS 'Снапшот строки ПОСЛЕ изменения';
COMMENT ON COLUMN audit.users_log.operation_type IS 'INSERT | UPDATE | DELETE';

-- Теневая таблица для plant_requests
CREATE TABLE audit.plant_requests_log
(
    log_id         UUID        NOT NULL DEFAULT uuid_generate_v4(),
    entity_id      UUID        NOT NULL,
    operation_type VARCHAR(20) NOT NULL,
    old_data       JSONB,
    new_data       JSONB,
    changed_by     VARCHAR(100),
    changed_date   TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT plant_requests_log_pkey            PRIMARY KEY (log_id),
    CONSTRAINT plant_requests_log_operation_check
        CHECK (operation_type IN ('INSERT', 'UPDATE', 'DELETE'))
);

COMMENT ON TABLE audit.plant_requests_log IS 'Иммутабельный аудит изменений plant_requests';

-- Системные события (старт, ошибки, важные действия)
CREATE TABLE audit.system_events
(
    id           UUID        NOT NULL DEFAULT uuid_generate_v4(),
    event_type   VARCHAR(100),           -- 'APP_START' | 'AI_FALLBACK' | ...
    severity     VARCHAR(20),            -- INFO | WARN | ERROR | CRITICAL
    -- Гибкая структура — любые данные события
    payload      JSONB,
    created_date TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT system_events_pkey          PRIMARY KEY (id),
    CONSTRAINT system_events_severity_check
        CHECK (severity IN ('INFO', 'WARN', 'ERROR', 'CRITICAL'))
);

COMMENT ON TABLE audit.system_events IS 'Системные события и алерты';

-- Лог ошибок — пишется из GlobalExceptionHandler
CREATE TABLE audit.error_logs
(
    id           UUID      NOT NULL DEFAULT uuid_generate_v4(),
    -- Может быть NULL если ошибка до парсинга X-Request-ID
    request_id   UUID,
    error_code   VARCHAR(100),           -- 'RATE_LIMIT_EXCEEDED' | 'PLANT_NOT_FOUND'
    message      TEXT,
    stacktrace   TEXT,
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT error_logs_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE audit.error_logs IS 'Лог всех ошибок приложения с stacktrace';