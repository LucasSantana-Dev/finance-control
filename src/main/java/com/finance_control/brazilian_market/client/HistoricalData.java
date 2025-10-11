package com.finance_control.brazilian_market.client;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Generic historical data structure.
 * This can be populated by any market data provider.
 */
@Data
@Builder
public class HistoricalData {
    private String symbol;
    private String currency;
    private String exchange;
    private String timezone;
    private List<HistoricalPoint> data;
    private LocalDateTime lastUpdated;
}

/**
 * Individual historical data point
 */
@Data
@Builder
class HistoricalPoint {
    private LocalDateTime timestamp;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;
}
