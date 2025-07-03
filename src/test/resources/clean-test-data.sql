-- Clean test data in reverse order of dependencies
DELETE FROM financial_goals;
DELETE FROM transaction_responsibilities;
DELETE FROM transactions;
DELETE FROM transaction_source_entity;
DELETE FROM transaction_subcategories;
DELETE FROM transaction_categories;
DELETE FROM transaction_responsibles;
DELETE FROM users;

-- Reset sequences to avoid primary key conflicts
ALTER TABLE financial_goals ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transaction_responsibilities ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transactions ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transaction_source_entity ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transaction_subcategories ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transaction_categories ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transaction_responsibles ALTER COLUMN id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN id RESTART WITH 1; 