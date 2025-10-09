-- Dashboard cache tables for improved performance
-- These tables can store pre-calculated dashboard metrics

-- Table for caching dashboard summary data
CREATE TABLE IF NOT EXISTS dashboard_cache (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    cache_key VARCHAR(255) NOT NULL,
    cache_data JSONB NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_dashboard_cache_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_dashboard_cache_user_key UNIQUE (user_id, cache_key)
);

-- Index for faster cache lookups
CREATE INDEX IF NOT EXISTS idx_dashboard_cache_user_expires ON dashboard_cache(user_id, expires_at);
CREATE INDEX IF NOT EXISTS idx_dashboard_cache_key ON dashboard_cache(cache_key);

-- Table for storing dashboard preferences
CREATE TABLE IF NOT EXISTS dashboard_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    default_period_months INTEGER DEFAULT 12,
    chart_colors JSONB,
    visible_widgets JSONB,
    refresh_interval_minutes INTEGER DEFAULT 15,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_dashboard_preferences_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table for storing dashboard alerts and notifications
CREATE TABLE IF NOT EXISTS dashboard_alerts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    alert_message TEXT NOT NULL,
    alert_data JSONB,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_dashboard_alerts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Index for faster alert queries
CREATE INDEX IF NOT EXISTS idx_dashboard_alerts_user_read ON dashboard_alerts(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_dashboard_alerts_type ON dashboard_alerts(alert_type);

-- Add comments for documentation
COMMENT ON TABLE dashboard_cache IS 'Cache table for storing pre-calculated dashboard metrics';
COMMENT ON TABLE dashboard_preferences IS 'User preferences for dashboard customization';
COMMENT ON TABLE dashboard_alerts IS 'Dashboard alerts and notifications for users';

COMMENT ON COLUMN dashboard_cache.cache_key IS 'Unique key for cache entry (e.g., summary_2024_01)';
COMMENT ON COLUMN dashboard_cache.cache_data IS 'JSON data containing cached dashboard metrics';
COMMENT ON COLUMN dashboard_cache.expires_at IS 'When this cache entry expires';

COMMENT ON COLUMN dashboard_preferences.default_period_months IS 'Default number of months for trend charts';
COMMENT ON COLUMN dashboard_preferences.chart_colors IS 'Custom chart colors in JSON format';
COMMENT ON COLUMN dashboard_preferences.visible_widgets IS 'Which dashboard widgets are visible';
COMMENT ON COLUMN dashboard_preferences.refresh_interval_minutes IS 'How often to refresh dashboard data';

COMMENT ON COLUMN dashboard_alerts.alert_type IS 'Type of alert (budget_exceeded, goal_achieved, etc.)';
COMMENT ON COLUMN dashboard_alerts.alert_message IS 'Human-readable alert message';
COMMENT ON COLUMN dashboard_alerts.alert_data IS 'Additional alert data in JSON format';
