-- Row Level Security (RLS) Policies for All Tables
-- This migration enables RLS on all application tables and creates policies
-- to ensure users can only access their own data
--
-- Note: This assumes Supabase Auth is being used and auth.uid() is available
-- For local development, you may need to disable RLS or use service_role

-- Enable RLS on all main application tables
ALTER TABLE IF EXISTS users ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS transaction_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS transaction_subcategories ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS transaction_source_entity ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS transaction_responsibles ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS financial_goals ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS investments ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS dashboard_cache ENABLE ROW LEVEL SECURITY;

-- ============================================
-- USERS TABLE POLICIES
-- ============================================
-- Users can view their own user record
CREATE POLICY IF NOT EXISTS "Users can view own user"
    ON users
    FOR SELECT
    USING (auth.uid()::text = id::text OR auth.role() = 'service_role');

-- Users can update their own user record
CREATE POLICY IF NOT EXISTS "Users can update own user"
    ON users
    FOR UPDATE
    USING (auth.uid()::text = id::text OR auth.role() = 'service_role');

-- Service role can insert users (for signup)
CREATE POLICY IF NOT EXISTS "Service can insert users"
    ON users
    FOR INSERT
    WITH CHECK (auth.role() = 'service_role');

-- ============================================
-- PROFILES TABLE POLICIES
-- ============================================
CREATE POLICY IF NOT EXISTS "Users can view own profile"
    ON profiles
    FOR SELECT
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can insert own profile"
    ON profiles
    FOR INSERT
    WITH CHECK (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can update own profile"
    ON profiles
    FOR UPDATE
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can delete own profile"
    ON profiles
    FOR DELETE
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

-- ============================================
-- TRANSACTIONS TABLE POLICIES
-- ============================================
CREATE POLICY IF NOT EXISTS "Users can view own transactions"
    ON transactions
    FOR SELECT
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can insert own transactions"
    ON transactions
    FOR INSERT
    WITH CHECK (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can update own transactions"
    ON transactions
    FOR UPDATE
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can delete own transactions"
    ON transactions
    FOR DELETE
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

-- ============================================
-- TRANSACTION CATEGORIES TABLE POLICIES
-- ============================================
-- Note: transaction_categories is a global/shared table (no user_id column)
-- All authenticated users can view, only service_role can modify
CREATE POLICY IF NOT EXISTS "Users can view categories"
    ON transaction_categories
    FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY IF NOT EXISTS "Service can manage categories"
    ON transaction_categories
    FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- ============================================
-- TRANSACTION SUBCATEGORIES TABLE POLICIES
-- ============================================
-- Note: transaction_subcategories is a global/shared table (no user_id column)
-- All authenticated users can view, only service_role can modify
CREATE POLICY IF NOT EXISTS "Users can view subcategories"
    ON transaction_subcategories
    FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY IF NOT EXISTS "Service can manage subcategories"
    ON transaction_subcategories
    FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- ============================================
-- TRANSACTION SOURCE ENTITY TABLE POLICIES
-- ============================================
-- Note: Table name is transaction_source_entity (not transaction_sources)
-- Note: user_id is BIGINT, may need UUID mapping for proper auth integration
CREATE POLICY IF NOT EXISTS "Users can view sources"
    ON transaction_source_entity
    FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY IF NOT EXISTS "Service can manage sources"
    ON transaction_source_entity
    FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- ============================================
-- TRANSACTION RESPONSIBLES TABLE POLICIES
-- ============================================
-- Note: Table name is transaction_responsibles (not transaction_responsibilities)
-- Policies are already handled by Supabase schema, but keeping for reference
-- The actual policies use a helper function: user_can_access_transaction()
CREATE POLICY IF NOT EXISTS "Users can view own responsibilities"
    ON transaction_responsibles
    FOR SELECT
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM transactions t
            WHERE t.id = transaction_responsibles.transaction_id
            AND (auth.uid() = t.user_id OR auth.role() = 'service_role')
        )
    );

CREATE POLICY IF NOT EXISTS "Users can insert own responsibilities"
    ON transaction_responsibles
    FOR INSERT
    TO authenticated
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM transactions t
            WHERE t.id = transaction_responsibles.transaction_id
            AND (auth.uid() = t.user_id OR auth.role() = 'service_role')
        )
    );

CREATE POLICY IF NOT EXISTS "Users can update own responsibilities"
    ON transaction_responsibles
    FOR UPDATE
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM transactions t
            WHERE t.id = transaction_responsibles.transaction_id
            AND (auth.uid() = t.user_id OR auth.role() = 'service_role')
        )
    );

CREATE POLICY IF NOT EXISTS "Users can delete own responsibilities"
    ON transaction_responsibles
    FOR DELETE
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM transactions t
            WHERE t.id = transaction_responsibles.transaction_id
            AND (auth.uid() = t.user_id OR auth.role() = 'service_role')
        )
    );

-- ============================================
-- FINANCIAL GOALS TABLE POLICIES
-- ============================================
CREATE POLICY IF NOT EXISTS "Users can view own goals"
    ON financial_goals
    FOR SELECT
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can insert own goals"
    ON financial_goals
    FOR INSERT
    WITH CHECK (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can update own goals"
    ON financial_goals
    FOR UPDATE
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can delete own goals"
    ON financial_goals
    FOR DELETE
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

-- ============================================
-- INVESTMENTS TABLE POLICIES
-- ============================================
CREATE POLICY IF NOT EXISTS "Users can view own investments"
    ON investments
    FOR SELECT
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can insert own investments"
    ON investments
    FOR INSERT
    WITH CHECK (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can update own investments"
    ON investments
    FOR UPDATE
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can delete own investments"
    ON investments
    FOR DELETE
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

-- ============================================
-- DASHBOARD CACHE TABLE POLICIES
-- ============================================
CREATE POLICY IF NOT EXISTS "Users can view own cache"
    ON dashboard_cache
    FOR SELECT
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can insert own cache"
    ON dashboard_cache
    FOR INSERT
    WITH CHECK (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can update own cache"
    ON dashboard_cache
    FOR UPDATE
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Users can delete own cache"
    ON dashboard_cache
    FOR DELETE
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

-- ============================================
-- BRAZILIAN MARKET TABLES (Read-only for users, managed by service)
-- ============================================
-- These tables are typically managed by background jobs and read by users
ALTER TABLE IF EXISTS brazilian_stocks ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS brazilian_fiis ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS market_data_cache ENABLE ROW LEVEL SECURITY;

-- Users can read market data
CREATE POLICY IF NOT EXISTS "Users can view market data"
    ON brazilian_stocks
    FOR SELECT
    USING (TRUE);

CREATE POLICY IF NOT EXISTS "Users can view FII data"
    ON brazilian_fiis
    FOR SELECT
    USING (TRUE);

CREATE POLICY IF NOT EXISTS "Users can view market cache"
    ON market_data_cache
    FOR SELECT
    USING (TRUE);

-- Only service role can manage market data
CREATE POLICY IF NOT EXISTS "Service can manage stocks"
    ON brazilian_stocks
    FOR ALL
    USING (auth.role() = 'service_role')
    WITH CHECK (auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Service can manage FIIs"
    ON brazilian_fiis
    FOR ALL
    USING (auth.role() = 'service_role')
    WITH CHECK (auth.role() = 'service_role');

CREATE POLICY IF NOT EXISTS "Service can manage market cache"
    ON market_data_cache
    FOR ALL
    USING (auth.role() = 'service_role')
    WITH CHECK (auth.role() = 'service_role');
