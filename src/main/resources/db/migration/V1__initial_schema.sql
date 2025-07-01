CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE transaction_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE transaction_subcategories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    category_id BIGINT REFERENCES transaction_categories(id)
);

CREATE TABLE transaction_source_entity (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    source_type VARCHAR(50) NOT NULL DEFAULT 'OTHER',
    bank_name VARCHAR(255),
    account_number VARCHAR(100),
    card_type VARCHAR(100),
    card_last_four VARCHAR(4),
    account_balance NUMERIC(19,2),
    is_active BOOLEAN DEFAULT TRUE,
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE transaction_responsibles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    subtype VARCHAR(50) NOT NULL,
    source VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    installments INTEGER,
    date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES users(id),
    category_id BIGINT NOT NULL REFERENCES transaction_categories(id),
    subcategory_id BIGINT REFERENCES transaction_subcategories(id),
    source_entity_id BIGINT REFERENCES transaction_source_entity(id)
);

CREATE TABLE transaction_responsibilities (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    responsible_id BIGINT NOT NULL REFERENCES transaction_responsibles(id),
    percentage NUMERIC(5,2) NOT NULL,
    calculated_amount NUMERIC(19,2),
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_transaction_responsible UNIQUE (transaction_id, responsible_id)
);

CREATE TABLE financial_goals (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    goal_type VARCHAR(50) NOT NULL,
    target_amount NUMERIC(19,2) NOT NULL,
    current_amount NUMERIC(19,2) DEFAULT 0,
    deadline DATE,
    is_active BOOLEAN DEFAULT TRUE,
    auto_calculate BOOLEAN DEFAULT FALSE,
    account_id BIGINT REFERENCES transaction_source_entity(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Indexes for better query performance

-- Users table indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_full_name ON users(full_name);

-- Transaction category indexes
CREATE INDEX idx_transaction_category_name ON transaction_categories(name);

-- Transaction subcategory indexes
CREATE INDEX idx_transaction_subcategory_category ON transaction_subcategories(category_id);
CREATE INDEX idx_transaction_subcategory_name ON transaction_subcategories(name);

-- Transaction source entity indexes
CREATE INDEX idx_transaction_source_entity_user ON transaction_source_entity(user_id);
CREATE INDEX idx_transaction_source_entity_active ON transaction_source_entity(is_active);
CREATE INDEX idx_transaction_source_entity_type ON transaction_source_entity(source_type);
CREATE INDEX idx_transaction_source_entity_bank ON transaction_source_entity(bank_name);
CREATE INDEX idx_transaction_source_entity_user_active ON transaction_source_entity(user_id, is_active);

-- Transaction responsible indexes
CREATE INDEX idx_transaction_responsibles_name ON transaction_responsibles(name);

-- Transactions table indexes (most important for performance)
CREATE INDEX idx_transactions_user ON transactions(user_id);
CREATE INDEX idx_transactions_date ON transactions(date);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE INDEX idx_transactions_source_entity ON transactions(source_entity_id);
CREATE INDEX idx_transactions_user_date ON transactions(user_id, date);
CREATE INDEX idx_transactions_user_type ON transactions(user_id, type);
CREATE INDEX idx_transactions_user_category ON transactions(user_id, category_id);
CREATE INDEX idx_transactions_amount ON transactions(amount);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

-- Transaction responsibility indexes
CREATE INDEX idx_transaction_responsibilities_transaction ON transaction_responsibilities(transaction_id);
CREATE INDEX idx_transaction_responsibilities_responsible ON transaction_responsibilities(responsible_id);

-- Financial goals indexes
CREATE INDEX idx_financial_goals_user ON financial_goals(user_id);
CREATE INDEX idx_financial_goals_account ON financial_goals(account_id);
CREATE INDEX idx_financial_goals_active ON financial_goals(is_active);
CREATE INDEX idx_financial_goals_type ON financial_goals(goal_type);
CREATE INDEX idx_financial_goals_deadline ON financial_goals(deadline);
CREATE INDEX idx_financial_goals_user_active ON financial_goals(user_id, is_active);
CREATE INDEX idx_financial_goals_user_type ON financial_goals(user_id, goal_type); 