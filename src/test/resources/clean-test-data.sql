-- Clean test data in reverse order of dependencies
DELETE FROM dashboard_alerts;
DELETE FROM dashboard_preferences;
DELETE FROM dashboard_cache;
DELETE FROM investments;
DELETE FROM financial_goals;
DELETE FROM transaction_responsibilities;
DELETE FROM transactions;
DELETE FROM transaction_source_entity;
DELETE FROM transaction_subcategories;
DELETE FROM transaction_categories;
DELETE FROM transaction_responsibles;
DELETE FROM brazilian_bonds;
DELETE FROM fii_funds;
DELETE FROM brazilian_stocks;
DELETE FROM market_indicators;
DELETE FROM user_profiles;
DELETE FROM users;

-- Reset sequences to avoid primary key conflicts
ALTER TABLE dashboard_alerts ALTER COLUMN id RESTART WITH 1;
ALTER TABLE dashboard_preferences ALTER COLUMN id RESTART WITH 1;
ALTER TABLE dashboard_cache ALTER COLUMN id RESTART WITH 1;
ALTER TABLE investments ALTER COLUMN id RESTART WITH 1;
ALTER TABLE financial_goals ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transaction_responsibilities ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transactions ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transaction_source_entity ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transaction_subcategories ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transaction_categories ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transaction_responsibles ALTER COLUMN id RESTART WITH 1;
ALTER TABLE brazilian_bonds ALTER COLUMN id RESTART WITH 1;
ALTER TABLE fii_funds ALTER COLUMN id RESTART WITH 1;
ALTER TABLE brazilian_stocks ALTER COLUMN id RESTART WITH 1;
ALTER TABLE market_indicators ALTER COLUMN id RESTART WITH 1;
ALTER TABLE user_profiles ALTER COLUMN id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN id RESTART WITH 1; 