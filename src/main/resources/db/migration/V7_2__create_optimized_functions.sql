-- Migration V7.2: Create optimized functions and views (transactional)
-- This migration creates functions and views that can run in transactions

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
    -- Get total expenses for percentage calculation
    SELECT COALESCE(SUM(amount), 0) INTO total_expenses
    FROM transactions
    WHERE user_id = p_user_id
        AND type = 'EXPENSE'
        AND date::date BETWEEN p_start_date AND p_end_date;

    RETURN QUERY
    SELECT
        c.name as category_name,
        COALESCE(SUM(t.amount), 0) as total_amount,
        COUNT(*) as transaction_count,
        CASE
            WHEN total_expenses > 0 THEN (SUM(t.amount) / total_expenses) * 100
            ELSE 0
        END as percentage
    FROM transactions t
    JOIN transaction_categories c ON t.category_id = c.id
    WHERE t.user_id = p_user_id
        AND t.type = 'EXPENSE'
        AND t.date::date BETWEEN p_start_date AND p_end_date
    GROUP BY c.id, c.name
    ORDER BY total_amount DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- Function to calculate financial goal progress
CREATE OR REPLACE FUNCTION calculate_goal_progress(
    p_goal_id BIGINT
) RETURNS TABLE (
    goal_name VARCHAR(255),
    target_amount NUMERIC(19,2),
    current_amount NUMERIC(19,2),
    progress_percentage NUMERIC(5,2),
    days_remaining INTEGER,
    is_on_track BOOLEAN
) AS $$
DECLARE
    goal_record RECORD;
    current_amount_calc NUMERIC(19,2);
    days_remaining_calc INTEGER;
    progress_percentage_calc NUMERIC(5,2);
    is_on_track_calc BOOLEAN;
BEGIN
    -- Get goal details
    SELECT * INTO goal_record
    FROM financial_goals
    WHERE id = p_goal_id;

    IF NOT FOUND THEN
        RETURN;
    END IF;

    -- Calculate current amount based on goal type
    IF goal_record.goal_type = 'SAVINGS' THEN
        SELECT COALESCE(SUM(amount), 0) INTO current_amount_calc
        FROM transactions
        WHERE user_id = goal_record.user_id
            AND type = 'INCOME'
            AND date::date BETWEEN goal_record.start_date AND CURRENT_DATE;
    ELSIF goal_record.goal_type = 'DEBT_PAYOFF' THEN
        SELECT COALESCE(SUM(amount), 0) INTO current_amount_calc
        FROM transactions
        WHERE user_id = goal_record.user_id
            AND type = 'EXPENSE'
            AND category_id IN (
                SELECT id FROM transaction_categories
                WHERE name ILIKE '%debt%' OR name ILIKE '%loan%'
            )
            AND date::date BETWEEN goal_record.start_date AND CURRENT_DATE;
    ELSE
        current_amount_calc := 0;
    END IF;

    -- Calculate days remaining
    days_remaining_calc := EXTRACT(DAYS FROM (goal_record.deadline - CURRENT_DATE))::INTEGER;

    -- Calculate progress percentage
    IF goal_record.target_amount > 0 THEN
        progress_percentage_calc := (current_amount_calc / goal_record.target_amount) * 100;
    ELSE
        progress_percentage_calc := 0;
    END IF;

    -- Determine if on track (simple calculation based on time elapsed)
    is_on_track_calc := CASE
        WHEN days_remaining_calc <= 0 THEN current_amount_calc >= goal_record.target_amount
        ELSE (current_amount_calc / goal_record.target_amount) >=
             (EXTRACT(DAYS FROM (CURRENT_DATE - goal_record.start_date))::NUMERIC /
              EXTRACT(DAYS FROM (goal_record.deadline - goal_record.start_date))::NUMERIC)
    END;

    RETURN QUERY
    SELECT
        goal_record.name,
        goal_record.target_amount,
        current_amount_calc,
        progress_percentage_calc,
        days_remaining_calc,
        is_on_track_calc;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- MATERIALIZED VIEWS FOR DASHBOARD PERFORMANCE
-- =====================================================

-- Materialized view for monthly transaction summaries
CREATE MATERIALIZED VIEW IF NOT EXISTS monthly_transaction_summary AS
SELECT
    user_id,
    DATE_TRUNC('month', date) as month,
    type,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    AVG(amount) as avg_amount,
    MIN(amount) as min_amount,
    MAX(amount) as max_amount
FROM transactions
GROUP BY user_id, DATE_TRUNC('month', date), type;

-- Create index on the materialized view
CREATE UNIQUE INDEX IF NOT EXISTS idx_monthly_summary_user_month_type
ON monthly_transaction_summary(user_id, month, type);

-- Materialized view for category spending analysis
CREATE MATERIALIZED VIEW IF NOT EXISTS category_spending_analysis AS
SELECT
    t.user_id,
    c.name as category_name,
    t.type,
    DATE_TRUNC('month', t.date) as month,
    COUNT(*) as transaction_count,
    SUM(t.amount) as total_amount,
    AVG(t.amount) as avg_amount
FROM transactions t
JOIN transaction_categories c ON t.category_id = c.id
GROUP BY t.user_id, c.name, t.type, DATE_TRUNC('month', t.date);

-- Create index on the materialized view
CREATE UNIQUE INDEX IF NOT EXISTS idx_category_analysis_user_category_type_month
ON category_spending_analysis(user_id, category_name, type, month);

-- =====================================================
-- REFRESH FUNCTIONS FOR MATERIALIZED VIEWS
-- =====================================================

-- Function to refresh all materialized views
CREATE OR REPLACE FUNCTION refresh_dashboard_views()
RETURNS VOID AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY monthly_transaction_summary;
    REFRESH MATERIALIZED VIEW CONCURRENTLY category_spending_analysis;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- PERFORMANCE OPTIMIZATION COMMENTS
-- =====================================================

-- These functions and views are designed to:
-- 1. Reduce query complexity for common dashboard operations
-- 2. Provide pre-calculated aggregations for better performance
-- 3. Enable efficient filtering and sorting on large datasets
-- 4. Support real-time dashboard updates with minimal database load
