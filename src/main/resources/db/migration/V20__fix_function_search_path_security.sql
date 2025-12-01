-- Migration V20: Fix Function search_path Security Issues and Add Missing RLS Policies
-- This migration addresses Supabase advisor warnings:
-- 1. Functions with mutable search_path (table_change_audit, insert_audit_log)
-- 2. Missing RLS policies on transaction_responsibilities table
--
-- Security Issues:
-- - Functions without SET search_path are vulnerable to search_path injection attacks
-- - Tables with RLS enabled but no policies allow no access (blocking legitimate queries)
--
-- Fixes:
-- - Add SET search_path = public to secure functions
-- - Add comprehensive RLS policies for transaction_responsibilities table
--
-- This migration is idempotent and checks for function/table existence before altering.

-- ============================================
-- FIX table_change_audit FUNCTION
-- ============================================
-- Fix all overloads of table_change_audit function
DO $$
DECLARE
    func_oid oid;
    func_signature text;
BEGIN
    -- Loop through all overloads of table_change_audit
    FOR func_oid, func_signature IN
        SELECT p.oid, pg_get_function_identity_arguments(p.oid)
        FROM pg_proc p
        JOIN pg_namespace n ON p.pronamespace = n.oid
        WHERE n.nspname = 'public'
        AND p.proname = 'table_change_audit'
    LOOP
        -- Alter each function overload to set search_path
        -- Use the function's OID to uniquely identify it
        EXECUTE format('ALTER FUNCTION public.table_change_audit(%s) SET search_path = public', func_signature);

        RAISE NOTICE 'Fixed search_path for function: public.table_change_audit(%)', func_signature;
    END LOOP;
END $$;

-- ============================================
-- FIX insert_audit_log FUNCTION
-- ============================================
-- Fix all overloads of insert_audit_log function
DO $$
DECLARE
    func_oid oid;
    func_signature text;
BEGIN
    -- Loop through all overloads of insert_audit_log
    FOR func_oid, func_signature IN
        SELECT p.oid, pg_get_function_identity_arguments(p.oid)
        FROM pg_proc p
        JOIN pg_namespace n ON p.pronamespace = n.oid
        WHERE n.nspname = 'public'
        AND p.proname = 'insert_audit_log'
    LOOP
        -- Alter each function overload to set search_path
        EXECUTE format('ALTER FUNCTION public.insert_audit_log(%s) SET search_path = public', func_signature);

        RAISE NOTICE 'Fixed search_path for function: public.insert_audit_log(%)', func_signature;
    END LOOP;
END $$;

-- ============================================
-- ADD MISSING RLS POLICIES FOR transaction_responsibilities TABLE
-- ============================================
-- The transaction_responsibilities table is a junction table linking transactions to responsibles
-- It has RLS enabled but no policies, which blocks all access
-- Users should be able to manage responsibilities for their own transactions
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
        AND table_name = 'transaction_responsibilities'
    ) THEN
        -- Ensure RLS is enabled (it should already be, but make sure)
        ALTER TABLE transaction_responsibilities ENABLE ROW LEVEL SECURITY;

        -- Users can view responsibilities for their own transactions
        -- Access is granted through the transaction's user_id
        DROP POLICY IF EXISTS "Users can view own transaction responsibilities" ON transaction_responsibilities;
        CREATE POLICY "Users can view own transaction responsibilities"
            ON transaction_responsibilities
            FOR SELECT
            TO authenticated
            USING (
                EXISTS (
                    SELECT 1 FROM transactions t
                    WHERE t.id = transaction_responsibilities.transaction_id
                    AND (auth.uid()::text = t.user_id::text OR auth.role() = 'service_role')
                )
            );

        -- Users can insert responsibilities for their own transactions
        DROP POLICY IF EXISTS "Users can insert own transaction responsibilities" ON transaction_responsibilities;
        CREATE POLICY "Users can insert own transaction responsibilities"
            ON transaction_responsibilities
            FOR INSERT
            TO authenticated
            WITH CHECK (
                EXISTS (
                    SELECT 1 FROM transactions t
                    WHERE t.id = transaction_responsibilities.transaction_id
                    AND (auth.uid()::text = t.user_id::text OR auth.role() = 'service_role')
                )
            );

        -- Users can update responsibilities for their own transactions
        DROP POLICY IF EXISTS "Users can update own transaction responsibilities" ON transaction_responsibilities;
        CREATE POLICY "Users can update own transaction responsibilities"
            ON transaction_responsibilities
            FOR UPDATE
            TO authenticated
            USING (
                EXISTS (
                    SELECT 1 FROM transactions t
                    WHERE t.id = transaction_responsibilities.transaction_id
                    AND (auth.uid()::text = t.user_id::text OR auth.role() = 'service_role')
                )
            );

        -- Users can delete responsibilities for their own transactions
        DROP POLICY IF EXISTS "Users can delete own transaction responsibilities" ON transaction_responsibilities;
        CREATE POLICY "Users can delete own transaction responsibilities"
            ON transaction_responsibilities
            FOR DELETE
            TO authenticated
            USING (
                EXISTS (
                    SELECT 1 FROM transactions t
                    WHERE t.id = transaction_responsibilities.transaction_id
                    AND (auth.uid()::text = t.user_id::text OR auth.role() = 'service_role')
                )
            );

        -- Add comments for documentation
        COMMENT ON POLICY "Users can view own transaction responsibilities" ON transaction_responsibilities IS
            'Allows users to view responsibility assignments for their own transactions. Access is granted through the transaction''s user_id.';
        COMMENT ON POLICY "Users can insert own transaction responsibilities" ON transaction_responsibilities IS
            'Allows users to create responsibility assignments for their own transactions.';
        COMMENT ON POLICY "Users can update own transaction responsibilities" ON transaction_responsibilities IS
            'Allows users to update responsibility assignments (e.g., percentage, notes) for their own transactions.';
        COMMENT ON POLICY "Users can delete own transaction responsibilities" ON transaction_responsibilities IS
            'Allows users to remove responsibility assignments from their own transactions.';
    END IF;
END $$;

-- ============================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================
-- Note: Function comments are set dynamically above if functions exist
-- The search_path setting prevents search_path injection attacks by ensuring
-- functions always use the 'public' schema, regardless of the caller's search_path
--
-- The transaction_responsibilities RLS policies ensure users can only manage
-- responsibility assignments for transactions they own, maintaining data isolation
