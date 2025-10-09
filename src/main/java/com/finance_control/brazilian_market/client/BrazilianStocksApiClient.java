package com.finance_control.brazilian_market.client;

import com.finance_control.brazilian_market.model.BrazilianStock;
import com.finance_control.brazilian_market.model.FII;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client for accessing Brazilian stocks and FIIs data.
 * Uses the open-source Brazilian Stocks API and other data sources.
 */
@Component
@Slf4j
public class BrazilianStocksApiClient {

    private final RestTemplate restTemplate;
    private final String stocksApiBaseUrl;
    private final String alphaVantageApiKey;

    public BrazilianStocksApiClient(RestTemplate restTemplate,
                                  @Value("${brazilian-market.stocks.base-url:https://api.brazilianstocks.com}") String stocksApiBaseUrl,
                                  @Value("${brazilian-market.alpha-vantage.api-key:}") String alphaVantageApiKey) {
        this.restTemplate = restTemplate;
        this.stocksApiBaseUrl = stocksApiBaseUrl;
        this.alphaVantageApiKey = alphaVantageApiKey;
    }

    /**
     * Fetches all available Brazilian stocks.
     */
    public List<BrazilianStock> getAllStocks() {
        try {
            log.debug("Fetching all Brazilian stocks");
            String url = UriComponentsBuilder.fromHttpUrl(stocksApiBaseUrl)
                    .pathSegment("stocks")
                    .build()
                    .toUriString();

            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return mapToBrazilianStocks(response.getBody());
            }

            log.warn("No stocks data found");
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching Brazilian stocks", e);
            return new ArrayList<>();
        }
    }

    /**
     * Fetches all available FIIs.
     */
    public List<FII> getAllFIIs() {
        try {
            log.debug("Fetching all FIIs");
            String url = UriComponentsBuilder.fromHttpUrl(stocksApiBaseUrl)
                    .pathSegment("fiis")
                    .build()
                    .toUriString();

            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return mapToFIIs(response.getBody());
            }

            log.warn("No FIIs data found");
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching FIIs", e);
            return new ArrayList<>();
        }
    }

    /**
     * Fetches real-time quote for a specific stock.
     */
    public BrazilianStock getStockQuote(String ticker) {
        try {
            log.debug("Fetching quote for stock: {}", ticker);

            // Try Brazilian Stocks API first
            BrazilianStock stock = getStockFromBrazilianApi(ticker);
            if (stock != null) {
                return stock;
            }

            // Fallback to Alpha Vantage
            return getStockFromAlphaVantage(ticker);
        } catch (Exception e) {
            log.error("Error fetching quote for stock: {}", ticker, e);
            return null;
        }
    }

    /**
     * Fetches real-time quote for a specific FII.
     */
    public FII getFIIQuote(String ticker) {
        try {
            log.debug("Fetching quote for FII: {}", ticker);
            String url = UriComponentsBuilder.fromHttpUrl(stocksApiBaseUrl)
                    .pathSegment("fiis", ticker)
                    .build()
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return mapToFII(response.getBody());
            }

            log.warn("No FII data found for ticker: {}", ticker);
            return null;
        } catch (Exception e) {
            log.error("Error fetching quote for FII: {}", ticker, e);
            return null;
        }
    }

    /**
     * Searches for stocks by company name or ticker.
     */
    public List<BrazilianStock> searchStocks(String query) {
        try {
            log.debug("Searching stocks with query: {}", query);
            String url = UriComponentsBuilder.fromHttpUrl(stocksApiBaseUrl)
                    .pathSegment("stocks", "search")
                    .queryParam("q", query)
                    .build()
                    .toUriString();

            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return mapToBrazilianStocks(response.getBody());
            }

            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error searching stocks with query: {}", query, e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets market summary data.
     */
    public Map<String, Object> getMarketSummary() {
        try {
            log.debug("Fetching market summary");
            String url = UriComponentsBuilder.fromHttpUrl(stocksApiBaseUrl)
                    .pathSegment("market", "summary")
                    .build()
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }

            return Map.of();
        } catch (Exception e) {
            log.error("Error fetching market summary", e);
            return Map.of();
        }
    }

    /**
     * Private method to get stock from Brazilian Stocks API.
     */
    private BrazilianStock getStockFromBrazilianApi(String ticker) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(stocksApiBaseUrl)
                    .pathSegment("stocks", ticker)
                    .build()
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return mapToBrazilianStock(response.getBody());
            }

            return null;
        } catch (Exception e) {
            log.debug("Error fetching stock {} from Brazilian API: {}", ticker, e.getMessage());
            return null;
        }
    }

    /**
     * Private method to get stock from Alpha Vantage API.
     */
    private BrazilianStock getStockFromAlphaVantage(String ticker) {
        if (alphaVantageApiKey == null || alphaVantageApiKey.trim().isEmpty()) {
            log.debug("Alpha Vantage API key not configured");
            return null;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://www.alphavantage.co/query")
                    .queryParam("function", "GLOBAL_QUOTE")
                    .queryParam("symbol", ticker + ".SA") // Brazilian stocks suffix
                    .queryParam("apikey", alphaVantageApiKey)
                    .build()
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return mapToBrazilianStockFromAlphaVantage(response.getBody(), ticker);
            }

            return null;
        } catch (Exception e) {
            log.debug("Error fetching stock {} from Alpha Vantage: {}", ticker, e.getMessage());
            return null;
        }
    }

    /**
     * Maps API response to BrazilianStock objects.
     */
    private List<BrazilianStock> mapToBrazilianStocks(List<Map<String, Object>> data) {
        List<BrazilianStock> stocks = new ArrayList<>();
        for (Map<String, Object> item : data) {
            BrazilianStock stock = mapToBrazilianStock(item);
            if (stock != null) {
                stocks.add(stock);
            }
        }
        return stocks;
    }

    /**
     * Maps API response to BrazilianStock object.
     */
    private BrazilianStock mapToBrazilianStock(Map<String, Object> data) {
        try {
            BrazilianStock stock = new BrazilianStock();
            stock.setTicker((String) data.get("ticker"));
            stock.setCompanyName((String) data.get("companyName"));
            stock.setDescription((String) data.get("description"));

            // Map stock type
            String stockTypeStr = (String) data.get("stockType");
            if (stockTypeStr != null) {
                stock.setStockType(BrazilianStock.StockType.valueOf(stockTypeStr.toUpperCase()));
            }

            // Map segment
            String segmentStr = (String) data.get("segment");
            if (segmentStr != null) {
                stock.setSegment(BrazilianStock.MarketSegment.valueOf(segmentStr.toUpperCase()));
            }

            // Map price data
            if (data.get("currentPrice") != null) {
                stock.setCurrentPrice(new BigDecimal(data.get("currentPrice").toString()));
            }
            if (data.get("previousClose") != null) {
                stock.setPreviousClose(new BigDecimal(data.get("previousClose").toString()));
            }
            if (data.get("volume") != null) {
                stock.setVolume(Long.valueOf(data.get("volume").toString()));
            }
            if (data.get("marketCap") != null) {
                stock.setMarketCap(new BigDecimal(data.get("marketCap").toString()));
            }

            stock.setLastUpdated(LocalDateTime.now());
            stock.setIsActive(true);

            return stock;
        } catch (Exception e) {
            log.error("Error mapping stock data", e);
            return null;
        }
    }

    /**
     * Maps Alpha Vantage response to BrazilianStock object.
     */
    private BrazilianStock mapToBrazilianStockFromAlphaVantage(Map<String, Object> data, String ticker) {
        try {
            Map<String, Object> quote = (Map<String, Object>) data.get("Global Quote");
            if (quote == null) {
                return null;
            }

            BrazilianStock stock = new BrazilianStock();
            stock.setTicker(ticker);
            stock.setCompanyName(ticker); // Alpha Vantage doesn't provide company name

            // Map price data
            if (quote.get("05. price") != null) {
                stock.setCurrentPrice(new BigDecimal(quote.get("05. price").toString()));
            }
            if (quote.get("08. previous close") != null) {
                stock.setPreviousClose(new BigDecimal(quote.get("08. previous close").toString()));
            }
            if (quote.get("06. volume") != null) {
                stock.setVolume(Long.valueOf(quote.get("06. volume").toString()));
            }

            stock.setLastUpdated(LocalDateTime.now());
            stock.setIsActive(true);

            return stock;
        } catch (Exception e) {
            log.error("Error mapping Alpha Vantage stock data", e);
            return null;
        }
    }

    /**
     * Maps API response to FII objects.
     */
    private List<FII> mapToFIIs(List<Map<String, Object>> data) {
        List<FII> fiis = new ArrayList<>();
        for (Map<String, Object> item : data) {
            FII fii = mapToFII(item);
            if (fii != null) {
                fiis.add(fii);
            }
        }
        return fiis;
    }

    /**
     * Maps API response to FII object.
     */
    private FII mapToFII(Map<String, Object> data) {
        try {
            FII fii = new FII();
            fii.setTicker((String) data.get("ticker"));
            fii.setFundName((String) data.get("fundName"));
            fii.setDescription((String) data.get("description"));

            // Map FII type
            String fiiTypeStr = (String) data.get("fiiType");
            if (fiiTypeStr != null) {
                fii.setFiiType(FII.FIIType.valueOf(fiiTypeStr.toUpperCase()));
            }

            // Map segment
            String segmentStr = (String) data.get("segment");
            if (segmentStr != null) {
                fii.setSegment(FII.FIISegment.valueOf(segmentStr.toUpperCase()));
            }

            // Map price data
            if (data.get("currentPrice") != null) {
                fii.setCurrentPrice(new BigDecimal(data.get("currentPrice").toString()));
            }
            if (data.get("previousClose") != null) {
                fii.setPreviousClose(new BigDecimal(data.get("previousClose").toString()));
            }
            if (data.get("volume") != null) {
                fii.setVolume(Long.valueOf(data.get("volume").toString()));
            }
            if (data.get("marketCap") != null) {
                fii.setMarketCap(new BigDecimal(data.get("marketCap").toString()));
            }
            if (data.get("dividendYield") != null) {
                fii.setDividendYield(new BigDecimal(data.get("dividendYield").toString()));
            }
            if (data.get("lastDividend") != null) {
                fii.setLastDividend(new BigDecimal(data.get("lastDividend").toString()));
            }
            if (data.get("netWorth") != null) {
                fii.setNetWorth(new BigDecimal(data.get("netWorth").toString()));
            }

            fii.setLastUpdated(LocalDateTime.now());
            fii.setIsActive(true);

            return fii;
        } catch (Exception e) {
            log.error("Error mapping FII data", e);
            return null;
        }
    }
}
