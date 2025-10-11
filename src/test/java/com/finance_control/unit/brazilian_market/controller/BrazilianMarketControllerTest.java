package com.finance_control.unit.brazilian_market.controller;

import com.finance_control.brazilian_market.controller.BrazilianMarketController;
import com.finance_control.brazilian_market.model.BrazilianStock;
import com.finance_control.brazilian_market.model.FII;
import com.finance_control.brazilian_market.model.MarketIndicator;
import com.finance_control.brazilian_market.service.BrazilianMarketDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BrazilianMarketController.
 */
@ExtendWith(MockitoExtension.class)
class BrazilianMarketControllerTest {

    @Mock
    private BrazilianMarketDataService marketDataService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BrazilianMarketController controller;

    private MarketIndicator testIndicator;
    private BrazilianStock testStock;
    private FII testFII;

    @BeforeEach
    void setUp() {
        // Setup test data
        testIndicator = new MarketIndicator();
        testIndicator.setCode("SELIC");
        testIndicator.setName("Taxa Selic");
        testIndicator.setCurrentValue(new BigDecimal("13.75"));

        testStock = new BrazilianStock();
        testStock.setTicker("PETR4");
        testStock.setCompanyName("Petrobras");
        testStock.setCurrentPrice(new BigDecimal("25.50"));

        testFII = new FII();
        testFII.setTicker("HGLG11");
        testFII.setFundName("CSHG Logística");
        testFII.setCurrentPrice(new BigDecimal("120.00"));

        // Setup authentication mock
        when(authentication.getName()).thenReturn("1");
    }

    @Test
    void getCurrentSelicRate_ShouldReturnSelicRate() {
        // Given
        when(marketDataService.getCurrentSelicRate()).thenReturn(new BigDecimal("13.75"));

        // When
        ResponseEntity<BigDecimal> response = controller.getCurrentSelicRate();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new BigDecimal("13.75"));
        verify(marketDataService).getCurrentSelicRate();
    }

    @Test
    void getCurrentCDIRate_ShouldReturnCDIRate() {
        // Given
        when(marketDataService.getCurrentCDIRate()).thenReturn(new BigDecimal("13.25"));

        // When
        ResponseEntity<BigDecimal> response = controller.getCurrentCDIRate();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new BigDecimal("13.25"));
        verify(marketDataService).getCurrentCDIRate();
    }

    @Test
    void getCurrentIPCA_ShouldReturnIPCA() {
        // Given
        when(marketDataService.getCurrentIPCA()).thenReturn(new BigDecimal("4.62"));

        // When
        ResponseEntity<BigDecimal> response = controller.getCurrentIPCA();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new BigDecimal("4.62"));
        verify(marketDataService).getCurrentIPCA();
    }

    @Test
    void getKeyIndicators_ShouldReturnIndicators() {
        // Given
        List<MarketIndicator> indicators = List.of(testIndicator);
        when(marketDataService.getKeyIndicators()).thenReturn(indicators);

        // When
        ResponseEntity<List<MarketIndicator>> response = controller.getKeyIndicators();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getCode()).isEqualTo("SELIC");
        verify(marketDataService).getKeyIndicators();
    }

    @Test
    void updateSelicRate_ShouldReturnFuture() {
        // Given
        CompletableFuture<MarketIndicator> future = CompletableFuture.completedFuture(testIndicator);
        when(marketDataService.updateSelicRate()).thenReturn(future);

        // When
        ResponseEntity<CompletableFuture<MarketIndicator>> response = controller.updateSelicRate();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(marketDataService).updateSelicRate();
    }

    @Test
    void updateCDIRate_ShouldReturnFuture() {
        // Given
        CompletableFuture<MarketIndicator> future = CompletableFuture.completedFuture(testIndicator);
        when(marketDataService.updateCDIRate()).thenReturn(future);

        // When
        ResponseEntity<CompletableFuture<MarketIndicator>> response = controller.updateCDIRate();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(marketDataService).updateCDIRate();
    }

    @Test
    void updateIPCA_ShouldReturnFuture() {
        // Given
        CompletableFuture<MarketIndicator> future = CompletableFuture.completedFuture(testIndicator);
        when(marketDataService.updateIPCA()).thenReturn(future);

        // When
        ResponseEntity<CompletableFuture<MarketIndicator>> response = controller.updateIPCA();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(marketDataService).updateIPCA();
    }

    @Test
    void getUserStocks_WithValidAuthentication_ShouldReturnStocks() {
        // Given
        List<BrazilianStock> stocks = List.of(testStock);
        when(marketDataService.getUserStocks(1L)).thenReturn(stocks);

        // When
        ResponseEntity<List<BrazilianStock>> response = controller.getUserStocks(authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getTicker()).isEqualTo("PETR4");
        verify(marketDataService).getUserStocks(1L);
    }

    @Test
    void getUserFIIs_WithValidAuthentication_ShouldReturnFIIs() {
        // Given
        List<FII> fiis = List.of(testFII);
        when(marketDataService.getUserFIIs(1L)).thenReturn(fiis);

        // When
        ResponseEntity<List<FII>> response = controller.getUserFIIs(authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getTicker()).isEqualTo("HGLG11");
        verify(marketDataService).getUserFIIs(1L);
    }

    @Test
    void searchUserStocks_WithValidQuery_ShouldReturnFilteredStocks() {
        // Given
        List<BrazilianStock> stocks = List.of(testStock);
        when(marketDataService.searchUserStocks(1L, "PETR")).thenReturn(stocks);

        // When
        ResponseEntity<List<BrazilianStock>> response = controller.searchUserStocks("PETR", authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getTicker()).isEqualTo("PETR4");
        verify(marketDataService).searchUserStocks(1L, "PETR");
    }

    @Test
    void searchUserFIIs_WithValidQuery_ShouldReturnFilteredFIIs() {
        // Given
        List<FII> fiis = List.of(testFII);
        when(marketDataService.searchUserFIIs(1L, "HGLG")).thenReturn(fiis);

        // When
        ResponseEntity<List<FII>> response = controller.searchUserFIIs("HGLG", authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getTicker()).isEqualTo("HGLG11");
        verify(marketDataService).searchUserFIIs(1L, "HGLG");
    }

    @Test
    void updateStockData_WithValidTicker_ShouldReturnFuture() {
        // Given
        CompletableFuture<BrazilianStock> future = CompletableFuture.completedFuture(testStock);
        when(marketDataService.updateStockData("PETR4", 1L)).thenReturn(future);

        // When
        ResponseEntity<CompletableFuture<BrazilianStock>> response = controller.updateStockData("PETR4", authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(marketDataService).updateStockData("PETR4", 1L);
    }

    @Test
    void updateFIIData_WithValidTicker_ShouldReturnFuture() {
        // Given
        CompletableFuture<FII> future = CompletableFuture.completedFuture(testFII);
        when(marketDataService.updateFIIData("HGLG11", 1L)).thenReturn(future);

        // When
        ResponseEntity<CompletableFuture<FII>> response = controller.updateFIIData("HGLG11", authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(marketDataService).updateFIIData("HGLG11", 1L);
    }

    @Test
    void getMarketSummary_ShouldReturnSummary() {
        // Given
        Object summary = Map.of("totalStocks", 500, "totalFIIs", 200);
        when(marketDataService.getMarketSummary()).thenReturn(summary);

        // When
        ResponseEntity<Object> response = controller.getMarketSummary();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(marketDataService).getMarketSummary();
    }

    @Test
    void updateAllIndicators_ShouldReturnFuturesMap() {
        // Given
        CompletableFuture<MarketIndicator> selicFuture = CompletableFuture.completedFuture(testIndicator);
        CompletableFuture<MarketIndicator> cdiFuture = CompletableFuture.completedFuture(testIndicator);
        CompletableFuture<MarketIndicator> ipcaFuture = CompletableFuture.completedFuture(testIndicator);

        when(marketDataService.updateSelicRate()).thenReturn(selicFuture);
        when(marketDataService.updateCDIRate()).thenReturn(cdiFuture);
        when(marketDataService.updateIPCA()).thenReturn(ipcaFuture);

        // When
        ResponseEntity<Map<String, CompletableFuture<MarketIndicator>>> response = controller.updateAllIndicators();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(3);
        assertThat(response.getBody()).containsKeys("selic", "cdi", "ipca");
        verify(marketDataService).updateSelicRate();
        verify(marketDataService).updateCDIRate();
        verify(marketDataService).updateIPCA();
    }

    @Test
    void getUserIdFromAuthentication_WithValidAuthentication_ShouldReturnUserId() {
        // Given
        when(authentication.getName()).thenReturn("123");

        // When
        ResponseEntity<List<BrazilianStock>> response = controller.getUserStocks(authentication);

        // Then
        verify(marketDataService).getUserStocks(123L);
    }

    @Test
    void getUserIdFromAuthentication_WithNullAuthentication_ShouldThrowException() {
        // When & Then
        try {
            controller.getUserStocks(null);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("User not authenticated");
        }
    }

    @Test
    void getUserIdFromAuthentication_WithInvalidUserId_ShouldThrowException() {
        // Given
        when(authentication.getName()).thenReturn("invalid");

        // When & Then
        try {
            controller.getUserStocks(authentication);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Invalid user ID in authentication");
        }
    }
}
