-- Migration V17: Add priority and status fields to financial_goals table
-- This aligns the backend schema with Supabase goals table structure
-- Note: Uses uppercase values to match @Enumerated(EnumType.STRING) which stores enum names

-- Add priority column
ALTER TABLE financial_goals
ADD COLUMN IF NOT EXISTS priority VARCHAR(10) DEFAULT 'MEDIUM'
CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'));

-- Add status column
ALTER TABLE financial_goals
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE'
CHECK (status IN ('ACTIVE', 'COMPLETED', 'PAUSED', 'CANCELLED'));

-- Update existing goals to have default values
UPDATE financial_goals
SET priority = 'MEDIUM'
WHERE priority IS NULL;

-- Set default status to ACTIVE for all existing goals
-- Note: Application logic will update status based on completion/active state as needed
UPDATE financial_goals
SET status = 'ACTIVE'
WHERE status IS NULL;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_financial_goals_priority ON financial_goals(priority);
CREATE INDEX IF NOT EXISTS idx_financial_goals_status ON financial_goals(status);
CREATE INDEX IF NOT EXISTS idx_financial_goals_user_status ON financial_goals(user_id, status);
CREATE INDEX IF NOT EXISTS idx_financial_goals_user_priority ON financial_goals(user_id, priority);

-- Add comments for documentation
COMMENT ON COLUMN financial_goals.priority IS 'Goal priority: LOW, MEDIUM, HIGH (matches enum names)';
COMMENT ON COLUMN financial_goals.status IS 'Goal status: ACTIVE, COMPLETED, PAUSED, CANCELLED (matches enum names)';
