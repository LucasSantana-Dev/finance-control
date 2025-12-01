-- Migration V19: Enable RLS on Missing Tables
-- This migration addresses Supabase advisor warnings about RLS being disabled on public tables
-- Tables: market_indicators, transaction_categories, transaction_subcategories,
--         transaction_source_entity, signup_audit, signup_blocklist
--
-- This migration is idempotent and checks for table existence before enabling RLS.

-- ============================================
-- MARKET_INDICATORS TABLE (Public read-only data)
-- ============================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'market_indicators'
    ) THEN
        -- Enable RLS
        ALTER TABLE market_indicators ENABLE ROW LEVEL SECURITY;

        -- Users can read market indicators (public data)
        DROP POLICY IF EXISTS "Users can view market indicators" ON market_indicators;
        CREATE POLICY "Users can view market indicators"
            ON market_indicators
            FOR SELECT
            TO authenticated
            USING (true);

        -- Only service role can manage market indicators
        DROP POLICY IF EXISTS "Service can manage market indicators" ON market_indicators;
        CREATE POLICY "Service can manage market indicators"
            ON market_indicators
            FOR ALL
            TO service_role
            USING (true)
            WITH CHECK (true);
    END IF;
END $$;

-- ============================================
-- TRANSACTION_CATEGORIES TABLE (Global/shared table)
-- ============================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'transaction_categories'
    ) THEN
        -- Enable RLS (re-enable if it was disabled)
        ALTER TABLE transaction_categories ENABLE ROW LEVEL SECURITY;

        -- All authenticated users can view categories (global/shared table)
        DROP POLICY IF EXISTS "Users can view categories" ON transaction_categories;
        CREATE POLICY "Users can view categories"
            ON transaction_categories
            FOR SELECT
            TO authenticated
            USING (true);

        -- Only service role can manage categories
        DROP POLICY IF EXISTS "Service can manage categories" ON transaction_categories;
        CREATE POLICY "Service can manage categories"
            ON transaction_categories
            FOR ALL
            TO service_role
            USING (true)
            WITH CHECK (true);
    END IF;
END $$;

-- ============================================
-- TRANSACTION_SUBCATEGORIES TABLE (Global/shared table)
-- ============================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'transaction_subcategories'
    ) THEN
        -- Enable RLS (re-enable if it was disabled)
        ALTER TABLE transaction_subcategories ENABLE ROW LEVEL SECURITY;

        -- All authenticated users can view subcategories (global/shared table)
        DROP POLICY IF EXISTS "Users can view subcategories" ON transaction_subcategories;
        CREATE POLICY "Users can view subcategories"
            ON transaction_subcategories
            FOR SELECT
            TO authenticated
            USING (true);

        -- Only service role can manage subcategories
        DROP POLICY IF EXISTS "Service can manage subcategories" ON transaction_subcategories;
        CREATE POLICY "Service can manage subcategories"
            ON transaction_subcategories
            FOR ALL
            TO service_role
            USING (true)
            WITH CHECK (true);
    END IF;
END $$;

-- ============================================
-- TRANSACTION_SOURCE_ENTITY TABLE (User-specific)
-- ============================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'transaction_source_entity'
    ) THEN
        -- Enable RLS (re-enable if it was disabled)
        ALTER TABLE transaction_source_entity ENABLE ROW LEVEL SECURITY;

        -- Users can view sources (matches V12 policy pattern - simple authenticated access)
        -- Note: This table may not have user_id column in Supabase, so using simple policy
        DROP POLICY IF EXISTS "Users can view sources" ON transaction_source_entity;
        CREATE POLICY "Users can view sources"
            ON transaction_source_entity
            FOR SELECT
            TO authenticated
            USING (true);

        -- Service role can manage all sources
        DROP POLICY IF EXISTS "Service can manage sources" ON transaction_source_entity;
        CREATE POLICY "Service can manage sources"
            ON transaction_source_entity
            FOR ALL
            TO service_role
            USING (true)
            WITH CHECK (true);
    END IF;
END $$;

-- ============================================
-- SIGNUP_AUDIT TABLE (Supabase Auth extension - service role only)
-- ============================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'signup_audit'
    ) THEN
        -- Enable RLS
        ALTER TABLE signup_audit ENABLE ROW LEVEL SECURITY;

        -- Only service role can access signup audit logs
        DROP POLICY IF EXISTS "Service can access signup audit" ON signup_audit;
        CREATE POLICY "Service can access signup audit"
            ON signup_audit
            FOR ALL
            TO service_role
            USING (true)
            WITH CHECK (true);

        -- Deny all access to authenticated users (audit logs are sensitive)
        DROP POLICY IF EXISTS "Deny authenticated access to signup audit" ON signup_audit;
        CREATE POLICY "Deny authenticated access to signup audit"
            ON signup_audit
            FOR ALL
            TO authenticated
            USING (false)
            WITH CHECK (false);
    END IF;
END $$;

-- ============================================
-- SIGNUP_BLOCKLIST TABLE (Supabase Auth extension - service role only)
-- ============================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'signup_blocklist'
    ) THEN
        -- Enable RLS
        ALTER TABLE signup_blocklist ENABLE ROW LEVEL SECURITY;

        -- Only service role can access signup blocklist
        DROP POLICY IF EXISTS "Service can access signup blocklist" ON signup_blocklist;
        CREATE POLICY "Service can access signup blocklist"
            ON signup_blocklist
            FOR ALL
            TO service_role
            USING (true)
            WITH CHECK (true);

        -- Deny all access to authenticated users (blocklist is sensitive)
        DROP POLICY IF EXISTS "Deny authenticated access to signup blocklist" ON signup_blocklist;
        CREATE POLICY "Deny authenticated access to signup blocklist"
            ON signup_blocklist
            FOR ALL
            TO authenticated
            USING (false)
            WITH CHECK (false);
    END IF;
END $$;

-- ============================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'market_indicators') THEN
        COMMENT ON POLICY "Users can view market indicators" ON market_indicators IS
            'Allows authenticated users to read market indicators (public economic data).';
        COMMENT ON POLICY "Service can manage market indicators" ON market_indicators IS
            'Only service role can insert, update, or delete market indicators.';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'transaction_categories') THEN
        COMMENT ON POLICY "Users can view categories" ON transaction_categories IS
            'Allows authenticated users to view transaction categories (global/shared table).';
        COMMENT ON POLICY "Service can manage categories" ON transaction_categories IS
            'Only service role can manage transaction categories.';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'transaction_subcategories') THEN
        COMMENT ON POLICY "Users can view subcategories" ON transaction_subcategories IS
            'Allows authenticated users to view transaction subcategories (global/shared table).';
        COMMENT ON POLICY "Service can manage subcategories" ON transaction_subcategories IS
            'Only service role can manage transaction subcategories.';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'transaction_source_entity') THEN
        COMMENT ON POLICY "Users can view sources" ON transaction_source_entity IS
            'Allows authenticated users to view transaction sources.';
        COMMENT ON POLICY "Service can manage sources" ON transaction_source_entity IS
            'Only service role can manage all transaction sources.';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'signup_audit') THEN
        COMMENT ON POLICY "Service can access signup audit" ON signup_audit IS
            'Only service role can access signup audit logs (sensitive security data).';
        COMMENT ON POLICY "Deny authenticated access to signup audit" ON signup_audit IS
            'Denies all access to authenticated users for signup audit logs.';
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'signup_blocklist') THEN
        COMMENT ON POLICY "Service can access signup blocklist" ON signup_blocklist IS
            'Only service role can access signup blocklist (sensitive security data).';
        COMMENT ON POLICY "Deny authenticated access to signup blocklist" ON signup_blocklist IS
            'Denies all access to authenticated users for signup blocklist.';
    END IF;
END $$;
