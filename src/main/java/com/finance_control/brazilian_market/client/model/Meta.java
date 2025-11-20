package com.finance_control.brazilian_market.client.model;

import java.util.List;

/**
 * Metadata from US Market API chart response.
 */
public class Meta {
    private String currency;
    private String symbol;
    private String exchangeName;
    private String instrumentType;
    private Long firstTradeDate;
    private Long regularMarketTime;
    private Long gmtoffset;
    private String timezone;
    private String exchangeTimezoneName;
    private Double regularMarketPrice;
    private Double previousClose;
    private Long scale;
    private Long priceHint;
    private Double currentTradingPeriod;
    private String dataGranularity;
    private String range;
    private List<String> validRanges;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public Long getFirstTradeDate() {
        return firstTradeDate;
    }

    public void setFirstTradeDate(Long firstTradeDate) {
        this.firstTradeDate = firstTradeDate;
    }

    public Long getRegularMarketTime() {
        return regularMarketTime;
    }

    public void setRegularMarketTime(Long regularMarketTime) {
        this.regularMarketTime = regularMarketTime;
    }

    public Long getGmtoffset() {
        return gmtoffset;
    }

    public void setGmtoffset(Long gmtoffset) {
        this.gmtoffset = gmtoffset;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getExchangeTimezoneName() {
        return exchangeTimezoneName;
    }

    public void setExchangeTimezoneName(String exchangeTimezoneName) {
        this.exchangeTimezoneName = exchangeTimezoneName;
    }

    public Double getRegularMarketPrice() {
        return regularMarketPrice;
    }

    public void setRegularMarketPrice(Double regularMarketPrice) {
        this.regularMarketPrice = regularMarketPrice;
    }

    public Double getPreviousClose() {
        return previousClose;
    }

    public void setPreviousClose(Double previousClose) {
        this.previousClose = previousClose;
    }

    public Long getScale() {
        return scale;
    }

    public void setScale(Long scale) {
        this.scale = scale;
    }

    public Long getPriceHint() {
        return priceHint;
    }

    public void setPriceHint(Long priceHint) {
        this.priceHint = priceHint;
    }

    public Double getCurrentTradingPeriod() {
        return currentTradingPeriod;
    }

    public void setCurrentTradingPeriod(Double currentTradingPeriod) {
        this.currentTradingPeriod = currentTradingPeriod;
    }

    public String getDataGranularity() {
        return dataGranularity;
    }

    public void setDataGranularity(String dataGranularity) {
        this.dataGranularity = dataGranularity;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public List<String> getValidRanges() {
        return validRanges;
    }

    public void setValidRanges(List<String> validRanges) {
        this.validRanges = validRanges;
    }
}

