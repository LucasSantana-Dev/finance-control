package com.finance_control.brazilian_market.client.model;

/**
 * Quote response from US Market API.
 * EI_EXPOSE_REP suppression needed: This DTO is used for JSON deserialization from external APIs.
 * Jackson requires direct field access for proper deserialization of API responses.
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
public class QuoteResponse {
    private String symbol;
    private String shortName;
    private String longName;
    private String currency;
    private Double regularMarketPrice;
    private Double previousClose;
    private Long regularMarketVolume;
    private Double marketCap;
    private Double dividendYield;
    private Double pe;
    private Double eps;
    private String exchange;
    private String timezone;
    private String exchangeTimezoneName;
    private Long regularMarketTime;
    private Double regularMarketDayHigh;
    private Double regularMarketDayLow;
    private Double regularMarketOpen;
    private Double twoHundredDayAverage;
    private Double fiftyDayAverage;
    private Double priceToBook;
    private Double priceToSales;
    private Double enterpriseToRevenue;
    private Double enterpriseToEbitda;
    private Double profitMargins;
    private Double grossMargins;
    private Double operatingMargins;
    private Double returnOnAssets;
    private Double returnOnEquity;
    private Double totalCash;
    private Double totalDebt;
    private Double totalRevenue;
    private Double revenueGrowth;
    private Double earningsGrowth;
    private Double targetMeanPrice;
    private Double targetHighPrice;
    private Double targetLowPrice;
    private Double recommendationMean;
    private String recommendationKey;
    private Long numberOfAnalystOpinions;
    private Double bookValue;
    private Double priceToBookValue;
    private Double netIncomeToCommon;
    private Double trailingEps;
    private Double forwardEps;
    private Double pegRatio;
    private Double lastDividendValue;
    private String lastDividendDate;
    private Double lastCapGain;
    private Double annualDividendRate;
    private Double annualDividendYield;
    private Double beta;
    private Double impliedSharesOutstanding;
    private Double floatShares;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public Long getRegularMarketVolume() {
        return regularMarketVolume;
    }

    public void setRegularMarketVolume(Long regularMarketVolume) {
        this.regularMarketVolume = regularMarketVolume;
    }

    public Double getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }

    public Double getDividendYield() {
        return dividendYield;
    }

    public void setDividendYield(Double dividendYield) {
        this.dividendYield = dividendYield;
    }

    public Double getPe() {
        return pe;
    }

    public void setPe(Double pe) {
        this.pe = pe;
    }

    public Double getEps() {
        return eps;
    }

    public void setEps(Double eps) {
        this.eps = eps;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
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

    public Long getRegularMarketTime() {
        return regularMarketTime;
    }

    public void setRegularMarketTime(Long regularMarketTime) {
        this.regularMarketTime = regularMarketTime;
    }

    public Double getRegularMarketDayHigh() {
        return regularMarketDayHigh;
    }

    public void setRegularMarketDayHigh(Double regularMarketDayHigh) {
        this.regularMarketDayHigh = regularMarketDayHigh;
    }

    public Double getRegularMarketDayLow() {
        return regularMarketDayLow;
    }

    public void setRegularMarketDayLow(Double regularMarketDayLow) {
        this.regularMarketDayLow = regularMarketDayLow;
    }

    public Double getRegularMarketOpen() {
        return regularMarketOpen;
    }

    public void setRegularMarketOpen(Double regularMarketOpen) {
        this.regularMarketOpen = regularMarketOpen;
    }

    public Double getTwoHundredDayAverage() {
        return twoHundredDayAverage;
    }

    public void setTwoHundredDayAverage(Double twoHundredDayAverage) {
        this.twoHundredDayAverage = twoHundredDayAverage;
    }

    public Double getFiftyDayAverage() {
        return fiftyDayAverage;
    }

    public void setFiftyDayAverage(Double fiftyDayAverage) {
        this.fiftyDayAverage = fiftyDayAverage;
    }

    public Double getPriceToBook() {
        return priceToBook;
    }

    public void setPriceToBook(Double priceToBook) {
        this.priceToBook = priceToBook;
    }

    public Double getPriceToSales() {
        return priceToSales;
    }

    public void setPriceToSales(Double priceToSales) {
        this.priceToSales = priceToSales;
    }

    public Double getEnterpriseToRevenue() {
        return enterpriseToRevenue;
    }

    public void setEnterpriseToRevenue(Double enterpriseToRevenue) {
        this.enterpriseToRevenue = enterpriseToRevenue;
    }

    public Double getEnterpriseToEbitda() {
        return enterpriseToEbitda;
    }

    public void setEnterpriseToEbitda(Double enterpriseToEbitda) {
        this.enterpriseToEbitda = enterpriseToEbitda;
    }

    public Double getProfitMargins() {
        return profitMargins;
    }

    public void setProfitMargins(Double profitMargins) {
        this.profitMargins = profitMargins;
    }

    public Double getGrossMargins() {
        return grossMargins;
    }

    public void setGrossMargins(Double grossMargins) {
        this.grossMargins = grossMargins;
    }

    public Double getOperatingMargins() {
        return operatingMargins;
    }

    public void setOperatingMargins(Double operatingMargins) {
        this.operatingMargins = operatingMargins;
    }

    public Double getReturnOnAssets() {
        return returnOnAssets;
    }

    public void setReturnOnAssets(Double returnOnAssets) {
        this.returnOnAssets = returnOnAssets;
    }

    public Double getReturnOnEquity() {
        return returnOnEquity;
    }

    public void setReturnOnEquity(Double returnOnEquity) {
        this.returnOnEquity = returnOnEquity;
    }

    public Double getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(Double totalCash) {
        this.totalCash = totalCash;
    }

    public Double getTotalDebt() {
        return totalDebt;
    }

    public void setTotalDebt(Double totalDebt) {
        this.totalDebt = totalDebt;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Double getRevenueGrowth() {
        return revenueGrowth;
    }

    public void setRevenueGrowth(Double revenueGrowth) {
        this.revenueGrowth = revenueGrowth;
    }

    public Double getEarningsGrowth() {
        return earningsGrowth;
    }

    public void setEarningsGrowth(Double earningsGrowth) {
        this.earningsGrowth = earningsGrowth;
    }

    public Double getTargetMeanPrice() {
        return targetMeanPrice;
    }

    public void setTargetMeanPrice(Double targetMeanPrice) {
        this.targetMeanPrice = targetMeanPrice;
    }

    public Double getTargetHighPrice() {
        return targetHighPrice;
    }

    public void setTargetHighPrice(Double targetHighPrice) {
        this.targetHighPrice = targetHighPrice;
    }

    public Double getTargetLowPrice() {
        return targetLowPrice;
    }

    public void setTargetLowPrice(Double targetLowPrice) {
        this.targetLowPrice = targetLowPrice;
    }

    public Double getRecommendationMean() {
        return recommendationMean;
    }

    public void setRecommendationMean(Double recommendationMean) {
        this.recommendationMean = recommendationMean;
    }

    public String getRecommendationKey() {
        return recommendationKey;
    }

    public void setRecommendationKey(String recommendationKey) {
        this.recommendationKey = recommendationKey;
    }

    public Long getNumberOfAnalystOpinions() {
        return numberOfAnalystOpinions;
    }

    public void setNumberOfAnalystOpinions(Long numberOfAnalystOpinions) {
        this.numberOfAnalystOpinions = numberOfAnalystOpinions;
    }

    public Double getBookValue() {
        return bookValue;
    }

    public void setBookValue(Double bookValue) {
        this.bookValue = bookValue;
    }

    public Double getPriceToBookValue() {
        return priceToBookValue;
    }

    public void setPriceToBookValue(Double priceToBookValue) {
        this.priceToBookValue = priceToBookValue;
    }

    public Double getNetIncomeToCommon() {
        return netIncomeToCommon;
    }

    public void setNetIncomeToCommon(Double netIncomeToCommon) {
        this.netIncomeToCommon = netIncomeToCommon;
    }

    public Double getTrailingEps() {
        return trailingEps;
    }

    public void setTrailingEps(Double trailingEps) {
        this.trailingEps = trailingEps;
    }

    public Double getForwardEps() {
        return forwardEps;
    }

    public void setForwardEps(Double forwardEps) {
        this.forwardEps = forwardEps;
    }

    public Double getPegRatio() {
        return pegRatio;
    }

    public void setPegRatio(Double pegRatio) {
        this.pegRatio = pegRatio;
    }

    public Double getLastDividendValue() {
        return lastDividendValue;
    }

    public void setLastDividendValue(Double lastDividendValue) {
        this.lastDividendValue = lastDividendValue;
    }

    public String getLastDividendDate() {
        return lastDividendDate;
    }

    public void setLastDividendDate(String lastDividendDate) {
        this.lastDividendDate = lastDividendDate;
    }

    public Double getLastCapGain() {
        return lastCapGain;
    }

    public void setLastCapGain(Double lastCapGain) {
        this.lastCapGain = lastCapGain;
    }

    public Double getAnnualDividendRate() {
        return annualDividendRate;
    }

    public void setAnnualDividendRate(Double annualDividendRate) {
        this.annualDividendRate = annualDividendRate;
    }

    public Double getAnnualDividendYield() {
        return annualDividendYield;
    }

    public void setAnnualDividendYield(Double annualDividendYield) {
        this.annualDividendYield = annualDividendYield;
    }

    public Double getBeta() {
        return beta;
    }

    public void setBeta(Double beta) {
        this.beta = beta;
    }

    public Double getImpliedSharesOutstanding() {
        return impliedSharesOutstanding;
    }

    public void setImpliedSharesOutstanding(Double impliedSharesOutstanding) {
        this.impliedSharesOutstanding = impliedSharesOutstanding;
    }

    public Double getFloatShares() {
        return floatShares;
    }

    public void setFloatShares(Double floatShares) {
        this.floatShares = floatShares;
    }
}

