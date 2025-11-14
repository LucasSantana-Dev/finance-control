-- Add completion fields to financial_goals table
ALTER TABLE financial_goals ADD COLUMN completed_date TIMESTAMP;
ALTER TABLE financial_goals ADD COLUMN is_completed BOOLEAN DEFAULT FALSE;
ALTER TABLE financial_goals ADD COLUMN completion_notes VARCHAR(1000);
ALTER TABLE financial_goals ADD COLUMN achievement_notes VARCHAR(1000);
ALTER TABLE financial_goals ADD COLUMN actual_savings DECIMAL(19,2);
ALTER TABLE financial_goals ADD COLUMN actual_investment DECIMAL(19,2);

-- Add reconciliation fields to transactions table
ALTER TABLE transactions ADD COLUMN reconciled_amount DECIMAL(19,2);
ALTER TABLE transactions ADD COLUMN reconciliation_date TIMESTAMP;
ALTER TABLE transactions ADD COLUMN is_reconciled BOOLEAN DEFAULT FALSE;
ALTER TABLE transactions ADD COLUMN reconciliation_notes VARCHAR(1000);
ALTER TABLE transactions ADD COLUMN bank_reference VARCHAR(100);
ALTER TABLE transactions ADD COLUMN external_reference VARCHAR(100);
