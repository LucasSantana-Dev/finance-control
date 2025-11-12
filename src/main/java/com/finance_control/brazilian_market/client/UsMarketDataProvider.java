package com.finance_control.brazilian_market.client;

import com.finance_control.brazilian_market.model.Investment;
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
 * US market data provider for stocks and ETFs.
 * Provides free access to real-time and historical data for US markets.
 * This implementation uses public API endpoints.
 */
@Component("usMarketDataProvider")
@RequiredArgsConstructor
@Slf4j
public class UsMarketDataProvider implements MarketDataProvider {

    private final RestTemplate restTemplate;
    private static final String CHART_BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart";
    private static final String QUOTE_BASE_URL = "https://query1.finance.yahoo.com/v7/finance/quote";

    @Override
    public Optional<MarketQuote> getQuote(String ticker) {
        try {
            log.debug("Fetching quote for ticker: {} from US market API", ticker);

            String url = UriComponentsBuilder.fromUriString(QUOTE_BASE_URL)
                    .queryParam("symbols", ticker)
                    .queryParam("fields", "symbol,shortName,longName,regularMarketPrice,previousClose," +
                            "regularMarketVolume,marketCap,dividendYield,pe,eps,exchange,timezone,regularMarketTime")
                    .build()
                    .toUriString();

            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class);

            if (response != null && response.getQuoteResponse() != null &&
                response.getQuoteResponse().getResult() != null &&
                !response.getQuoteResponse().getResult().isEmpty()) {
                return Optional.of(convertToMarketQuote(response.getQuoteResponse().getResult().get(0)));
            }

            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching quote for ticker: {} from US market API", ticker, e);
            return Optional.empty();
        }
    }

    @Override
    public List<MarketQuote> getQuotes(List<String> tickers) {
        try {
            log.debug("Fetching quotes for {} tickers from US market API", tickers.size());

            String symbols = String.join(",", tickers);
            String url = UriComponentsBuilder.fromUriString(QUOTE_BASE_URL)
                    .queryParam("symbols", symbols)
                    .queryParam("fields", "symbol,shortName,longName,regularMarketPrice,previousClose," +
                            "regularMarketVolume,marketCap,dividendYield,pe,eps,exchange,timezone,regularMarketTime")
                    .build()
                    .toUriString();

            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class);

            if (response != null && response.getQuoteResponse() != null &&
                response.getQuoteResponse().getResult() != null) {
                return response.getQuoteResponse().getResult().stream()
                        .map(this::convertToMarketQuote)
                        .collect(Collectors.toList());
            }

            return List.of();
        } catch (Exception e) {
            log.error("Error fetching quotes for tickers: {} from US market API", tickers, e);
            return List.of();
        }
    }

    @Override
    public Optional<HistoricalData> getHistoricalData(String ticker, String period, String interval) {
        try {
            log.debug("Fetching historical data for ticker: {} from US market API", ticker);

            String url = UriComponentsBuilder.fromUriString(CHART_BASE_URL + "/" + ticker)
                    .queryParam("period1", "0")
                    .queryParam("period2", String.valueOf(System.currentTimeMillis() / 1000))
                    .queryParam("interval", interval)
                    .queryParam("includePrePost", "true")
                    .queryParam("events", "div,split")
                    .build()
                    .toUriString();

            ChartResponse response = restTemplate.getForObject(url, ChartResponse.class);
            return response != null ? Optional.of(convertToHistoricalData(response)) : Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching historical data for ticker: {} from US market API", ticker, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean supportsInvestmentType(Investment.InvestmentType investmentType) {
        return investmentType == Investment.InvestmentType.STOCK ||
               investmentType == Investment.InvestmentType.ETF;
    }

    @Override
    public String getProviderName() {
        return "US Market API";
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
     * Convert API chart response to generic HistoricalData
     */
    private HistoricalData convertToHistoricalData(ChartResponse response) {
        try {
            if (response == null || response.getChart() == null ||
                response.getChart().getResult() == null || response.getChart().getResult().isEmpty()) {
                return null;
            }

            ChartResultItem result = response.getChart().getResult().get(0);
            Meta meta = result.getMeta();
            Indicators indicators = result.getIndicators();

            if (meta == null || indicators == null || indicators.getQuote() == null ||
                indicators.getQuote().isEmpty()) {
                return null;
            }

            // Use first quote series if needed in the future; currently we don't process detailed points
            List<HistoricalPoint> dataPoints = List.of(); // TODO: process timestamp and quote OHLCV arrays

            return HistoricalData.builder()
                    .symbol(meta.getSymbol())
                    .currency(meta.getCurrency())
                    .exchange(meta.getExchangeName())
                    .timezone(meta.getTimezone())
                    .data(dataPoints)
                    .lastUpdated(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error converting API chart to HistoricalData", e);
            return null;
        }
    }

    /**
     * Convert API quote response to Investment market data (legacy method)
     */
    public Optional<MarketData> convertToMarketData(QuoteResponse quote, Investment.InvestmentType investmentType) {
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
    // EI_EXPOSE_REP suppression needed: This DTO is used for JSON deserialization from external APIs
    // Jackson requires direct field access for proper deserialization of API responses
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
    public static class ApiResponse {
        private QuoteResponseWrapper quoteResponse;

        public QuoteResponseWrapper getQuoteResponse() { return quoteResponse; }
        public void setQuoteResponse(QuoteResponseWrapper quoteResponse) { this.quoteResponse = quoteResponse; }
    }

    // EI_EXPOSE_REP suppression needed: This DTO is used for JSON deserialization from external APIs
    // Jackson requires direct field access for proper deserialization of API responses
    public static class QuoteResponseWrapper {
        private List<QuoteResponse> result;
        private Object error;

        public List<QuoteResponse> getResult() {
            return result != null ? java.util.Collections.unmodifiableList(result) : null;
        }
        public void setResult(List<QuoteResponse> result) { this.result = result; }
        public Object getError() { return error; }
        public void setError(Object error) { this.error = error; }
    }

    /**
     * Quote response from API
     */
    // EI_EXPOSE_REP suppression needed: This DTO is used for JSON deserialization from external APIs
    // Jackson requires direct field access for proper deserialization of API responses
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
     * Chart response from API
     */
    // EI_EXPOSE_REP suppression needed: This DTO is used for JSON deserialization from external APIs
    // Jackson requires direct field access for proper deserialization of API responses
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
    public static class ChartResponse {
        private ChartResult chart;

        public ChartResult getChart() { return chart; }
        public void setChart(ChartResult chart) { this.chart = chart; }
    }

    // EI_EXPOSE_REP suppression needed: This DTO is used for JSON deserialization from external APIs
    // Jackson requires direct field access for proper deserialization of API responses
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
    public static class ChartResult {
        private List<ChartResultItem> result;
        private Object error;

        public List<ChartResultItem> getResult() { return result; }
        public void setResult(List<ChartResultItem> result) { this.result = result; }
        public Object getError() { return error; }
        public void setError(Object error) { this.error = error; }
    }

    // EI_EXPOSE_REP suppression needed: This DTO is used for JSON deserialization from external APIs
    // Jackson requires direct field access for proper deserialization of API responses
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
    public static class ChartResultItem {
        private Meta meta;
        private List<Long> timestamp;
        private Indicators indicators;

        public Meta getMeta() { return meta; }
        public void setMeta(Meta meta) { this.meta = meta; }
        public List<Long> getTimestamp() { return timestamp; }
        public void setTimestamp(List<Long> timestamp) { this.timestamp = timestamp; }
        public Indicators getIndicators() { return indicators; }
        public void setIndicators(Indicators indicators) { this.indicators = indicators; }
    }

    // EI_EXPOSE_REP suppression needed: This DTO is used for JSON deserialization from external APIs
    // Jackson requires direct field access for proper deserialization of API responses
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
    public static class Meta {
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

        // Getters and setters
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getExchangeName() { return exchangeName; }
        public void setExchangeName(String exchangeName) { this.exchangeName = exchangeName; }
        public String getInstrumentType() { return instrumentType; }
        public void setInstrumentType(String instrumentType) { this.instrumentType = instrumentType; }
        public Long getFirstTradeDate() { return firstTradeDate; }
        public void setFirstTradeDate(Long firstTradeDate) { this.firstTradeDate = firstTradeDate; }
        public Long getRegularMarketTime() { return regularMarketTime; }
        public void setRegularMarketTime(Long regularMarketTime) { this.regularMarketTime = regularMarketTime; }
        public Long getGmtoffset() { return gmtoffset; }
        public void setGmtoffset(Long gmtoffset) { this.gmtoffset = gmtoffset; }
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        public String getExchangeTimezoneName() { return exchangeTimezoneName; }
        public void setExchangeTimezoneName(String exchangeTimezoneName) { this.exchangeTimezoneName = exchangeTimezoneName; }
        public Double getRegularMarketPrice() { return regularMarketPrice; }
        public void setRegularMarketPrice(Double regularMarketPrice) { this.regularMarketPrice = regularMarketPrice; }
        public Double getPreviousClose() { return previousClose; }
        public void setPreviousClose(Double previousClose) { this.previousClose = previousClose; }
        public Long getScale() { return scale; }
        public void setScale(Long scale) { this.scale = scale; }
        public Long getPriceHint() { return priceHint; }
        public void setPriceHint(Long priceHint) { this.priceHint = priceHint; }
        public Double getCurrentTradingPeriod() { return currentTradingPeriod; }
        public void setCurrentTradingPeriod(Double currentTradingPeriod) { this.currentTradingPeriod = currentTradingPeriod; }
        public String getDataGranularity() { return dataGranularity; }
        public void setDataGranularity(String dataGranularity) { this.dataGranularity = dataGranularity; }
        public String getRange() { return range; }
        public void setRange(String range) { this.range = range; }
        public List<String> getValidRanges() { return validRanges; }
        public void setValidRanges(List<String> validRanges) { this.validRanges = validRanges; }
    }

    // EI_EXPOSE_REP suppression needed: This DTO is used for JSON deserialization from external APIs
    // Jackson requires direct field access for proper deserialization of API responses
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
    public static class Indicators {
        private List<Quote> quote;

        public List<Quote> getQuote() { return quote; }
        public void setQuote(List<Quote> quote) { this.quote = quote; }
    }

    // EI_EXPOSE_REP suppression needed: This DTO is used for JSON deserialization from external APIs
    // Jackson requires direct field access for proper deserialization of API responses
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
    public static class Quote {
        private List<Double> open;
        private List<Double> high;
        private List<Double> low;
        private List<Double> close;
        private List<Long> volume;

        public List<Double> getOpen() { return open; }
        public void setOpen(List<Double> open) { this.open = open; }
        public List<Double> getHigh() { return high; }
        public void setHigh(List<Double> high) { this.high = high; }
        public List<Double> getLow() { return low; }
        public void setLow(List<Double> low) { this.low = low; }
        public List<Double> getClose() { return close; }
        public void setClose(List<Double> close) { this.close = close; }
        public List<Long> getVolume() { return volume; }
        public void setVolume(List<Long> volume) { this.volume = volume; }
    }

    /**
     * Market data structure for internal use
     */
    @lombok.Data
    @lombok.Builder
    // EI_EXPOSE_REP suppression needed: This DTO is used for JSON deserialization from external APIs
    // Jackson requires direct field access for proper deserialization of API responses
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
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
