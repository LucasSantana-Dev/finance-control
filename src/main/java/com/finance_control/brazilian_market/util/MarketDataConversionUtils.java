package com.finance_control.brazilian_market.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for common market data conversion operations.
 * Provides reusable methods for price calculations and conversions
 * used across different market data providers.
 */
public final class MarketDataConversionUtils {

    private MarketDataConversionUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Safely converts a Number to BigDecimal, returning null if the input is null.
     *
     * @param value the number to convert
     * @return BigDecimal representation or null
     */
    public static BigDecimal toBigDecimalSafe(Number value) {
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    /**
     * Extracts previous close price with fallback to current price.
     *
     * @param previousClose the previous close value
     * @param currentPrice the current price to use as fallback
     * @return previous close or current price if previous close is null
     */
    public static BigDecimal extractPreviousClose(Number previousClose, BigDecimal currentPrice) {
        return previousClose != null ?
                new BigDecimal(previousClose.toString()) : currentPrice;
    }

    /**
     * Calculates day change (current price - previous close).
     *
     * @param currentPrice the current market price
     * @param previousClose the previous close price
     * @return day change value
     */
    public static BigDecimal calculateDayChange(BigDecimal currentPrice, BigDecimal previousClose) {
        return currentPrice.subtract(previousClose);
    }

    /**
     * Calculates day change percentage with division by zero protection.
     * Formula: (dayChange / previousClose) * 100
     *
     * @param dayChange the absolute day change
     * @param previousClose the previous close price
     * @return percentage change, or ZERO if previous close is zero
     */
    public static BigDecimal calculateDayChangePercent(BigDecimal dayChange, BigDecimal previousClose) {
        if (previousClose.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return dayChange.divide(previousClose, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Container class for price calculation results.
     */
    public static class PriceCalculation {
        private final BigDecimal currentPrice;
        private final BigDecimal previousClose;
        private final BigDecimal dayChange;
        private final BigDecimal dayChangePercent;

        public PriceCalculation(BigDecimal currentPrice, BigDecimal previousClose,
                                BigDecimal dayChange, BigDecimal dayChangePercent) {
            this.currentPrice = currentPrice;
            this.previousClose = previousClose;
            this.dayChange = dayChange;
            this.dayChangePercent = dayChangePercent;
        }

        public BigDecimal getCurrentPrice() {
            return currentPrice;
        }

        public BigDecimal getPreviousClose() {
            return previousClose;
        }

        public BigDecimal getDayChange() {
            return dayChange;
        }

        public BigDecimal getDayChangePercent() {
            return dayChangePercent;
        }
    }

    /**
     * Performs all price-related calculations in one call.
     * Extracts current price, previous close, day change, and day change percentage.
     *
     * @param regularMarketPrice the current market price from API
     * @param previousCloseValue the previous close value from API
     * @return PriceCalculation object containing all calculated values
     */
    public static PriceCalculation calculatePrices(Number regularMarketPrice, Number previousCloseValue) {
        BigDecimal currentPrice = new BigDecimal(regularMarketPrice.toString());
        BigDecimal previousClose = extractPreviousClose(previousCloseValue, currentPrice);
        BigDecimal dayChange = calculateDayChange(currentPrice, previousClose);
        BigDecimal dayChangePercent = calculateDayChangePercent(dayChange, previousClose);

        return new PriceCalculation(currentPrice, previousClose, dayChange, dayChangePercent);
    }
}
