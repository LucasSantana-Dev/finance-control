-- Test Users
INSERT INTO users (id, email, password, is_active, created_at, updated_at) VALUES 
(1, 'john.doe@example.com', '$2a$10$dummy.hash.for.testing', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'jane.smith@example.com', '$2a$10$dummy.hash.for.testing', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Test Transaction Categories
INSERT INTO transaction_categories (id, name, created_at, updated_at) VALUES 
(1, 'Food', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Transport', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Entertainment', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Salary', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Investment', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Test Transaction Subcategories
INSERT INTO transaction_subcategories (id, name, category_id, created_at, updated_at) VALUES 
(1, 'Restaurants', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Groceries', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Public Transport', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Movies', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Salary', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Stocks', 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Test Transaction Source Entities
INSERT INTO transaction_source_entity (id, name, description, source_type, bank_name, account_number, card_type, card_last_four, account_balance, is_active, user_id, created_at, updated_at) VALUES 
(1, 'Nubank Credit Card', 'Main credit card', 'CREDIT_CARD', 'Nubank', NULL, 'Credit', '1234', 5000.00, true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Itaú Savings Account', 'Main savings account', 'BANK_TRANSACTION', 'Itaú', '12345-6', NULL, NULL, 15000.00, true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'PIX Wallet', 'Personal PIX wallet', 'PIX', NULL, NULL, NULL, NULL, 2000.00, true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Jane Credit Card', 'Jane main card', 'CREDIT_CARD', 'Santander', NULL, 'Credit', '5678', 3000.00, true, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Test Transaction Responsibles
INSERT INTO transaction_responsibles (id, name, created_at, updated_at) VALUES 
(1, 'John Doe', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Jane Smith', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Shared', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Test Transactions
INSERT INTO transactions (id, type, subtype, source, description, amount, installments, date, created_at, updated_at, user_id, category_id, subcategory_id, source_entity_id) VALUES 
(1, 'EXPENSE', 'VARIABLE', 'CREDIT_CARD', 'Lunch at restaurant', 45.50, 1, DATEADD('DAY', -1, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1, 1, 1),
(2, 'INCOME', 'FIXED', 'BANK_TRANSACTION', 'Monthly salary', 5000.00, 1, DATEADD('DAY', -5, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 4, 5, 2),
(3, 'EXPENSE', 'VARIABLE', 'PIX', 'Movie tickets', 60.00, 1, DATEADD('DAY', -2, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 3, 4, 3),
(4, 'EXPENSE', 'VARIABLE', 'CREDIT_CARD', 'Groceries', 120.00, 1, DATEADD('DAY', -3, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 1, 2, 4);

-- Test Transaction Responsibilities
INSERT INTO transaction_responsibilities (id, transaction_id, responsible_id, percentage, notes, created_at, updated_at) VALUES 
(1, 1, 1, 100.00, 'Personal expense', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 1, 100.00, 'Personal income', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3, 3, 50.00, 'Shared expense', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 3, 2, 50.00, 'Shared expense', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 4, 2, 100.00, 'Personal expense', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Test Financial Goals
INSERT INTO financial_goals (id, name, description, goal_type, target_amount, current_amount, deadline, is_active, auto_calculate, account_id, user_id, created_at, updated_at) VALUES 
(1, 'Vacation Fund', 'Save for summer vacation', 'SAVINGS', 5000.00, 1500.00, '2024-06-30', true, true, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Emergency Fund', 'Emergency savings', 'SAVINGS', 10000.00, 8000.00, '2024-12-31', true, false, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'New Car', 'Save for new car', 'PURCHASE', 30000.00, 5000.00, '2025-06-30', true, true, 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Jane Vacation', 'Jane vacation fund', 'SAVINGS', 3000.00, 1000.00, '2024-08-31', true, true, 4, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 