-- Migration V7.1: Create concurrent indexes (non-transactional)
-- This migration creates indexes concurrently to avoid blocking operations
-- These indexes are created outside of transactions for better performance

-- =====================================================
-- OPTIMIZED INDEXES FOR COMMON QUERY PATTERNS
-- =====================================================

-- Composite indexes for transaction queries (most common operations)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_user_type_date_amount
ON transactions(user_id, type, date DESC, amount DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_user_date_type_category
ON transactions(user_id, date DESC, type, category_id);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_user_reconciled_date
ON transactions(user_id, is_reconciled, date DESC)
WHERE is_reconciled = false;

-- Partial indexes for active records (reduces index size and improves performance)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_active_user_date
ON transactions(user_id, date DESC)
WHERE is_reconciled = false;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_financial_goals_active_user_deadline
ON financial_goals(user_id, deadline)
WHERE is_active = true;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_brazilian_stocks_active_user_ticker
ON brazilian_stocks(user_id, ticker)
WHERE is_active = true;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_fii_funds_active_user_ticker
ON fii_funds(user_id, ticker)
WHERE is_active = true;

-- Indexes for dashboard and analytics queries
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_user_category_type_date
ON transactions(user_id, category_id, type, date DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_transactions_user_subcategory_type_date
ON transactions(user_id, subcategory_id, type, date DESC);

-- Indexes for Brazilian market data queries
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_market_indicators_type_date
ON market_indicators(type, date DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_brazilian_stocks_ticker_date
ON brazilian_stocks(ticker, date DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_fiis_ticker_date
ON fiis(ticker, date DESC);

-- Indexes for user profile and authentication
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email_active
ON users(email)
WHERE is_active = true;

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_profiles_user_id_active
ON user_profiles(user_id)
WHERE is_active = true;
