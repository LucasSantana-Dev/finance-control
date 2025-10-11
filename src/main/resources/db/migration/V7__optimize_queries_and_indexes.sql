-- Migration V7: Optimize queries and indexes for better performance
-- This migration adds optimized indexes, functions, and materialized views
-- to improve query performance for common operations

-- =====================================================
-- OPTIMIZED INDEXES FOR COMMON QUERY PATTERNS
-- =====================================================

-- Composite indexes for transaction queries (most common operations)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_user_type_date_amount
ON transactions(user_id, type, date DESC, amount DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_user_date_type_category
ON transactions(user_id, date DESC, type, category_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_user_reconciled_date
ON transactions(user_id, reconciled, date DESC)
WHERE reconciled = false;

-- Partial indexes for active records (reduces index size and improves performance)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_active_user_date
ON transactions(user_id, date DESC)
WHERE reconciled = false;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_financial_goals_active_user_deadline
ON financial_goals(user_id, deadline)
WHERE is_active = true;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_brazilian_stocks_active_user_ticker
ON brazilian_stocks(user_id, ticker)
WHERE is_active = true;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_fii_funds_active_user_ticker
ON fii_funds(user_id, ticker)
WHERE is_active = true;

-- Indexes for text search optimization
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_description_gin
ON transactions USING gin(to_tsvector('portuguese', description));

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_description_trgm
ON transactions USING gin(description gin_trgm_ops);

-- Indexes for Brazilian market data queries
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_brazilian_stocks_user_type_segment
ON brazilian_stocks(user_id, stock_type, segment);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_fii_funds_user_type_segment
ON fii_funds(user_id, fii_type, segment);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_brazilian_bonds_user_type_maturity
ON brazilian_bonds(user_id, bond_type, maturity_date);

-- Indexes for dashboard analytics queries
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_user_type_date_amount_covering
ON transactions(user_id, type, date DESC, amount DESC)
INCLUDE (category_id, description, source);

-- =====================================================
-- OPTIMIZED FUNCTIONS FOR COMMON CALCULATIONS
-- =====================================================

-- Function to calculate monthly financial summary
CREATE OR REPLACE FUNCTION calculate_monthly_summary(
    p_user_id BIGINT,
    p_start_date DATE,
    p_end_date DATE
) RETURNS TABLE (
    total_income NUMERIC(19,2),
    total_expenses NUMERIC(19,2),
    transaction_count BIGINT,
    avg_transaction_amount NUMERIC(19,2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as total_income,
        COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as total_expenses,
        COUNT(*) as transaction_count,
        COALESCE(AVG(t.amount), 0) as avg_transaction_amount
    FROM transactions t
    WHERE t.user_id = p_user_id
        AND t.date::date BETWEEN p_start_date AND p_end_date;
END;
$$ LANGUAGE plpgsql;

-- Function to get top spending categories
CREATE OR REPLACE FUNCTION get_top_spending_categories(
    p_user_id BIGINT,
    p_start_date DATE,
    p_end_date DATE,
    p_limit INTEGER DEFAULT 10
) RETURNS TABLE (
    category_name VARCHAR(255),
    total_amount NUMERIC(19,2),
    transaction_count BIGINT,
    percentage NUMERIC(5,2)
) AS $$
DECLARE
    total_expenses NUMERIC(19,2);
BEGIN
    -- Calculate total expenses for percentage calculation
    SELECT COALESCE(SUM(t.amount), 0) INTO total_expenses
    FROM transactions t
    WHERE t.user_id = p_user_id
        AND t.type = 'EXPENSE'
        AND t.date::date BETWEEN p_start_date AND p_end_date;

    RETURN QUERY
    SELECT
        tc.name as category_name,
        COALESCE(SUM(t.amount), 0) as total_amount,
        COUNT(*) as transaction_count,
        CASE
            WHEN total_expenses > 0 THEN
                ROUND((SUM(t.amount) / total_expenses) * 100, 2)
            ELSE 0
        END as percentage
    FROM transactions t
    JOIN transaction_categories tc ON t.category_id = tc.id
    WHERE t.user_id = p_user_id
        AND t.type = 'EXPENSE'
        AND t.date::date BETWEEN p_start_date AND p_end_date
    GROUP BY tc.name
    ORDER BY total_amount DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- Function to calculate goal progress
CREATE OR REPLACE FUNCTION calculate_goal_progress(
    p_user_id BIGINT
) RETURNS TABLE (
    goal_id BIGINT,
    goal_name VARCHAR(255),
    target_amount NUMERIC(19,2),
    current_amount NUMERIC(19,2),
    progress_percentage NUMERIC(5,2),
    days_remaining INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        fg.id as goal_id,
        fg.name as goal_name,
        fg.target_amount,
        fg.current_amount,
        CASE
            WHEN fg.target_amount > 0 THEN
                ROUND((fg.current_amount / fg.target_amount) * 100, 2)
            ELSE 0
        END as progress_percentage,
        CASE
            WHEN fg.deadline IS NOT NULL THEN
                EXTRACT(DAYS FROM (fg.deadline - CURRENT_DATE))::INTEGER
            ELSE NULL
        END as days_remaining
    FROM financial_goals fg
    WHERE fg.user_id = p_user_id
        AND fg.is_active = true
    ORDER BY fg.deadline ASC NULLS LAST;
END;
$$ LANGUAGE plpgsql;

-- Function to get transaction trends
CREATE OR REPLACE FUNCTION get_transaction_trends(
    p_user_id BIGINT,
    p_months INTEGER DEFAULT 12
) RETURNS TABLE (
    month_date DATE,
    total_income NUMERIC(19,2),
    total_expenses NUMERIC(19,2),
    net_balance NUMERIC(19,2),
    transaction_count BIGINT
) AS $$
DECLARE
    start_date DATE;
    end_date DATE;
    current_month DATE;
BEGIN
    start_date := CURRENT_DATE - INTERVAL '1 month' * p_months;
    end_date := CURRENT_DATE;
    current_month := date_trunc('month', start_date);

    WHILE current_month <= date_trunc('month', end_date) LOOP
        RETURN QUERY
        SELECT
            current_month::DATE as month_date,
            COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as total_income,
            COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as total_expenses,
            COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0) as net_balance,
            COUNT(*) as transaction_count
        FROM transactions t
        WHERE t.user_id = p_user_id
            AND t.date >= current_month
            AND t.date < current_month + INTERVAL '1 month';

        current_month := current_month + INTERVAL '1 month';
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- MATERIALIZED VIEWS FOR DASHBOARD PERFORMANCE
-- =====================================================

-- Materialized view for user financial summary (refreshed daily)
CREATE MATERIALIZED VIEW IF NOT EXISTS user_financial_summary AS
SELECT
    u.id as user_id,
    u.email,
    COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as total_income,
    COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as total_expenses,
    COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0) as net_worth,
    COUNT(t.id) as total_transactions,
    COUNT(CASE WHEN t.reconciled = false THEN 1 END) as pending_reconciliations,
    COUNT(CASE WHEN fg.is_active = true THEN 1 END) as active_goals,
    COUNT(CASE WHEN fg.is_active = false AND fg.current_amount >= fg.target_amount THEN 1 END) as completed_goals,
    MAX(t.date) as last_transaction_date,
    CURRENT_DATE as summary_date
FROM users u
LEFT JOIN transactions t ON u.id = t.user_id
LEFT JOIN financial_goals fg ON u.id = fg.user_id
GROUP BY u.id, u.email;

-- Create index on materialized view
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_financial_summary_user_id
ON user_financial_summary(user_id);

-- Materialized view for monthly trends (refreshed daily)
CREATE MATERIALIZED VIEW IF NOT EXISTS monthly_trends_summary AS
SELECT
    u.id as user_id,
    DATE_TRUNC('month', t.date)::DATE as month_date,
    COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as total_income,
    COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as total_expenses,
    COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0) as net_balance,
    COUNT(t.id) as transaction_count
FROM users u
LEFT JOIN transactions t ON u.id = t.user_id
WHERE t.date >= CURRENT_DATE - INTERVAL '24 months'
GROUP BY u.id, DATE_TRUNC('month', t.date);

-- Create index on materialized view
CREATE UNIQUE INDEX IF NOT EXISTS idx_monthly_trends_summary_user_month
ON monthly_trends_summary(user_id, month_date);

-- =====================================================
-- PERFORMANCE OPTIMIZATION SETTINGS
-- =====================================================

-- Update table statistics for better query planning
ANALYZE transactions;
ANALYZE financial_goals;
ANALYZE brazilian_stocks;
ANALYZE fii_funds;
ANALYZE brazilian_bonds;
ANALYZE users;

-- =====================================================
-- COMMENTS FOR DOCUMENTATION
-- =====================================================

COMMENT ON FUNCTION calculate_monthly_summary IS 'Calculates monthly financial summary for a user within a date range';
COMMENT ON FUNCTION get_top_spending_categories IS 'Returns top spending categories for a user within a date range';
COMMENT ON FUNCTION calculate_goal_progress IS 'Calculates progress for all active financial goals of a user';
COMMENT ON FUNCTION get_transaction_trends IS 'Returns monthly transaction trends for a user over specified months';

COMMENT ON MATERIALIZED VIEW user_financial_summary IS 'Pre-calculated financial summary for all users, refreshed daily';
COMMENT ON MATERIALIZED VIEW monthly_trends_summary IS 'Pre-calculated monthly trends for all users, refreshed daily';

-- =====================================================
-- REFRESH SCHEDULE (to be set up with pg_cron or similar)
-- =====================================================

-- Note: These would typically be set up with pg_cron or a scheduled job
-- REFRESH MATERIALIZED VIEW CONCURRENTLY user_financial_summary;
-- REFRESH MATERIALIZED VIEW CONCURRENTLY monthly_trends_summary;
