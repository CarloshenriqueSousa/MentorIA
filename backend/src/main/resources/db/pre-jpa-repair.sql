-- Reparos idempotentes executados antes do Hibernate (spring.sql.init).
-- Corrige schema legado do Supabase e remove contas sem senha local.

DO $$
DECLARE
    orphan_ids UUID[];
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = current_schema() AND table_name = 'users'
    ) THEN
        RETURN;
    END IF;

    ALTER TABLE users DROP COLUMN IF EXISTS supabase_user_id;

    ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
    ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(32);
    ALTER TABLE users ADD COLUMN IF NOT EXISTS plan_type VARCHAR(32);

    UPDATE users SET role = 'USER' WHERE role IS NULL;
    UPDATE users SET plan_type = 'FREE' WHERE plan_type IS NULL;

    SELECT array_agg(id) INTO orphan_ids
    FROM users
    WHERE password_hash IS NULL OR btrim(password_hash) = '';

    IF orphan_ids IS NULL THEN
        RETURN;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'chat_messages') THEN
        DELETE FROM chat_messages
        WHERE session_id IN (
            SELECT id FROM chat_sessions WHERE user_id = ANY (orphan_ids)
        );
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'chat_sessions') THEN
        DELETE FROM chat_sessions WHERE user_id = ANY (orphan_ids);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'study_progress') THEN
        DELETE FROM study_progress WHERE user_id = ANY (orphan_ids);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'message_usage') THEN
        DELETE FROM message_usage WHERE user_id = ANY (orphan_ids);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'study_plans') THEN
        DELETE FROM study_plans WHERE user_id = ANY (orphan_ids);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'subscriptions') THEN
        DELETE FROM subscriptions WHERE user_id = ANY (orphan_ids);
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_profiles') THEN
        DELETE FROM user_profiles WHERE user_id = ANY (orphan_ids);
    END IF;

    DELETE FROM users WHERE id = ANY (orphan_ids);
END $$;
