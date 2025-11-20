package com.finance_control.brazilian_market.client.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Market data structure for internal use.
 */
@Data
@Builder
public class MarketData {
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal dayChange;
    private BigDecimal dayChangePercent;
    private Long volume;
    private BigDecimal marketCap;
    private BigDecimal dividendYield;
    private LocalDateTime lastUpdated;
}

