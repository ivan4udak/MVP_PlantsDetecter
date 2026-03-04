-- =======================================================
-- V3: Аудит — теневые таблицы и триггеры
-- =======================================================
-- Концепция: для каждой бизнес-таблицы создаём _log таблицу.
-- PostgreSQL ТРИГГЕР автоматически пишет туда при любом изменении.
-- Это НЕЛЬЗЯ обойти из кода — работает на уровне БД.

-- =====================
-- Аудит таблица для users
-- =====================
CREATE TABLE audit.users_log (
                           log_id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           entity_id      UUID         NOT NULL,   -- id записи в users
                           operation_type VARCHAR(20)  NOT NULL,   -- INSERT / UPDATE / DELETE
                           old_data       JSONB,                   -- Что было ДО изменения
                           new_data       JSONB,                   -- Что стало ПОСЛЕ изменения
                           changed_by     VARCHAR(100),            -- Кто изменил
                           changed_date   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_log_entity_id    ON users_log(entity_id);
CREATE INDEX idx_users_log_changed_date ON users_log(changed_date);

-- =====================
-- Аудит таблица для plant_requests
-- =====================
CREATE TABLE audit.plant_requests_log (
                                    log_id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                    entity_id      UUID        NOT NULL,
                                    operation_type VARCHAR(20) NOT NULL,
                                    old_data       JSONB,
                                    new_data       JSONB,
                                    changed_by     VARCHAR(100),
                                    changed_date   TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_plant_requests_log_entity_id ON plant_requests_log(entity_id);