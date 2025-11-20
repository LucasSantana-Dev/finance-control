package com.finance_control.brazilian_market.client.model;

import java.util.List;

/**
 * Indicators from US Market API chart response.
 */
public class Indicators {
    private List<Quote> quote;

    public List<Quote> getQuote() {
        return quote;
    }

    public void setQuote(List<Quote> quote) {
        this.quote = quote;
    }
}

