-- =======================================================
-- V4: Триггерная функция для аудита
-- =======================================================

-- Создаём универсальную функцию для аудита
-- Она будет вызываться ПОСЛЕ каждого INSERT/UPDATE/DELETE
CREATE OR REPLACE FUNCTION audit_trigger_function()
RETURNS TRIGGER AS $$
-- $$ — это разделитель тела функции в PostgreSQL
BEGIN
    -- TG_OP — специальная переменная PostgreSQL: 'INSERT', 'UPDATE', или 'DELETE'
    -- TG_TABLE_NAME — имя таблицы которая вызвала триггер
    -- NEW — новые данные (после изменения)
    -- OLD — старые данные (до изменения)
    -- to_jsonb() — конвертирует строку таблицы в JSONB

    IF TG_OP = 'INSERT' THEN
        EXECUTE format(
            'INSERT INTO %I_log (entity_id, operation_type, old_data, new_data, changed_by)
             VALUES ($1, $2, $3, $4, $5)',
            TG_TABLE_NAME   -- подставляет имя таблицы: users → users_log
        ) USING NEW.id, 'INSERT', NULL, to_jsonb(NEW), NEW.created_by;
RETURN NEW;

ELSIF TG_OP = 'UPDATE' THEN
        EXECUTE format(
            'INSERT INTO %I_log (entity_id, operation_type, old_data, new_data, changed_by)
             VALUES ($1, $2, $3, $4, $5)',
            TG_TABLE_NAME
        ) USING NEW.id, 'UPDATE', to_jsonb(OLD), to_jsonb(NEW), NEW.updated_by;
RETURN NEW;

ELSIF TG_OP = 'DELETE' THEN
        EXECUTE format(
            'INSERT INTO %I_log (entity_id, operation_type, old_data, new_data, changed_by)
             VALUES ($1, $2, $3, $4, $5)',
            TG_TABLE_NAME
        ) USING OLD.id, 'DELETE', to_jsonb(OLD), NULL, OLD.updated_by;
RETURN OLD;
END IF;

RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Подключаем триггер к таблице users
-- AFTER — триггер срабатывает ПОСЛЕ операции (данные уже сохранены)
-- FOR EACH ROW — для каждой изменённой строки отдельно
CREATE TRIGGER users_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON users
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_function();

-- Подключаем триггер к plant_requests
CREATE TRIGGER plant_requests_audit_trigger
    AFTER INSERT OR UPDATE OR DELETE ON plant_requests
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_function();