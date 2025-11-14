-- Open Finance Brasil Integration Tables
-- All tables will be stored in Supabase PostgreSQL

-- Open Finance Institutions Registry
CREATE TABLE IF NOT EXISTS open_finance_institutions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    api_base_url VARCHAR(512) NOT NULL,
    authorization_url VARCHAR(512) NOT NULL,
    token_url VARCHAR(512) NOT NULL,
    certificate_required BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uq_open_finance_institution_code UNIQUE (code)
);

-- Open Finance Consents (OAuth 2.0 tokens and consent status)
CREATE TABLE IF NOT EXISTS open_finance_consents (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    institution_id BIGINT NOT NULL REFERENCES open_finance_institutions(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    scopes TEXT NOT NULL,
    access_token TEXT,
    refresh_token TEXT,
    expires_at TIMESTAMP,
    revoked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_consent_status CHECK (status IN ('PENDING', 'AUTHORIZED', 'REVOKED', 'EXPIRED', 'FAILED'))
);

-- Connected Accounts (bank accounts linked via Open Finance)
CREATE TABLE IF NOT EXISTS connected_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    consent_id BIGINT NOT NULL REFERENCES open_finance_consents(id) ON DELETE CASCADE,
    institution_id BIGINT NOT NULL REFERENCES open_finance_institutions(id) ON DELETE CASCADE,
    external_account_id VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    account_number VARCHAR(100),
    branch VARCHAR(50),
    account_holder_name VARCHAR(255),
    balance NUMERIC(19,2),
    currency VARCHAR(3) DEFAULT 'BRL',
    last_synced_at TIMESTAMP,
    sync_status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uq_connected_account_external UNIQUE (institution_id, external_account_id),
    CONSTRAINT chk_sync_status CHECK (sync_status IN ('PENDING', 'SYNCING', 'SUCCESS', 'FAILED', 'DISABLED'))
);

-- Account Sync Logs (tracking sync operations)
CREATE TABLE IF NOT EXISTS account_sync_logs (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES connected_accounts(id) ON DELETE CASCADE,
    sync_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    records_imported INTEGER DEFAULT 0,
    error_message TEXT,
    synced_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_sync_log_status CHECK (status IN ('SUCCESS', 'FAILED', 'PARTIAL')),
    CONSTRAINT chk_sync_type CHECK (sync_type IN ('BALANCE', 'TRANSACTIONS', 'FULL'))
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_open_finance_consents_user_id ON open_finance_consents(user_id);
CREATE INDEX IF NOT EXISTS idx_open_finance_consents_institution_id ON open_finance_consents(institution_id);
CREATE INDEX IF NOT EXISTS idx_open_finance_consents_status ON open_finance_consents(status);
CREATE INDEX IF NOT EXISTS idx_open_finance_consents_expires_at ON open_finance_consents(expires_at);

CREATE INDEX IF NOT EXISTS idx_connected_accounts_user_id ON connected_accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_connected_accounts_consent_id ON connected_accounts(consent_id);
CREATE INDEX IF NOT EXISTS idx_connected_accounts_institution_id ON connected_accounts(institution_id);
CREATE INDEX IF NOT EXISTS idx_connected_accounts_sync_status ON connected_accounts(sync_status);
CREATE INDEX IF NOT EXISTS idx_connected_accounts_last_synced_at ON connected_accounts(last_synced_at);

CREATE INDEX IF NOT EXISTS idx_account_sync_logs_account_id ON account_sync_logs(account_id);
CREATE INDEX IF NOT EXISTS idx_account_sync_logs_sync_type ON account_sync_logs(sync_type);
CREATE INDEX IF NOT EXISTS idx_account_sync_logs_status ON account_sync_logs(status);
CREATE INDEX IF NOT EXISTS idx_account_sync_logs_synced_at ON account_sync_logs(synced_at);

CREATE INDEX IF NOT EXISTS idx_open_finance_institutions_code ON open_finance_institutions(code);
CREATE INDEX IF NOT EXISTS idx_open_finance_institutions_is_active ON open_finance_institutions(is_active);

-- Comments for documentation
COMMENT ON TABLE open_finance_institutions IS 'Registry of Open Finance Brasil participating institutions';
COMMENT ON TABLE open_finance_consents IS 'OAuth 2.0 consents and tokens for Open Finance API access';
COMMENT ON TABLE connected_accounts IS 'Bank accounts connected via Open Finance with sync status';
COMMENT ON TABLE account_sync_logs IS 'Logs of account synchronization operations';

COMMENT ON COLUMN open_finance_consents.status IS 'Consent status: PENDING, AUTHORIZED, REVOKED, EXPIRED, FAILED';
COMMENT ON COLUMN open_finance_consents.scopes IS 'Comma-separated list of OAuth scopes granted';
COMMENT ON COLUMN connected_accounts.sync_status IS 'Sync status: PENDING, SYNCING, SUCCESS, FAILED, DISABLED';
COMMENT ON COLUMN account_sync_logs.sync_type IS 'Type of sync: BALANCE, TRANSACTIONS, FULL';
