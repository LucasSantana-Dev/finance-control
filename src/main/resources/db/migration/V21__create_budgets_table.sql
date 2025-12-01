-- Migration V21: Create budgets table for budget tracking
-- This table stores user budgets with period-based tracking (weekly, monthly, yearly)
-- Aligned with frontend Supabase migration: 20251123054400_create_budgets_table.sql
-- Note: Uses BIGINT IDs and references user_categories (not categories) to match backend schema

CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES user_categories(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    amount NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    period VARCHAR(20) NOT NULL CHECK (period IN ('WEEKLY', 'MONTHLY', 'YEARLY')),
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    alert_threshold NUMERIC(5,2) DEFAULT 80 CHECK (alert_threshold >= 0 AND alert_threshold <= 100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(user_id, name, period, start_date)
);

-- Create indexes for better query performance
CREATE INDEX idx_budgets_user_id ON budgets(user_id);
CREATE INDEX idx_budgets_category_id ON budgets(category_id);
CREATE INDEX idx_budgets_period ON budgets(period);
CREATE INDEX idx_budgets_is_active ON budgets(is_active);
CREATE INDEX idx_budgets_start_date ON budgets(start_date);
CREATE INDEX idx_budgets_user_active ON budgets(user_id, is_active);
CREATE INDEX idx_budgets_user_period ON budgets(user_id, period);
CREATE INDEX idx_budgets_category_period ON budgets(category_id, period) WHERE category_id IS NOT NULL;

-- Add comments for documentation
COMMENT ON TABLE budgets IS 'User budgets for tracking spending limits by period (weekly, monthly, yearly)';
COMMENT ON COLUMN budgets.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN budgets.category_id IS 'Foreign key to user_categories table (optional - null for general budgets)';
COMMENT ON COLUMN budgets.name IS 'Budget name (e.g., "Monthly Groceries", "Weekly Transport")';
COMMENT ON COLUMN budgets.amount IS 'Budget limit amount (must be positive)';
COMMENT ON COLUMN budgets.period IS 'Budget period: WEEKLY, MONTHLY, or YEARLY';
COMMENT ON COLUMN budgets.start_date IS 'Budget start date';
COMMENT ON COLUMN budgets.end_date IS 'Budget end date (nullable for ongoing budgets)';
COMMENT ON COLUMN budgets.is_active IS 'Whether the budget is currently active';
COMMENT ON COLUMN budgets.alert_threshold IS 'Percentage threshold for budget alerts (0-100, default 80)';
