-- Add updated_at column to transaction_responsibilities table
-- This column was missing but is required by the BaseModel entity

ALTER TABLE transaction_responsibilities
ADD COLUMN updated_at TIMESTAMP;
