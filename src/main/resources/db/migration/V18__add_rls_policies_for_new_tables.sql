-- Migration V18: Add Row Level Security (RLS) Policies for New Tables
-- This migration enables RLS on notifications, user_settings, and user_categories tables
-- and creates comprehensive policies to ensure users can only access their own data
--
-- Note: Uses auth.uid()::text = user_id::text pattern for BIGINT user_id compatibility
-- with Supabase Auth UUID. The backend uses BIGINT for user_id, while Supabase Auth
-- uses UUID, so we convert both to text for comparison.
--
-- This migration is idempotent and checks for table existence before creating policies.

-- ============================================
-- NOTIFICATIONS TABLE POLICIES
-- ============================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'notifications'
    ) THEN
        -- Enable RLS
        ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

        -- Users can view their own notifications
        DROP POLICY IF EXISTS "Users can view own notifications" ON notifications;
        CREATE POLICY "Users can view own notifications"
            ON notifications
            FOR SELECT
            USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

        -- Users can insert their own notifications
        DROP POLICY IF EXISTS "Users can insert own notifications" ON notifications;
        CREATE POLICY "Users can insert own notifications"
            ON notifications
            FOR INSERT
            WITH CHECK (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

        -- Users can update their own notifications
        DROP POLICY IF EXISTS "Users can update own notifications" ON notifications;
        CREATE POLICY "Users can update own notifications"
            ON notifications
            FOR UPDATE
            USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

        -- Users can delete their own notifications
        DROP POLICY IF EXISTS "Users can delete own notifications" ON notifications;
        CREATE POLICY "Users can delete own notifications"
            ON notifications
            FOR DELETE
            USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

        -- Composite index for common notification queries (user, read status, creation date)
        CREATE INDEX IF NOT EXISTS idx_notifications_user_read_created
            ON notifications(user_id, is_read, created_at DESC);
    END IF;
END $$;

-- ============================================
-- USER_SETTINGS TABLE POLICIES
-- ============================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'user_settings'
    ) THEN
        -- Enable RLS
        ALTER TABLE user_settings ENABLE ROW LEVEL SECURITY;

        -- Note: user_settings has a UNIQUE constraint on user_id, so each user has only one settings record

        -- Users can view their own settings
        DROP POLICY IF EXISTS "Users can view own settings" ON user_settings;
        CREATE POLICY "Users can view own settings"
            ON user_settings
            FOR SELECT
            USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

        -- Users can insert their own settings (one per user due to UNIQUE constraint)
        DROP POLICY IF EXISTS "Users can insert own settings" ON user_settings;
        CREATE POLICY "Users can insert own settings"
            ON user_settings
            FOR INSERT
            WITH CHECK (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

        -- Users can update their own settings
        DROP POLICY IF EXISTS "Users can update own settings" ON user_settings;
        CREATE POLICY "Users can update own settings"
            ON user_settings
            FOR UPDATE
            USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

        -- Users cannot delete their own settings (settings should persist)
        -- Service role can delete if needed for data cleanup
        DROP POLICY IF EXISTS "Service can delete settings" ON user_settings;
        CREATE POLICY "Service can delete settings"
            ON user_settings
            FOR DELETE
            USING (auth.role() = 'service_role');
    END IF;
END $$;

-- ============================================
-- USER_CATEGORIES TABLE POLICIES
-- ============================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'user_categories'
    ) THEN
        -- Enable RLS
        ALTER TABLE user_categories ENABLE ROW LEVEL SECURITY;

        -- Users can view their own categories
        DROP POLICY IF EXISTS "Users can view own categories" ON user_categories;
        CREATE POLICY "Users can view own categories"
            ON user_categories
            FOR SELECT
            USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

        -- Users can insert their own categories
        DROP POLICY IF EXISTS "Users can insert own categories" ON user_categories;
        CREATE POLICY "Users can insert own categories"
            ON user_categories
            FOR INSERT
            WITH CHECK (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

        -- Users can update their own categories
        DROP POLICY IF EXISTS "Users can update own categories" ON user_categories;
        CREATE POLICY "Users can update own categories"
            ON user_categories
            FOR UPDATE
            USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

        -- Users can delete their own categories
        DROP POLICY IF EXISTS "Users can delete own categories" ON user_categories;
        CREATE POLICY "Users can delete own categories"
            ON user_categories
            FOR DELETE
            USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

        -- Composite index for user categories queries (user, type, default flag)
        CREATE INDEX IF NOT EXISTS idx_user_categories_user_type_default
            ON user_categories(user_id, type, is_default);
    END IF;
END $$;

-- ============================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================
-- Add comments only if tables exist
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'notifications') THEN
        COMMENT ON POLICY "Users can view own notifications" ON notifications IS
            'Allows users to view only their own notifications. Service role can view all.';
        COMMENT ON POLICY "Users can insert own notifications" ON notifications IS
            'Allows users to create notifications for themselves. Service role can create for any user.';
        COMMENT ON POLICY "Users can update own notifications" ON notifications IS
            'Allows users to update only their own notifications (e.g., mark as read).';
        COMMENT ON POLICY "Users can delete own notifications" ON notifications IS
            'Allows users to delete only their own notifications.';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'user_settings') THEN
        COMMENT ON POLICY "Users can view own settings" ON user_settings IS
            'Allows users to view only their own settings. Service role can view all.';
        COMMENT ON POLICY "Users can insert own settings" ON user_settings IS
            'Allows users to create their own settings record (one per user).';
        COMMENT ON POLICY "Users can update own settings" ON user_settings IS
            'Allows users to update only their own settings.';
        COMMENT ON POLICY "Service can delete settings" ON user_settings IS
            'Only service role can delete settings (for data cleanup).';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'user_categories') THEN
        COMMENT ON POLICY "Users can view own categories" ON user_categories IS
            'Allows users to view only their own categories. Service role can view all.';
        COMMENT ON POLICY "Users can insert own categories" ON user_categories IS
            'Allows users to create their own transaction categories.';
        COMMENT ON POLICY "Users can update own categories" ON user_categories IS
            'Allows users to update only their own categories.';
        COMMENT ON POLICY "Users can delete own categories" ON user_categories IS
            'Allows users to delete only their own categories.';
    END IF;
END $$;
