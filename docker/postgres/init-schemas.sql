-- Этот файл выполняется ОДИН РАЗ при первом старте PostgreSQL контейнера

-- Создаём три схемы
CREATE SCHEMA IF NOT EXISTS app;
CREATE SCHEMA IF NOT EXISTS analytics;
CREATE SCHEMA IF NOT EXISTS audit;

-- Права доступа для приложения
-- Приложение может делать всё в app и analytics
GRANT ALL ON SCHEMA app       TO plant_user;
GRANT ALL ON SCHEMA analytics TO plant_user;

-- В audit схему приложение может только ЧИТАТЬ и INSERT
-- Но НЕ может делать UPDATE или DELETE (иммутабельный аудит!)
GRANT USAGE ON SCHEMA audit TO plant_user;

-- Комментарии
COMMENT ON SCHEMA app       IS 'Основные таблицы приложения';
COMMENT ON SCHEMA analytics IS 'Таблицы для бизнес-аналитики и метрик';
COMMENT ON SCHEMA audit     IS 'Иммутабельные аудит таблицы';