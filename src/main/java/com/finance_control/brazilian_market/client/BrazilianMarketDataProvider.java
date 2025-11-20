package com.finance_control.brazilian_market.client;

import com.finance_control.brazilian_market.model.InvestmentType;
import com.finance_control.brazilian_market.util.MarketDataConversionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Brazilian market data provider for stocks and FIIs.
 * Provides free access to real-time data for Brazilian markets.
 * This implementation uses public API endpoints.
 */
@Component("brazilianMarketDataProvider")
@RequiredArgsConstructor
@Slf4j
public class BrazilianMarketDataProvider implements MarketDataProvider {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://brapi.dev/api";
    private static final String QUOTE_ENDPOINT = "/quote";
    private static final String QUOTES_ENDPOINT = "/quotes";

    @Override
    public Optional<MarketQuote> getQuote(String ticker) {
        try {
            log.debug("Fetching quote for ticker: {} from Brazilian market API", ticker);

            String url = UriComponentsBuilder.fromUriString(BASE_URL + QUOTE_ENDPOINT)
                    .queryParam("symbols", ticker)
                    .queryParam("range", "1d")
                    .queryParam("interval", "1d")
                    .queryParam("fundamental", "true")
                    .build()
                    .toUriString();

            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class);

            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                return Optional.of(convertToMarketQuote(response.getResults().get(0)));
            }

            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching quote for ticker: {} from Brazilian market API", ticker, e);
            return Optional.empty();
        }
    }

    @Override
    public List<MarketQuote> getQuotes(List<String> tickers) {
        try {
            log.debug("Fetching quotes for {} tickers from Brazilian market API", tickers.size());

            String symbols = String.join(",", tickers);
            String url = UriComponentsBuilder.fromUriString(BASE_URL + QUOTES_ENDPOINT)
                    .queryParam("symbols", symbols)
                    .queryParam("range", "1d")
                    .queryParam("interval", "1d")
                    .queryParam("fundamental", "true")
                    .build()
                    .toUriString();

            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class);

            if (response != null && response.getResults() != null) {
                return response.getResults().stream()
                        .map(this::convertToMarketQuote)
                        .collect(Collectors.toList());
            }

            return List.of();
        } catch (Exception e) {
            log.error("Error fetching quotes for tickers: {} from Brazilian market API", tickers, e);
            return List.of();
        }
    }

    @Override
    public Optional<HistoricalData> getHistoricalData(String ticker, String period, String interval) {
        // Brapi doesn't provide historical data in the same format
        // This would need to be implemented if historical data is required
        log.warn("Historical data not implemented for Brazilian market provider");
        return Optional.empty();
    }

    @Override
    public boolean supportsInvestmentType(InvestmentType investmentType) {
        return investmentType == InvestmentType.STOCK ||
               investmentType == InvestmentType.FII;
    }

    @Override
    public String getProviderName() {
        return "Brazilian Market API";
    }

    /**
     * Convert API quote response to generic MarketQuote
     */
    private MarketQuote convertToMarketQuote(QuoteResponse quote) {
        try {
            if (quote == null || quote.getRegularMarketPrice() == null) {
                return null;
            }

            MarketDataConversionUtils.PriceCalculation prices =
                    MarketDataConversionUtils.calculatePrices(
                            quote.getRegularMarketPrice(),
                            quote.getPreviousClose()
                    );

            return MarketQuote.builder()
                    .symbol(quote.getSymbol())
                    .shortName(quote.getShortName())
                    .longName(quote.getLongName())
                    .currency(quote.getCurrency())
                    .currentPrice(prices.getCurrentPrice())
                    .previousClose(prices.getPreviousClose())
                    .dayChange(prices.getDayChange())
                    .dayChangePercent(prices.getDayChangePercent())
                    .volume(quote.getRegularMarketVolume())
                    .marketCap(MarketDataConversionUtils.toBigDecimalSafe(quote.getMarketCap()))
                    .dividendYield(MarketDataConversionUtils.toBigDecimalSafe(quote.getDividendYield()))
                    .pe(MarketDataConversionUtils.toBigDecimalSafe(quote.getPe()))
                    .eps(MarketDataConversionUtils.toBigDecimalSafe(quote.getEps()))
                    .exchange(quote.getExchange())
                    .timezone(quote.getTimezone())
                    .lastUpdated(LocalDateTime.now())
                    .dayHigh(MarketDataConversionUtils.toBigDecimalSafe(quote.getRegularMarketDayHigh()))
                    .dayLow(MarketDataConversionUtils.toBigDecimalSafe(quote.getRegularMarketDayLow()))
                    .open(MarketDataConversionUtils.toBigDecimalSafe(quote.getRegularMarketOpen()))
                    .fiftyDayAverage(MarketDataConversionUtils.toBigDecimalSafe(quote.getFiftyDayAverage()))
                    .twoHundredDayAverage(MarketDataConversionUtils.toBigDecimalSafe(quote.getTwoHundredDayAverage()))
                    .priceToBook(MarketDataConversionUtils.toBigDecimalSafe(quote.getPriceToBook()))
                    .priceToSales(MarketDataConversionUtils.toBigDecimalSafe(quote.getPriceToSales()))
                    .beta(MarketDataConversionUtils.toBigDecimalSafe(quote.getBeta()))
                    .bookValue(MarketDataConversionUtils.toBigDecimalSafe(quote.getBookValue()))
                    .trailingEps(MarketDataConversionUtils.toBigDecimalSafe(quote.getTrailingEps()))
                    .forwardEps(MarketDataConversionUtils.toBigDecimalSafe(quote.getForwardEps()))
                    .pegRatio(MarketDataConversionUtils.toBigDecimalSafe(quote.getPegRatio()))
                    .lastDividendValue(MarketDataConversionUtils.toBigDecimalSafe(quote.getLastDividendValue()))
                    .lastDividendDate(quote.getLastDividendDate())
                    .annualDividendRate(MarketDataConversionUtils.toBigDecimalSafe(quote.getAnnualDividendRate()))
                    .annualDividendYield(MarketDataConversionUtils.toBigDecimalSafe(quote.getAnnualDividendYield()))
                    .build();
        } catch (Exception e) {
            log.error("Error converting API quote to MarketQuote for symbol: {}", quote.getSymbol(), e);
            return null;
        }
    }

    /**
     * Convert API quote response to Investment market data (legacy method)
     */
    public Optional<MarketData> convertToMarketData(QuoteResponse quote, InvestmentType investmentType) {
        try {
            if (quote == null || quote.getRegularMarketPrice() == null) {
                return Optional.empty();
            }

            MarketDataConversionUtils.PriceCalculation prices =
                    MarketDataConversionUtils.calculatePrices(
                            quote.getRegularMarketPrice(),
                            quote.getPreviousClose()
                    );

            return Optional.of(MarketData.builder()
                    .currentPrice(prices.getCurrentPrice())
                    .previousClose(prices.getPreviousClose())
                    .dayChange(prices.getDayChange())
                    .dayChangePercent(prices.getDayChangePercent())
                    .volume(quote.getRegularMarketVolume())
                    .marketCap(MarketDataConversionUtils.toBigDecimalSafe(quote.getMarketCap()))
                    .dividendYield(MarketDataConversionUtils.toBigDecimalSafe(quote.getDividendYield()))
                    .lastUpdated(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            String symbol = quote != null ? quote.getSymbol() : "unknown";
            log.error("Error converting API quote to market data for symbol: {}", symbol, e);
            return Optional.empty();
        }
    }

    /**
     * Response wrapper for API
     */
    public static class ApiResponse {
        private List<QuoteResponse> results;
        private String requestedAt;
        private String took;

        // Getters and setters
        public List<QuoteResponse> getResults() {
            return results != null ? java.util.Collections.unmodifiableList(results) : null;
        }
        public void setResults(List<QuoteResponse> results) { this.results = results; }
        public String getRequestedAt() { return requestedAt; }
        public void setRequestedAt(String requestedAt) { this.requestedAt = requestedAt; }
        public String getTook() { return took; }
        public void setTook(String took) { this.took = took; }
    }

    /**
     * Quote response from API
     */
    public static class QuoteResponse {
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

        // Getters and setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getShortName() { return shortName; }
        public void setShortName(String shortName) { this.shortName = shortName; }
        public String getLongName() { return longName; }
        public void setLongName(String longName) { this.longName = longName; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public Double getRegularMarketPrice() { return regularMarketPrice; }
        public void setRegularMarketPrice(Double regularMarketPrice) { this.regularMarketPrice = regularMarketPrice; }
        public Double getPreviousClose() { return previousClose; }
        public void setPreviousClose(Double previousClose) { this.previousClose = previousClose; }
        public Long getRegularMarketVolume() { return regularMarketVolume; }
        public void setRegularMarketVolume(Long regularMarketVolume) { this.regularMarketVolume = regularMarketVolume; }
        public Double getMarketCap() { return marketCap; }
        public void setMarketCap(Double marketCap) { this.marketCap = marketCap; }
        public Double getDividendYield() { return dividendYield; }
        public void setDividendYield(Double dividendYield) { this.dividendYield = dividendYield; }
        public Double getPe() { return pe; }
        public void setPe(Double pe) { this.pe = pe; }
        public Double getEps() { return eps; }
        public void setEps(Double eps) { this.eps = eps; }
        public String getExchange() { return exchange; }
        public void setExchange(String exchange) { this.exchange = exchange; }
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        public String getExchangeTimezoneName() { return exchangeTimezoneName; }
        public void setExchangeTimezoneName(String exchangeTimezoneName) { this.exchangeTimezoneName = exchangeTimezoneName; }
        public Long getRegularMarketTime() { return regularMarketTime; }
        public void setRegularMarketTime(Long regularMarketTime) { this.regularMarketTime = regularMarketTime; }
        public Double getRegularMarketDayHigh() { return regularMarketDayHigh; }
        public void setRegularMarketDayHigh(Double regularMarketDayHigh) { this.regularMarketDayHigh = regularMarketDayHigh; }
        public Double getRegularMarketDayLow() { return regularMarketDayLow; }
        public void setRegularMarketDayLow(Double regularMarketDayLow) { this.regularMarketDayLow = regularMarketDayLow; }
        public Double getRegularMarketOpen() { return regularMarketOpen; }
        public void setRegularMarketOpen(Double regularMarketOpen) { this.regularMarketOpen = regularMarketOpen; }
        public Double getTwoHundredDayAverage() { return twoHundredDayAverage; }
        public void setTwoHundredDayAverage(Double twoHundredDayAverage) { this.twoHundredDayAverage = twoHundredDayAverage; }
        public Double getFiftyDayAverage() { return fiftyDayAverage; }
        public void setFiftyDayAverage(Double fiftyDayAverage) { this.fiftyDayAverage = fiftyDayAverage; }
        public Double getPriceToBook() { return priceToBook; }
        public void setPriceToBook(Double priceToBook) { this.priceToBook = priceToBook; }
        public Double getPriceToSales() { return priceToSales; }
        public void setPriceToSales(Double priceToSales) { this.priceToSales = priceToSales; }
        public Double getEnterpriseToRevenue() { return enterpriseToRevenue; }
        public void setEnterpriseToRevenue(Double enterpriseToRevenue) { this.enterpriseToRevenue = enterpriseToRevenue; }
        public Double getEnterpriseToEbitda() { return enterpriseToEbitda; }
        public void setEnterpriseToEbitda(Double enterpriseToEbitda) { this.enterpriseToEbitda = enterpriseToEbitda; }
        public Double getProfitMargins() { return profitMargins; }
        public void setProfitMargins(Double profitMargins) { this.profitMargins = profitMargins; }
        public Double getGrossMargins() { return grossMargins; }
        public void setGrossMargins(Double grossMargins) { this.grossMargins = grossMargins; }
        public Double getOperatingMargins() { return operatingMargins; }
        public void setOperatingMargins(Double operatingMargins) { this.operatingMargins = operatingMargins; }
        public Double getReturnOnAssets() { return returnOnAssets; }
        public void setReturnOnAssets(Double returnOnAssets) { this.returnOnAssets = returnOnAssets; }
        public Double getReturnOnEquity() { return returnOnEquity; }
        public void setReturnOnEquity(Double returnOnEquity) { this.returnOnEquity = returnOnEquity; }
        public Double getTotalCash() { return totalCash; }
        public void setTotalCash(Double totalCash) { this.totalCash = totalCash; }
        public Double getTotalDebt() { return totalDebt; }
        public void setTotalDebt(Double totalDebt) { this.totalDebt = totalDebt; }
        public Double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
        public Double getRevenueGrowth() { return revenueGrowth; }
        public void setRevenueGrowth(Double revenueGrowth) { this.revenueGrowth = revenueGrowth; }
        public Double getEarningsGrowth() { return earningsGrowth; }
        public void setEarningsGrowth(Double earningsGrowth) { this.earningsGrowth = earningsGrowth; }
        public Double getTargetMeanPrice() { return targetMeanPrice; }
        public void setTargetMeanPrice(Double targetMeanPrice) { this.targetMeanPrice = targetMeanPrice; }
        public Double getTargetHighPrice() { return targetHighPrice; }
        public void setTargetHighPrice(Double targetHighPrice) { this.targetHighPrice = targetHighPrice; }
        public Double getTargetLowPrice() { return targetLowPrice; }
        public void setTargetLowPrice(Double targetLowPrice) { this.targetLowPrice = targetLowPrice; }
        public Double getRecommendationMean() { return recommendationMean; }
        public void setRecommendationMean(Double recommendationMean) { this.recommendationMean = recommendationMean; }
        public String getRecommendationKey() { return recommendationKey; }
        public void setRecommendationKey(String recommendationKey) { this.recommendationKey = recommendationKey; }
        public Long getNumberOfAnalystOpinions() { return numberOfAnalystOpinions; }
        public void setNumberOfAnalystOpinions(Long numberOfAnalystOpinions) { this.numberOfAnalystOpinions = numberOfAnalystOpinions; }
        public Double getBookValue() { return bookValue; }
        public void setBookValue(Double bookValue) { this.bookValue = bookValue; }
        public Double getPriceToBookValue() { return priceToBookValue; }
        public void setPriceToBookValue(Double priceToBookValue) { this.priceToBookValue = priceToBookValue; }
        public Double getNetIncomeToCommon() { return netIncomeToCommon; }
        public void setNetIncomeToCommon(Double netIncomeToCommon) { this.netIncomeToCommon = netIncomeToCommon; }
        public Double getTrailingEps() { return trailingEps; }
        public void setTrailingEps(Double trailingEps) { this.trailingEps = trailingEps; }
        public Double getForwardEps() { return forwardEps; }
        public void setForwardEps(Double forwardEps) { this.forwardEps = forwardEps; }
        public Double getPegRatio() { return pegRatio; }
        public void setPegRatio(Double pegRatio) { this.pegRatio = pegRatio; }
        public Double getLastDividendValue() { return lastDividendValue; }
        public void setLastDividendValue(Double lastDividendValue) { this.lastDividendValue = lastDividendValue; }
        public String getLastDividendDate() { return lastDividendDate; }
        public void setLastDividendDate(String lastDividendDate) { this.lastDividendDate = lastDividendDate; }
        public Double getLastCapGain() { return lastCapGain; }
        public void setLastCapGain(Double lastCapGain) { this.lastCapGain = lastCapGain; }
        public Double getAnnualDividendRate() { return annualDividendRate; }
        public void setAnnualDividendRate(Double annualDividendRate) { this.annualDividendRate = annualDividendRate; }
        public Double getAnnualDividendYield() { return annualDividendYield; }
        public void setAnnualDividendYield(Double annualDividendYield) { this.annualDividendYield = annualDividendYield; }
        public Double getBeta() { return beta; }
        public void setBeta(Double beta) { this.beta = beta; }
        public Double getImpliedSharesOutstanding() { return impliedSharesOutstanding; }
        public void setImpliedSharesOutstanding(Double impliedSharesOutstanding) { this.impliedSharesOutstanding = impliedSharesOutstanding; }
        public Double getFloatShares() { return floatShares; }
        public void setFloatShares(Double floatShares) { this.floatShares = floatShares; }
    }

    /**
     * Market data structure for internal use
     */
    @lombok.Data
    @lombok.Builder
    public static class MarketData {
        private BigDecimal currentPrice;
        private BigDecimal previousClose;
        private BigDecimal dayChange;
        private BigDecimal dayChangePercent;
        private Long volume;
        private BigDecimal marketCap;
        private BigDecimal dividendYield;
        private LocalDateTime lastUpdated;
    }
}
