-- Migration for Brazilian Market Data Tables
-- Creates tables for Brazilian stocks, FIIs, bonds, and market indicators

-- Brazilian Stocks Table
CREATE TABLE brazilian_stocks (
    id BIGSERIAL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    description TEXT,
    stock_type VARCHAR(50) NOT NULL,
    segment VARCHAR(50) NOT NULL,
    current_price DECIMAL(19,2),
    previous_close DECIMAL(19,2),
    day_change DECIMAL(19,2),
    day_change_percent DECIMAL(8,4),
    volume BIGINT,
    market_cap DECIMAL(19,2),
    last_updated TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_brazilian_stocks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_brazilian_stocks_ticker_user UNIQUE (ticker, user_id)
);

-- FIIs (Real Estate Investment Funds) Table
CREATE TABLE fii_funds (
    id BIGSERIAL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL,
    fund_name VARCHAR(255) NOT NULL,
    description TEXT,
    fii_type VARCHAR(50) NOT NULL,
    segment VARCHAR(50) NOT NULL,
    current_price DECIMAL(19,2),
    previous_close DECIMAL(19,2),
    day_change DECIMAL(19,2),
    day_change_percent DECIMAL(8,4),
    volume BIGINT,
    market_cap DECIMAL(19,2),
    dividend_yield DECIMAL(8,4),
    last_dividend DECIMAL(19,2),
    last_dividend_date DATE,
    net_worth DECIMAL(19,2),
    p_vp_ratio DECIMAL(8,4),
    last_updated TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fii_funds_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_fii_funds_ticker_user UNIQUE (ticker, user_id)
);

-- Brazilian Bonds Table
CREATE TABLE brazilian_bonds (
    id BIGSERIAL PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL,
    issuer_name VARCHAR(255) NOT NULL,
    description TEXT,
    bond_type VARCHAR(50) NOT NULL,
    index_type VARCHAR(50) NOT NULL,
    face_value DECIMAL(19,2),
    current_price DECIMAL(19,2),
    interest_rate DECIMAL(8,4),
    yield_to_maturity DECIMAL(8,4),
    maturity_date DATE,
    issue_date DATE,
    last_coupon_date DATE,
    next_coupon_date DATE,
    coupon_frequency INTEGER,
    credit_rating VARCHAR(10),
    liquidity VARCHAR(50),
    minimum_investment DECIMAL(19,2),
    is_tax_free BOOLEAN DEFAULT false,
    last_updated TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_brazilian_bonds_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_brazilian_bonds_ticker_user UNIQUE (ticker, user_id)
);

-- Market Indicators Table
CREATE TABLE market_indicators (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    indicator_type VARCHAR(50) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    current_value DECIMAL(19,6),
    previous_value DECIMAL(19,6),
    change_value DECIMAL(19,6),
    change_percent DECIMAL(8,4),
    reference_date DATE,
    last_updated TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_brazilian_stocks_user_id ON brazilian_stocks(user_id);
CREATE INDEX idx_brazilian_stocks_ticker ON brazilian_stocks(ticker);
CREATE INDEX idx_brazilian_stocks_stock_type ON brazilian_stocks(stock_type);
CREATE INDEX idx_brazilian_stocks_segment ON brazilian_stocks(segment);
CREATE INDEX idx_brazilian_stocks_is_active ON brazilian_stocks(is_active);
CREATE INDEX idx_brazilian_stocks_last_updated ON brazilian_stocks(last_updated);

CREATE INDEX idx_fii_funds_user_id ON fii_funds(user_id);
CREATE INDEX idx_fii_funds_ticker ON fii_funds(ticker);
CREATE INDEX idx_fii_funds_fii_type ON fii_funds(fii_type);
CREATE INDEX idx_fii_funds_segment ON fii_funds(segment);
CREATE INDEX idx_fii_funds_is_active ON fii_funds(is_active);
CREATE INDEX idx_fii_funds_last_updated ON fii_funds(last_updated);
CREATE INDEX idx_fii_funds_dividend_yield ON fii_funds(dividend_yield);
CREATE INDEX idx_fii_funds_p_vp_ratio ON fii_funds(p_vp_ratio);

CREATE INDEX idx_brazilian_bonds_user_id ON brazilian_bonds(user_id);
CREATE INDEX idx_brazilian_bonds_ticker ON brazilian_bonds(ticker);
CREATE INDEX idx_brazilian_bonds_bond_type ON brazilian_bonds(bond_type);
CREATE INDEX idx_brazilian_bonds_index_type ON brazilian_bonds(index_type);
CREATE INDEX idx_brazilian_bonds_maturity_date ON brazilian_bonds(maturity_date);
CREATE INDEX idx_brazilian_bonds_is_active ON brazilian_bonds(is_active);
CREATE INDEX idx_brazilian_bonds_last_updated ON brazilian_bonds(last_updated);

CREATE INDEX idx_market_indicators_code ON market_indicators(code);
CREATE INDEX idx_market_indicators_indicator_type ON market_indicators(indicator_type);
CREATE INDEX idx_market_indicators_frequency ON market_indicators(frequency);
CREATE INDEX idx_market_indicators_is_active ON market_indicators(is_active);
CREATE INDEX idx_market_indicators_last_updated ON market_indicators(last_updated);
CREATE INDEX idx_market_indicators_reference_date ON market_indicators(reference_date);

-- Insert initial market indicators
INSERT INTO market_indicators (code, name, description, indicator_type, frequency, is_active) VALUES
('SELIC', 'Taxa Selic', 'Taxa básica de juros da economia brasileira', 'INTEREST_RATE', 'DAILY', true),
('CDI', 'CDI', 'Certificado de Depósito Interbancário', 'INTEREST_RATE', 'DAILY', true),
('IPCA', 'IPCA', 'Índice Nacional de Preços ao Consumidor Amplo', 'INFLATION', 'MONTHLY', true),
('IGP-M', 'IGP-M', 'Índice Geral de Preços do Mercado', 'INFLATION', 'MONTHLY', true),
('USD_BRL', 'USD/BRL', 'Taxa de câmbio Dólar Americano / Real Brasileiro', 'EXCHANGE_RATE', 'DAILY', true);

-- Add comments for documentation
COMMENT ON TABLE brazilian_stocks IS 'Brazilian stocks traded on B3 (Brasil Bolsa Balcão)';
COMMENT ON TABLE fii_funds IS 'Brazilian Real Estate Investment Funds (FIIs)';
COMMENT ON TABLE brazilian_bonds IS 'Brazilian fixed income securities (CDB, RDB, LCI, LCA, etc.)';
COMMENT ON TABLE market_indicators IS 'Brazilian market indicators and economic data from BCB';

COMMENT ON COLUMN brazilian_stocks.stock_type IS 'Type of stock: ORDINARY, PREFERRED, UNIT, FII, ETF, OTHER';
COMMENT ON COLUMN brazilian_stocks.segment IS 'Market segment: NOVO_MERCADO, LEVEL_2, LEVEL_1, TRADITIONAL, BOVESPA_MAIS, etc.';
COMMENT ON COLUMN fii_funds.fii_type IS 'Type of FII: TIJOLO, PAPEL, HIBRIDO, FUNDO_DE_FUNDOS, OTHER';
COMMENT ON COLUMN fii_funds.segment IS 'FII segment: SHOPPING, OFFICES, LOGISTICS, RESIDENTIAL, HEALTHCARE, etc.';
COMMENT ON COLUMN brazilian_bonds.bond_type IS 'Type of bond: CDB, RDB, LCI, LCA, LF, DEBENTURE, TESOURO_DIRETO, OTHER';
COMMENT ON COLUMN brazilian_bonds.index_type IS 'Index type: CDI, IPCA, SELIC, PREFIXADO, IGP_M, OTHER';
COMMENT ON COLUMN market_indicators.indicator_type IS 'Type of indicator: INTEREST_RATE, INFLATION, EXCHANGE_RATE, STOCK_INDEX, ECONOMIC, OTHER';
COMMENT ON COLUMN market_indicators.frequency IS 'Data frequency: DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUALLY, IRREGULAR';
