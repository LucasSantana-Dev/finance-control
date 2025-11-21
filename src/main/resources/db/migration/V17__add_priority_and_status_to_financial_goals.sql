-- Migration V17: Add priority and status fields to financial_goals table
-- This aligns the backend schema with Supabase goals table structure

-- Add priority column
ALTER TABLE financial_goals
ADD COLUMN IF NOT EXISTS priority VARCHAR(10) DEFAULT 'medium'
CHECK (priority IN ('low', 'medium', 'high'));

-- Add status column
ALTER TABLE financial_goals
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'active'
CHECK (status IN ('active', 'completed', 'paused', 'cancelled'));

-- Update existing goals to have default values
UPDATE financial_goals
SET priority = 'medium'
WHERE priority IS NULL;

UPDATE financial_goals
SET status = CASE
    WHEN is_completed = true THEN 'completed'
    WHEN is_active = false THEN 'paused'
    ELSE 'active'
END
WHERE status IS NULL;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_financial_goals_priority ON financial_goals(priority);
CREATE INDEX IF NOT EXISTS idx_financial_goals_status ON financial_goals(status);
CREATE INDEX IF NOT EXISTS idx_financial_goals_user_status ON financial_goals(user_id, status);
CREATE INDEX IF NOT EXISTS idx_financial_goals_user_priority ON financial_goals(user_id, priority);

-- Add comments for documentation
COMMENT ON COLUMN financial_goals.priority IS 'Goal priority: low, medium, high';
COMMENT ON COLUMN financial_goals.status IS 'Goal status: active, completed, paused, cancelled';
