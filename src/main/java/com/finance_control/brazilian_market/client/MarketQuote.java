package com.finance_control.brazilian_market.client;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Generic market quote data structure.
 * This can be populated by any market data provider.
 */
@Data
@Builder
public class MarketQuote {
    private String symbol;
    private String shortName;
    private String longName;
    private String currency;
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal dayChange;
    private BigDecimal dayChangePercent;
    private Long volume;
    private BigDecimal marketCap;
    private BigDecimal dividendYield;
    private BigDecimal pe;
    private BigDecimal eps;
    private String exchange;
    private String timezone;
    private LocalDateTime lastUpdated;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private BigDecimal open;
    private BigDecimal fiftyDayAverage;
    private BigDecimal twoHundredDayAverage;
    private BigDecimal priceToBook;
    private BigDecimal priceToSales;
    private BigDecimal beta;
    private BigDecimal bookValue;
    private BigDecimal trailingEps;
    private BigDecimal forwardEps;
    private BigDecimal pegRatio;
    private BigDecimal lastDividendValue;
    private String lastDividendDate;
    private BigDecimal annualDividendRate;
    private BigDecimal annualDividendYield;
}
