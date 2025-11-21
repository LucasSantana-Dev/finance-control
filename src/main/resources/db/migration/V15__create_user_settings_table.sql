-- Migration V15: Create user_settings table
-- This table stores user preferences and settings

CREATE TABLE user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    currency_format VARCHAR(3) DEFAULT 'BRL' CHECK (currency_format IN ('BRL', 'USD', 'EUR')),
    date_format VARCHAR(20) DEFAULT 'DD/MM/YYYY' CHECK (date_format IN ('DD/MM/YYYY', 'MM/DD/YYYY', 'YYYY-MM-DD')),
    notification_email BOOLEAN DEFAULT true,
    notification_push BOOLEAN DEFAULT false,
    notification_transactions BOOLEAN DEFAULT true,
    notification_goals BOOLEAN DEFAULT true,
    notification_weekly_summary BOOLEAN DEFAULT true,
    theme VARCHAR(10) DEFAULT 'light' CHECK (theme IN ('light', 'dark', 'system')),
    language VARCHAR(10) DEFAULT 'pt-BR',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create index for user lookup
CREATE INDEX idx_user_settings_user_id ON user_settings(user_id);

-- Add comments for documentation
COMMENT ON TABLE user_settings IS 'User preferences and application settings';
COMMENT ON COLUMN user_settings.currency_format IS 'Currency format: BRL, USD, EUR';
COMMENT ON COLUMN user_settings.date_format IS 'Date format: DD/MM/YYYY, MM/DD/YYYY, YYYY-MM-DD';
COMMENT ON COLUMN user_settings.theme IS 'UI theme: light, dark, system';
COMMENT ON COLUMN user_settings.language IS 'User interface language (default: pt-BR)';
