-- =======================================================
-- V1: Создание таблицы пользователей
-- =======================================================

-- Расширение для генерации UUID (встроено в PostgreSQL)
-- UUID — уникальный идентификатор вида: 550e8400-e29b-41d4-a716-446655440000
-- Мы используем UUID вместо обычного числового id по причинам безопасности:
-- числовой id=1,2,3 легко перебрать, UUID — нет
SET search_path TO app, public;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE app.users (
    -- Первичный ключ: UUID генерируется автоматически
                       id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Email: уникальный, может быть NULL для guest пользователей
                       email               VARCHAR(255) UNIQUE,

    -- Хэш пароля (никогда не храним пароль в открытом виде!)
                       password_hash       VARCHAR(255),

    -- Тип пользователя: GUEST или REGISTERED
                       user_type           VARCHAR(20)  NOT NULL DEFAULT 'GUEST',

    -- Статус аккаунта: ACTIVE, BLOCKED, DELETED
                       status              VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',

    -- ID устройства (для guest сессий)
                       device_id           VARCHAR(255),

    -- Предпочитаемый язык: 'ru', 'en', 'de' и т.д.
                       preferred_language  VARCHAR(10)  NOT NULL DEFAULT 'en',

    -- Когда истекает guest сессия (NULL для обычных пользователей)
                       guest_expires_at    TIMESTAMP,

    -- ↓↓↓ Аудит поля — кто и когда создал/изменил запись ↓↓↓

    -- Кто создал запись (system, или email пользователя)
                       created_by          VARCHAR(100),

    -- Когда создана запись
                       created_date        TIMESTAMP    NOT NULL DEFAULT NOW(),

    -- Кто последний изменил
                       updated_by          VARCHAR(100),

    -- Когда последний раз изменена
                       updated_date        TIMESTAMP    NOT NULL DEFAULT NOW(),

    -- Оптимистичная блокировка: если два запроса пытаются изменить
    -- одну запись одновременно — тот кто пришёл вторым получит ошибку
    -- Spring автоматически проверяет эту версию через @Version
                       version             BIGINT       NOT NULL DEFAULT 0,

    -- Мягкое удаление: вместо DELETE мы ставим is_deleted = true
    -- Данные не теряются, историю можно восстановить
                       is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE
);

-- Индексы для быстрого поиска
-- Без индекса PostgreSQL читает ВСЮ таблицу при поиске по email
-- С индексом — поиск мгновенный (как оглавление в книге)
CREATE INDEX idx_users_email     ON users(email)      WHERE is_deleted = FALSE;
CREATE INDEX idx_users_user_type ON users(user_type)  WHERE is_deleted = FALSE;
CREATE INDEX idx_users_device_id ON users(device_id)  WHERE is_deleted = FALSE;

-- Комментарии к таблице (хорошая практика)
COMMENT ON TABLE  users                    IS 'Пользователи системы (гости и зарегистрированные)';
COMMENT ON COLUMN users.version            IS 'Версия для оптимистичной блокировки';
COMMENT ON COLUMN users.is_deleted         IS 'Мягкое удаление — запись не удаляется физически';
COMMENT ON COLUMN users.guest_expires_at   IS 'Время истечения guest сессии';