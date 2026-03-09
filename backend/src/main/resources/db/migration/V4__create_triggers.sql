-- =====================================================
-- V4: Триггеры аудита
-- Работают на уровне БД — их нельзя обойти из кода!
-- =====================================================

-- ─────────────────────────────────────────────────────
-- Функция аудита для таблицы users
-- Вызывается PostgreSQL автоматически при INSERT/UPDATE/DELETE
-- ─────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION audit.fn_audit_users()
    RETURNS TRIGGER
    LANGUAGE plpgsql
    SECURITY DEFINER  -- выполняется с правами владельца функции
AS
$$
BEGIN
    -- TG_OP: специальная переменная PostgreSQL = 'INSERT'|'UPDATE'|'DELETE'
    -- NEW: новая строка (доступна при INSERT и UPDATE)
    -- OLD: старая строка (доступна при UPDATE и DELETE)
    -- to_jsonb(): конвертирует строку таблицы в JSONB

    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit.users_log
            (entity_id, operation_type, old_data, new_data, changed_by)
        VALUES
            (NEW.id,
             'INSERT',
             NULL,          -- до INSERT не было данных
             to_jsonb(NEW), -- полный снапшот новой строки
             NEW.created_by);
RETURN NEW;

ELSIF TG_OP = 'UPDATE' THEN
        -- Оптимизация: не пишем в аудит если данные не изменились
        -- Сравниваем старый и новый JSONB
        IF to_jsonb(OLD) = to_jsonb(NEW) THEN
            RETURN NEW;
END IF;

INSERT INTO audit.users_log
(entity_id, operation_type, old_data, new_data, changed_by)
VALUES
    (NEW.id,
     'UPDATE',
     to_jsonb(OLD), -- что было
     to_jsonb(NEW), -- что стало
     NEW.updated_by);
RETURN NEW;

ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO audit.users_log
            (entity_id, operation_type, old_data, new_data, changed_by)
        VALUES
            (OLD.id,
             'DELETE',
             to_jsonb(OLD), -- последнее состояние
             NULL,
             OLD.updated_by);
RETURN OLD;
END IF;

RETURN NULL;
END;
$$;

-- Подключаем функцию к таблице users
-- AFTER: сначала данные сохраняются, потом пишем аудит
-- FOR EACH ROW: на каждую затронутую строку отдельно
CREATE TRIGGER trg_users_audit
    AFTER INSERT OR UPDATE OR DELETE
                    ON app.users
                        FOR EACH ROW
                        EXECUTE FUNCTION audit.fn_audit_users();

-- ─────────────────────────────────────────────────────
-- Функция аудита для plant_requests
-- ─────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION audit.fn_audit_plant_requests()
    RETURNS TRIGGER
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit.plant_requests_log
            (entity_id, operation_type, old_data, new_data, changed_by)
        VALUES
            (NEW.id, 'INSERT', NULL, to_jsonb(NEW), NEW.created_by);
RETURN NEW;

ELSIF TG_OP = 'UPDATE' THEN
        IF to_jsonb(OLD) = to_jsonb(NEW) THEN
            RETURN NEW;
END IF;
INSERT INTO audit.plant_requests_log
(entity_id, operation_type, old_data, new_data, changed_by)
VALUES
    (NEW.id, 'UPDATE', to_jsonb(OLD), to_jsonb(NEW), NEW.updated_by);
RETURN NEW;

ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO audit.plant_requests_log
            (entity_id, operation_type, old_data, new_data, changed_by)
        VALUES
            (OLD.id, 'DELETE', to_jsonb(OLD), NULL, OLD.updated_by);
RETURN OLD;
END IF;

RETURN NULL;
END;
$$;

CREATE TRIGGER trg_plant_requests_audit
    AFTER INSERT OR UPDATE OR DELETE
                    ON app.plant_requests
                        FOR EACH ROW
                        EXECUTE FUNCTION audit.fn_audit_plant_requests();