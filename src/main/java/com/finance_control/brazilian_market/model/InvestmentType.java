package com.finance_control.brazilian_market.model;

/**
 * Investment types enum.
 * Extracted from Investment.java to reduce class fan-out complexity.
 */
public enum InvestmentType {
    STOCK("Stock"),
    FII("Real Estate Investment Fund"),
    BOND("Bond"),
    ETF("Exchange Traded Fund"),
    CRYPTO("Cryptocurrency"),
    COMMODITY("Commodity"),
    CURRENCY("Currency"),
    OTHER("Other");

    private final String description;

    InvestmentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

