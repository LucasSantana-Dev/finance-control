-- Add updated_at column to transaction_responsibles table
-- This column was missing but is required by the BaseModel entity
-- Note: Table name is transaction_responsibles (not transaction_responsibilities)

ALTER TABLE transaction_responsibles
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
