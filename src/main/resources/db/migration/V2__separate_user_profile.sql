-- Migration to separate user essential data from profile data
-- This improves security, performance, and maintainability

-- Create user_profiles table
CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(100) NOT NULL,
    bio VARCHAR(500),
    phone VARCHAR(20),
    country VARCHAR(100),
    avatar_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(user_id)
);

-- Migrate existing data
INSERT INTO user_profiles (user_id, full_name, created_at, updated_at)
SELECT id, full_name, created_at, updated_at FROM users;

-- Remove full_name from users table (now in user_profiles)
ALTER TABLE users DROP COLUMN full_name;

-- Add indexes for performance
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_full_name ON user_profiles(full_name);
CREATE INDEX idx_user_profiles_country ON user_profiles(country);

-- Add comments for documentation
COMMENT ON TABLE user_profiles IS 'User profile information separated from essential authentication data';
COMMENT ON COLUMN user_profiles.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN user_profiles.full_name IS 'User full name (moved from users table)';
COMMENT ON COLUMN user_profiles.bio IS 'User biography or description';
COMMENT ON COLUMN user_profiles.phone IS 'User phone number';
COMMENT ON COLUMN user_profiles.country IS 'User country (used to determine currency and timezone)';
COMMENT ON COLUMN user_profiles.avatar_url IS 'URL to user avatar image'; 