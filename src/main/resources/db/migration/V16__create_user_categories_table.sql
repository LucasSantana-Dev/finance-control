-- Migration V16: Create user_categories table
-- This table stores user-specific transaction categories with color and icon support
-- Separate from global transaction_categories to maintain backward compatibility

CREATE TABLE user_categories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('income', 'expense')),
    color VARCHAR(7),
    icon VARCHAR(50),
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(user_id, name, type)
);

-- Create indexes for better query performance
CREATE INDEX idx_user_categories_user_id ON user_categories(user_id);
CREATE INDEX idx_user_categories_user_type ON user_categories(user_id, type);
CREATE INDEX idx_user_categories_type ON user_categories(type);
CREATE INDEX idx_user_categories_is_default ON user_categories(user_id, is_default);

-- Add comments for documentation
COMMENT ON TABLE user_categories IS 'User-specific transaction categories with color and icon support';
COMMENT ON COLUMN user_categories.type IS 'Category type: income or expense';
COMMENT ON COLUMN user_categories.color IS 'Hex color code for category display (e.g., #10b981)';
COMMENT ON COLUMN user_categories.icon IS 'Icon identifier for category display';
COMMENT ON COLUMN user_categories.is_default IS 'Whether this is a default category for the user';
