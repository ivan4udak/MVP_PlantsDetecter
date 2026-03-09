-- =====================================================
-- V1: Расширения, схемы, таблица пользователей
-- =====================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS app;
CREATE SCHEMA IF NOT EXISTS analytics;
CREATE SCHEMA IF NOT EXISTS audit;

CREATE TABLE app.users
(
    id                 UUID         NOT NULL DEFAULT uuid_generate_v4(),

    email              VARCHAR(255),
    password_hash      VARCHAR(255),

    -- user_type: КАК создан аккаунт
    -- GUEST       = создан через POST /session/guest
    -- REGISTERED  = создан через POST /auth/register
    user_type          VARCHAR(20)  NOT NULL DEFAULT 'GUEST',

    -- role: КАКИЕ ПРАВА у пользователя
    -- ROLE_GUEST   = только 3 запроса/день, нет истории
    -- ROLE_USER    = полный доступ к своим данным
    -- ROLE_ADMIN   = доступ к /admin/** endpoints
    -- ROLE_SYSTEM  = для внутренних сервисов и интеграций
    role               VARCHAR(20)  NOT NULL DEFAULT 'ROLE_GUEST',

    status             VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',

    device_id          VARCHAR(255),
    preferred_language VARCHAR(10)  NOT NULL DEFAULT 'en',
    guest_expires_at   TIMESTAMP,

    created_by         VARCHAR(100),
    created_date       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_by         VARCHAR(100),
    updated_date       TIMESTAMP    NOT NULL DEFAULT NOW(),
    version            BIGINT       NOT NULL DEFAULT 0,
    is_deleted         BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT users_pkey              PRIMARY KEY (id),
    CONSTRAINT users_email_unique      UNIQUE (email),

    -- user_type: только два значения — тип аккаунта
    CONSTRAINT users_user_type_check
        CHECK (user_type IN ('GUEST', 'REGISTERED')),

    -- role: четыре значения — права доступа
    CONSTRAINT users_role_check
        CHECK (role IN ('ROLE_GUEST', 'ROLE_USER', 'ROLE_ADMIN', 'ROLE_SYSTEM')),

    CONSTRAINT users_status_check
        CHECK (status IN ('ACTIVE', 'BLOCKED', 'DELETED'))
);

-- Комментарии
COMMENT ON TABLE  app.users            IS 'Пользователи системы';
COMMENT ON COLUMN app.users.user_type  IS 'Тип аккаунта: GUEST | REGISTERED';
COMMENT ON COLUMN app.users.role       IS 'Права доступа: ROLE_GUEST | ROLE_USER | ROLE_ADMIN | ROLE_SYSTEM';
COMMENT ON COLUMN app.users.version    IS 'JPA @Version: оптимистичная блокировка';
COMMENT ON COLUMN app.users.is_deleted IS 'Soft delete: true = скрыт, физически не удалён';