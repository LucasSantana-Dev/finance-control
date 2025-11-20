package com.finance_control.brazilian_market.client;

import com.finance_control.brazilian_market.client.model.ApiResponse;
import com.finance_control.brazilian_market.client.model.ChartResponse;
import com.finance_control.brazilian_market.client.model.MarketData;
import com.finance_control.brazilian_market.client.model.Meta;
import com.finance_control.brazilian_market.client.model.Quote;
import com.finance_control.brazilian_market.client.model.QuoteResponse;
import com.finance_control.brazilian_market.model.InvestmentType;
import com.finance_control.brazilian_market.util.MarketDataConversionUtils;
import com.finance_control.shared.monitoring.SentryService;
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
    private final SentryService sentryService;
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
            sentryService.captureException(e, java.util.Map.of(
                "operation", "get_quote",
                "ticker", ticker,
                "provider", "us_market"
            ));
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

            com.finance_control.brazilian_market.client.model.ChartResponse response =
                restTemplate.getForObject(url, com.finance_control.brazilian_market.client.model.ChartResponse.class);
            if (response == null) {
                return Optional.empty();
            }
            HistoricalData historicalData = convertToHistoricalData(response);
                return historicalData != null ? Optional.of(historicalData) : Optional.empty();
            } catch (Exception e) {
                log.error("Error fetching historical data for ticker: {} from US market API", ticker, e);
                sentryService.captureException(e, java.util.Map.of(
                    "operation", "get_historical_data",
                    "ticker", ticker,
                    "period", period != null ? period : "unknown",
                    "interval", interval != null ? interval : "unknown",
                    "provider", "us_market"
                ));
                return Optional.empty();
            }
    }

    @Override
    public boolean supportsInvestmentType(InvestmentType investmentType) {
        return investmentType == InvestmentType.STOCK ||
               investmentType == InvestmentType.ETF;
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
            ChartResponse.ChartResultItem result = extractChartResultItem(response);
            if (result == null) {
                return null;
            }

            Meta meta = validateAndExtractMeta(result);
            if (meta == null) {
                return null;
            }

            Quote quote = validateAndExtractQuote(result);
            if (quote == null) {
                return null;
            }

            List<Long> timestamps = validateAndExtractTimestamps(result);
            if (timestamps == null || timestamps.isEmpty()) {
                return null;
            }

            if (!hasValidPriceData(quote)) {
                return null;
            }

            List<HistoricalPoint> dataPoints = processHistoricalDataPoints(timestamps, quote);

            return buildHistoricalData(meta, dataPoints);
        } catch (Exception e) {
            log.error("Error converting API chart to HistoricalData", e);
            return null;
        }
    }

    private ChartResponse.ChartResultItem extractChartResultItem(ChartResponse response) {
        if (response == null || response.getChart() == null) {
            return null;
        }
        List<ChartResponse.ChartResultItem> results = response.getChart().getResult();
        if (results == null || results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    private Meta validateAndExtractMeta(ChartResponse.ChartResultItem result) {
        if (result == null) {
            return null;
        }
        return result.getMeta();
    }

    private Quote validateAndExtractQuote(ChartResponse.ChartResultItem result) {
        if (result == null) {
            return null;
        }
        com.finance_control.brazilian_market.client.model.Indicators indicators = result.getIndicators();
        if (indicators == null) {
            return null;
        }
        List<Quote> quotes = indicators.getQuote();
        if (quotes == null || quotes.isEmpty()) {
            return null;
        }
        return quotes.get(0);
    }

    private List<Long> validateAndExtractTimestamps(ChartResponse.ChartResultItem result) {
        if (result == null) {
            return null;
        }
        return result.getTimestamp();
    }

    private boolean hasValidPriceData(Quote quote) {
        if (quote == null) {
            return false;
        }
        List<Double> openPrices = quote.getOpen();
        return openPrices != null && !openPrices.isEmpty();
    }

    private HistoricalData buildHistoricalData(Meta meta, List<HistoricalPoint> dataPoints) {
        return HistoricalData.builder()
                .symbol(meta.getSymbol())
                .currency(meta.getCurrency())
                .exchange(meta.getExchangeName())
                .timezone(meta.getTimezone())
                .data(dataPoints)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Processes timestamp and quote arrays to create historical data points.
     *
     * @param timestamps List of Unix timestamps (seconds since epoch)
     * @param quote Quote object containing OHLCV arrays
     * @return List of HistoricalPoint objects
     */
    private List<HistoricalPoint> processHistoricalDataPoints(List<Long> timestamps, Quote quote) {
        if (timestamps == null || timestamps.isEmpty()) {
            return List.of();
        }

        int size = timestamps.size();
        List<HistoricalPoint> dataPoints = new java.util.ArrayList<>(size);

        List<Double> openPrices = quote.getOpen();
        List<Double> highPrices = quote.getHigh();
        List<Double> lowPrices = quote.getLow();
        List<Double> closePrices = quote.getClose();
        List<Long> volumes = quote.getVolume();

        for (int i = 0; i < size; i++) {
            Long timestamp = timestamps.get(i);
            if (timestamp == null) {
                continue; // Skip null timestamps
            }

            LocalDateTime dateTime = convertTimestampToDateTime(timestamp);
            OHLCVValues values = extractOHLCVValues(openPrices, highPrices, lowPrices, closePrices, volumes, i);

            if (hasAtLeastOnePrice(values)) {
                HistoricalPoint point = buildHistoricalPoint(dateTime, values);
                dataPoints.add(point);
            }
        }

        log.debug("Processed {} historical data points from {} timestamps", dataPoints.size(), size);
        return dataPoints;
    }

    private LocalDateTime convertTimestampToDateTime(Long timestamp) {
        return java.time.LocalDateTime.ofEpochSecond(timestamp, 0, java.time.ZoneOffset.UTC);
    }

    private OHLCVValues extractOHLCVValues(List<Double> openPrices, List<Double> highPrices,
                                           List<Double> lowPrices, List<Double> closePrices,
                                           List<Long> volumes, int index) {
        return new OHLCVValues(
                extractPriceValue(openPrices, index),
                extractPriceValue(highPrices, index),
                extractPriceValue(lowPrices, index),
                extractPriceValue(closePrices, index),
                extractVolumeValue(volumes, index)
        );
    }

    private BigDecimal extractPriceValue(List<Double> prices, int index) {
        if (prices == null || index >= prices.size() || prices.get(index) == null) {
            return null;
        }
        return BigDecimal.valueOf(prices.get(index));
    }

    private Long extractVolumeValue(List<Long> volumes, int index) {
        if (volumes == null || index >= volumes.size()) {
            return null;
        }
        return volumes.get(index);
    }

    private boolean hasAtLeastOnePrice(OHLCVValues values) {
        return values.open() != null || values.high() != null
            || values.low() != null || values.close() != null;
    }

    private HistoricalPoint buildHistoricalPoint(LocalDateTime dateTime, OHLCVValues values) {
        return HistoricalPoint.builder()
                .timestamp(dateTime)
                .open(values.open())
                .high(values.high())
                .low(values.low())
                .close(values.close())
                .volume(values.volume())
                .build();
    }

    private record OHLCVValues(BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Long volume) {}

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

            return Optional.of(com.finance_control.brazilian_market.client.model.MarketData.builder()
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

}
