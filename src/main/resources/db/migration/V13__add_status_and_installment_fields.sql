-- Migration V13: Add status and installment fields to transactions table
-- This aligns the Java backend schema with the Supabase frontend schema

-- Add status column
ALTER TABLE transactions
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'paid'
CHECK (status IN ('planned', 'pending', 'paid', 'cancelled'));

-- Add installment fields
ALTER TABLE transactions
ADD COLUMN IF NOT EXISTS installment_group_id UUID;

ALTER TABLE transactions
ADD COLUMN IF NOT EXISTS installment_number INTEGER;

ALTER TABLE transactions
ADD COLUMN IF NOT EXISTS total_installments INTEGER;

ALTER TABLE transactions
ADD COLUMN IF NOT EXISTS installment_amount NUMERIC(10,2);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_installment_group_id ON transactions(installment_group_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status_installment ON transactions(status, installment_group_id)
WHERE installment_group_id IS NOT NULL;

-- Add comments for documentation
COMMENT ON COLUMN transactions.status IS 'Transaction status: planned (future), pending (due), paid (completed), cancelled';
COMMENT ON COLUMN transactions.installment_group_id IS 'UUID linking related installment transactions together';
COMMENT ON COLUMN transactions.installment_number IS 'Installment number (1, 2, 3...) within the group';
COMMENT ON COLUMN transactions.total_installments IS 'Total number of installments in the group';
COMMENT ON COLUMN transactions.installment_amount IS 'Amount for this specific installment';
