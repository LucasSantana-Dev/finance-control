package com.finance_control.brazilian_market.service;

import com.finance_control.brazilian_market.client.BCBApiClient;
import com.finance_control.brazilian_market.client.BrazilianStocksApiClient;
import com.finance_control.brazilian_market.model.BrazilianStock;
import com.finance_control.brazilian_market.model.FII;
import com.finance_control.brazilian_market.model.MarketIndicator;
import com.finance_control.brazilian_market.repository.*;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BrazilianMarketDataService.
 */
@ExtendWith(MockitoExtension.class)
class BrazilianMarketDataServiceTest {

    @Mock
    private BCBApiClient bcbApiClient;

    @Mock
    private BrazilianStocksApiClient stocksApiClient;

    @Mock
    private BrazilianStockRepository stockRepository;

    @Mock
    private FIIRepository fiiRepository;

    @Mock
    private MarketIndicatorRepository indicatorRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BrazilianMarketDataService marketDataService;

    private User testUser;
    private BrazilianStock testStock;
    private FII testFII;
    private MarketIndicator testIndicator;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testStock = new BrazilianStock();
        testStock.setId(1L);
        testStock.setTicker("PETR4");
        testStock.setCompanyName("Petrobras");
        testStock.setCurrentPrice(new BigDecimal("25.50"));
        testStock.setUser(testUser);

        testFII = new FII();
        testFII.setId(1L);
        testFII.setTicker("HGLG11");
        testFII.setFundName("CSHG Logística");
        testFII.setCurrentPrice(new BigDecimal("120.00"));
        testFII.setUser(testUser);

        testIndicator = new MarketIndicator();
        testIndicator.setId(1L);
        testIndicator.setCode("SELIC");
        testIndicator.setName("Taxa Selic");
        testIndicator.setCurrentValue(new BigDecimal("13.75"));
    }

    @Test
    void getCurrentSelicRate_ShouldReturnSelicRate() {
        // Given
        when(indicatorRepository.findByCode("SELIC"))
                .thenReturn(Optional.of(testIndicator));

        // When
        BigDecimal result = marketDataService.getCurrentSelicRate();

        // Then
        assertThat(result).isEqualTo(new BigDecimal("13.75"));
        verify(indicatorRepository).findByCode("SELIC");
    }

    @Test
    void getCurrentSelicRate_WhenIndicatorNotFound_ShouldReturnZero() {
        // Given
        when(indicatorRepository.findByCode("SELIC"))
                .thenReturn(Optional.empty());

        // When
        BigDecimal result = marketDataService.getCurrentSelicRate();

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void getCurrentCDIRate_ShouldReturnCDIRate() {
        // Given
        testIndicator.setCode("CDI");
        testIndicator.setCurrentValue(new BigDecimal("13.25"));
        when(indicatorRepository.findByCode("CDI"))
                .thenReturn(Optional.of(testIndicator));

        // When
        BigDecimal result = marketDataService.getCurrentCDIRate();

        // Then
        assertThat(result).isEqualTo(new BigDecimal("13.25"));
        verify(indicatorRepository).findByCode("CDI");
    }

    @Test
    void getCurrentIPCA_ShouldReturnIPCA() {
        // Given
        testIndicator.setCode("IPCA");
        testIndicator.setCurrentValue(new BigDecimal("4.62"));
        when(indicatorRepository.findByCode("IPCA"))
                .thenReturn(Optional.of(testIndicator));

        // When
        BigDecimal result = marketDataService.getCurrentIPCA();

        // Then
        assertThat(result).isEqualTo(new BigDecimal("4.62"));
        verify(indicatorRepository).findByCode("IPCA");
    }

    @Test
    void getKeyIndicators_ShouldReturnKeyIndicators() {
        // Given
        List<MarketIndicator> indicators = List.of(testIndicator);
        when(indicatorRepository.findKeyIndicators()).thenReturn(indicators);

        // When
        List<MarketIndicator> result = marketDataService.getKeyIndicators();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("SELIC");
        verify(indicatorRepository).findKeyIndicators();
    }

    @Test
    void getUserStocks_ShouldReturnUserStocks() {
        // Given
        List<BrazilianStock> stocks = List.of(testStock);
        when(stockRepository.findByUserId(1L)).thenReturn(stocks);

        // When
        List<BrazilianStock> result = marketDataService.getUserStocks(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("PETR4");
        verify(stockRepository).findByUserId(1L);
    }

    @Test
    void getUserFIIs_ShouldReturnUserFIIs() {
        // Given
        List<FII> fiis = List.of(testFII);
        when(fiiRepository.findByUserId(1L)).thenReturn(fiis);

        // When
        List<FII> result = marketDataService.getUserFIIs(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("HGLG11");
        verify(fiiRepository).findByUserId(1L);
    }

    @Test
    void searchUserStocks_ShouldReturnFilteredStocks() {
        // Given
        List<BrazilianStock> stocks = List.of(testStock);
        when(stockRepository.searchByUserAndQuery(1L, "PETR")).thenReturn(stocks);

        // When
        List<BrazilianStock> result = marketDataService.searchUserStocks(1L, "PETR");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("PETR4");
        verify(stockRepository).searchByUserAndQuery(1L, "PETR");
    }

    @Test
    void searchUserFIIs_ShouldReturnFilteredFIIs() {
        // Given
        List<FII> fiis = List.of(testFII);
        when(fiiRepository.searchByUserAndQuery(1L, "HGLG")).thenReturn(fiis);

        // When
        List<FII> result = marketDataService.searchUserFIIs(1L, "HGLG");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("HGLG11");
        verify(fiiRepository).searchByUserAndQuery(1L, "HGLG");
    }

    @Test
    void updateSelicRate_ShouldUpdateIndicator() {
        // Given
        BigDecimal newRate = new BigDecimal("13.50");
        when(bcbApiClient.getCurrentSelicRate()).thenReturn(newRate);
        when(indicatorRepository.findByCode("SELIC")).thenReturn(Optional.of(testIndicator));
        when(indicatorRepository.save(any(MarketIndicator.class))).thenReturn(testIndicator);

        // When
        CompletableFuture<MarketIndicator> future = marketDataService.updateSelicRate();

        // Then
        assertThat(future).isNotNull();
        verify(bcbApiClient).getCurrentSelicRate();
        verify(indicatorRepository).save(any(MarketIndicator.class));
    }

    @Test
    void updateStockData_ShouldUpdateExistingStock() {
        // Given
        String ticker = "PETR4";
        Long userId = 1L;
        BigDecimal newPrice = new BigDecimal("26.00");

        BrazilianStock updatedStock = new BrazilianStock();
        updatedStock.setTicker(ticker);
        updatedStock.setCurrentPrice(newPrice);
        updatedStock.setUser(testUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(stocksApiClient.getStockQuote(ticker)).thenReturn(updatedStock);
        when(stockRepository.findByTickerAndUserId(ticker, userId)).thenReturn(Optional.of(testStock));
        when(stockRepository.save(any(BrazilianStock.class))).thenReturn(testStock);

        // When
        CompletableFuture<BrazilianStock> future = marketDataService.updateStockData(ticker, userId);

        // Then
        assertThat(future).isNotNull();
        verify(userRepository).findById(userId);
        verify(stocksApiClient).getStockQuote(ticker);
        verify(stockRepository).findByTickerAndUserId(ticker, userId);
        verify(stockRepository).save(any(BrazilianStock.class));
    }

    @Test
    void updateFIIData_ShouldUpdateExistingFII() {
        // Given
        String ticker = "HGLG11";
        Long userId = 1L;
        BigDecimal newPrice = new BigDecimal("125.00");

        FII updatedFII = new FII();
        updatedFII.setTicker(ticker);
        updatedFII.setCurrentPrice(newPrice);
        updatedFII.setUser(testUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(stocksApiClient.getFIIQuote(ticker)).thenReturn(updatedFII);
        when(fiiRepository.findByTickerAndUserId(ticker, userId)).thenReturn(Optional.of(testFII));
        when(fiiRepository.save(any(FII.class))).thenReturn(testFII);

        // When
        CompletableFuture<FII> future = marketDataService.updateFIIData(ticker, userId);

        // Then
        assertThat(future).isNotNull();
        verify(userRepository).findById(userId);
        verify(stocksApiClient).getFIIQuote(ticker);
        verify(fiiRepository).findByTickerAndUserId(ticker, userId);
        verify(fiiRepository).save(any(FII.class));
    }

    @Test
    void getMarketSummary_ShouldReturnSummary() {
        // Given
        Map<String, Object> summary = Map.of("totalStocks", 500, "totalFIIs", 200);
        when(stocksApiClient.getMarketSummary()).thenReturn(summary);

        // When
        Object result = marketDataService.getMarketSummary();

        // Then
        assertThat(result).isEqualTo(summary);
        verify(stocksApiClient).getMarketSummary();
    }
}
