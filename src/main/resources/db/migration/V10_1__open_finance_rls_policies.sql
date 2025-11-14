-- Row Level Security (RLS) Policies for Open Finance Tables
-- These policies ensure users can only access their own data
-- Run this migration after V10__create_open_finance_tables.sql
-- Note: RLS must be enabled in Supabase Dashboard for these tables

-- Enable RLS on all Open Finance tables
ALTER TABLE open_finance_consents ENABLE ROW LEVEL SECURITY;
ALTER TABLE connected_accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE account_sync_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE open_finance_institutions ENABLE ROW LEVEL SECURITY;

-- Policy: Users can only view their own consents
CREATE POLICY "Users can view own consents"
    ON open_finance_consents
    FOR SELECT
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

-- Policy: Users can insert their own consents
CREATE POLICY "Users can insert own consents"
    ON open_finance_consents
    FOR INSERT
    WITH CHECK (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

-- Policy: Users can update their own consents
CREATE POLICY "Users can update own consents"
    ON open_finance_consents
    FOR UPDATE
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

-- Policy: Users can delete their own consents
CREATE POLICY "Users can delete own consents"
    ON open_finance_consents
    FOR DELETE
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

-- Policy: Users can only view their own connected accounts
CREATE POLICY "Users can view own connected accounts"
    ON connected_accounts
    FOR SELECT
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

-- Policy: Users can insert their own connected accounts
CREATE POLICY "Users can insert own connected accounts"
    ON connected_accounts
    FOR INSERT
    WITH CHECK (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

-- Policy: Users can update their own connected accounts
CREATE POLICY "Users can update own connected accounts"
    ON connected_accounts
    FOR UPDATE
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

-- Policy: Users can delete their own connected accounts
CREATE POLICY "Users can delete own connected accounts"
    ON connected_accounts
    FOR DELETE
    USING (auth.uid()::text = user_id::text OR auth.role() = 'service_role');

-- Policy: Users can only view sync logs for their own accounts
CREATE POLICY "Users can view own sync logs"
    ON account_sync_logs
    FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM connected_accounts ca
            WHERE ca.id = account_sync_logs.account_id
            AND (auth.uid()::text = ca.user_id::text OR auth.role() = 'service_role')
        )
    );

-- Policy: Service role can insert sync logs (for background jobs)
CREATE POLICY "Service can insert sync logs"
    ON account_sync_logs
    FOR INSERT
    WITH CHECK (auth.role() = 'service_role');

-- Policy: Service role can update sync logs
CREATE POLICY "Service can update sync logs"
    ON account_sync_logs
    FOR UPDATE
    USING (auth.role() = 'service_role');

-- Policy: Institutions are publicly readable (for discovery)
CREATE POLICY "Institutions are publicly readable"
    ON open_finance_institutions
    FOR SELECT
    USING (is_active = TRUE);

-- Policy: Only service role can manage institutions
CREATE POLICY "Service can manage institutions"
    ON open_finance_institutions
    FOR ALL
    USING (auth.role() = 'service_role')
    WITH CHECK (auth.role() = 'service_role');
