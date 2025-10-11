-- Migration to create unified investments table
-- Replaces separate brazilian_stocks, fii_funds, and brazilian_bonds tables
-- with a single generic investments table

-- Drop existing tables and their indexes
DROP TABLE IF EXISTS brazilian_bonds CASCADE;
DROP TABLE IF EXISTS fii_funds CASCADE;
DROP TABLE IF EXISTS brazilian_stocks CASCADE;

-- Create unified investments table
CREATE TABLE investments (
    id BIGSERIAL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,

    -- Investment classification
    investment_type VARCHAR(50) NOT NULL, -- STOCK, FII, BOND, ETF, CRYPTO, etc.
    investment_subtype VARCHAR(50), -- ORDINARY, PREFERRED, UNIT, TIJOLO, PAPEL, etc.
    market_segment VARCHAR(50), -- NOVO_MERCADO, LEVEL_2, LEVEL_1, etc.
    sector VARCHAR(100), -- Technology, Healthcare, Real Estate, etc.
    industry VARCHAR(100), -- Software, Pharmaceuticals, Shopping Centers, etc.

    -- Market data (from external API)
    current_price DECIMAL(19,2),
    previous_close DECIMAL(19,2),
    day_change DECIMAL(19,2),
    day_change_percent DECIMAL(8,4),
    volume BIGINT,
    market_cap DECIMAL(19,2),

    -- Additional metrics (varies by investment type)
    dividend_yield DECIMAL(8,4), -- For stocks and FIIs
    last_dividend DECIMAL(19,2),
    last_dividend_date DATE,
    net_worth DECIMAL(19,2), -- For FIIs
    p_vp_ratio DECIMAL(8,4), -- For FIIs
    interest_rate DECIMAL(8,4), -- For bonds
    yield_to_maturity DECIMAL(8,4), -- For bonds
    maturity_date DATE, -- For bonds
    credit_rating VARCHAR(10), -- For bonds

    -- Metadata
    exchange VARCHAR(20) DEFAULT 'B3', -- B3, NYSE, NASDAQ, etc.
    currency VARCHAR(3) DEFAULT 'BRL',
    is_active BOOLEAN DEFAULT true,
    last_updated TIMESTAMP,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_investments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_investments_ticker_user UNIQUE (ticker, user_id),

    -- Check constraints
    CONSTRAINT chk_investment_type CHECK (investment_type IN ('STOCK', 'FII', 'BOND', 'ETF', 'CRYPTO', 'COMMODITY', 'CURRENCY', 'OTHER')),
    CONSTRAINT chk_stock_subtype CHECK (
        (investment_type = 'STOCK' AND investment_subtype IN ('ORDINARY', 'PREFERRED', 'UNIT', 'OTHER')) OR
        (investment_type != 'STOCK')
    ),
    CONSTRAINT chk_fii_subtype CHECK (
        (investment_type = 'FII' AND investment_subtype IN ('TIJOLO', 'PAPEL', 'HIBRIDO', 'FUNDO_DE_FUNDOS', 'OTHER')) OR
        (investment_type != 'FII')
    ),
    CONSTRAINT chk_bond_subtype CHECK (
        (investment_type = 'BOND' AND investment_subtype IN ('CDB', 'RDB', 'LCI', 'LCA', 'LF', 'DEBENTURE', 'TESOURO_DIRETO', 'OTHER')) OR
        (investment_type != 'BOND')
    )
);

-- Create indexes for better performance
CREATE INDEX idx_investments_user_id ON investments(user_id);
CREATE INDEX idx_investments_ticker ON investments(ticker);
CREATE INDEX idx_investments_investment_type ON investments(investment_type);
CREATE INDEX idx_investments_investment_subtype ON investments(investment_subtype);
CREATE INDEX idx_investments_sector ON investments(sector);
CREATE INDEX idx_investments_industry ON investments(industry);
CREATE INDEX idx_investments_is_active ON investments(is_active);
CREATE INDEX idx_investments_last_updated ON investments(last_updated);
CREATE INDEX idx_investments_exchange ON investments(exchange);

-- Create composite indexes for common queries
CREATE INDEX idx_investments_active_user_type ON investments(user_id, investment_type, is_active);
CREATE INDEX idx_investments_active_user_ticker ON investments(user_id, ticker, is_active);
CREATE INDEX idx_investments_type_subtype ON investments(investment_type, investment_subtype);

-- Create partial indexes for specific investment types
CREATE INDEX idx_investments_stocks ON investments(user_id, ticker)
WHERE investment_type = 'STOCK' AND is_active = true;

CREATE INDEX idx_investments_fiis ON investments(user_id, ticker)
WHERE investment_type = 'FII' AND is_active = true;

CREATE INDEX idx_investments_bonds ON investments(user_id, ticker)
WHERE investment_type = 'BOND' AND is_active = true;

-- Add comments for documentation
COMMENT ON TABLE investments IS 'Unified table for all types of investments (stocks, FIIs, bonds, ETFs, etc.)';
COMMENT ON COLUMN investments.investment_type IS 'Main investment type: STOCK, FII, BOND, ETF, CRYPTO, COMMODITY, CURRENCY, OTHER';
COMMENT ON COLUMN investments.investment_subtype IS 'Subtype within the main type (e.g., ORDINARY/PREFERRED for stocks, TIJOLO/PAPEL for FIIs)';
COMMENT ON COLUMN investments.market_segment IS 'Market segment: NOVO_MERCADO, LEVEL_2, LEVEL_1, TRADITIONAL, BOVESPA_MAIS, etc.';
COMMENT ON COLUMN investments.sector IS 'Business sector: Technology, Healthcare, Real Estate, Financial, etc.';
COMMENT ON COLUMN investments.industry IS 'Specific industry: Software, Pharmaceuticals, Shopping Centers, etc.';
COMMENT ON COLUMN investments.exchange IS 'Stock exchange: B3, NYSE, NASDAQ, etc.';
COMMENT ON COLUMN investments.currency IS 'Currency code: BRL, USD, EUR, etc.';

-- Insert some sample data for testing
INSERT INTO investments (ticker, name, investment_type, investment_subtype, sector, industry, current_price, user_id) VALUES
('PETR4', 'Petrobras PN', 'STOCK', 'PREFERRED', 'Energy', 'Oil & Gas', 35.50, 1),
('VALE3', 'Vale ON', 'STOCK', 'ORDINARY', 'Materials', 'Mining', 65.20, 1),
('ITUB4', 'Itaú Unibanco PN', 'STOCK', 'PREFERRED', 'Financial', 'Banking', 28.90, 1),
('HGLG11', 'CSHG Logística', 'FII', 'TIJOLO', 'Real Estate', 'Logistics', 95.80, 1),
('XPML11', 'XP Malls', 'FII', 'TIJOLO', 'Real Estate', 'Shopping Centers', 102.30, 1),
('BTLG11', 'BTG Pactual Logística', 'FII', 'TIJOLO', 'Real Estate', 'Logistics', 98.50, 1);
