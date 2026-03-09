-- =====================================================
-- V5: Индексы для производительности
-- Выносим в отдельный файл — легче анализировать
-- =====================================================

-- ─────────────────────────────────────────────────────
-- app.users индексы
-- ─────────────────────────────────────────────────────

-- Поиск по email при логине (самый частый запрос!)
-- WHERE is_deleted = FALSE — частичный индекс,
-- не индексирует удалённых пользователей
CREATE INDEX idx_users_email
    ON app.users (email)
    WHERE is_deleted = FALSE;

-- Поиск гостей при очистке устаревших сессий
CREATE INDEX idx_users_device_id
    ON app.users (device_id)
    WHERE is_deleted = FALSE;

-- Поиск по типу пользователя (для admin статистики)
CREATE INDEX idx_users_user_type
    ON app.users (user_type)
    WHERE is_deleted = FALSE;

-- Поиск истекших гостей (фоновая задача очистки)
CREATE INDEX idx_users_guest_expires_at
    ON app.users (guest_expires_at)
    WHERE user_type = 'GUEST'
      AND is_deleted = FALSE;

-- ─────────────────────────────────────────────────────
-- app.plant_requests индексы
-- ─────────────────────────────────────────────────────

-- История пользователя — главный запрос истории
-- Составной индекс: сначала user_id, потом дата
CREATE INDEX idx_plant_requests_user_date
    ON app.plant_requests (user_id, created_date DESC)
    WHERE is_deleted = FALSE;

-- Дедупликация по хэшу изображения
CREATE INDEX idx_plant_requests_image_hash
    ON app.plant_requests (image_hash)
    WHERE image_hash IS NOT NULL;

-- Для подсчёта запросов за день (rate limiting)
-- Частый запрос: SELECT COUNT(*) WHERE user_id=? AND created_date >= today
CREATE INDEX idx_plant_requests_user_created
    ON app.plant_requests (user_id, created_date)
    WHERE is_deleted = FALSE;

-- ─────────────────────────────────────────────────────
-- analytics индексы
-- ─────────────────────────────────────────────────────

-- Аналитика по провайдеру и дате (для admin дашборда)
CREATE INDEX idx_ai_usage_provider_date
    ON analytics.ai_usage_stats (provider, created_date DESC);

-- Общая стоимость за период
CREATE INDEX idx_ai_usage_created_date
    ON analytics.ai_usage_stats (created_date DESC);

-- Rate limit по юзеру за день
CREATE INDEX idx_rate_limit_user_date
    ON analytics.rate_limit_log (user_id, created_date DESC);

-- ─────────────────────────────────────────────────────
-- audit индексы
-- ─────────────────────────────────────────────────────

-- Поиск всей истории изменений конкретного пользователя
CREATE INDEX idx_users_log_entity_id
    ON audit.users_log (entity_id, changed_date DESC);

-- Поиск изменений за период (для compliance отчётов)
CREATE INDEX idx_users_log_changed_date
    ON audit.users_log (changed_date DESC);

-- История изменений конкретного запроса
CREATE INDEX idx_plant_requests_log_entity_id
    ON audit.plant_requests_log (entity_id, changed_date DESC);

-- Ошибки за период (для мониторинга)
CREATE INDEX idx_error_logs_created_date
    ON audit.error_logs (created_date DESC);

-- Системные события по severity (алерты)
CREATE INDEX idx_system_events_severity
    ON audit.system_events (severity, created_date DESC);
```

---

## ✅ Итог — что готово
```
✅ V1__create_users_table.sql   — схемы + uuid + таблица users
✅ V2__create_plant_tables.sql  — plant_requests + plant_raw_responses
✅ V3__create_audit_tables.sql  — analytics.* + audit.*
✅ V4__create_triggers.sql      — аудит триггеры с оптимизацией
✅ V5__create_indexes.sql       — все индексы с объяснением зачем