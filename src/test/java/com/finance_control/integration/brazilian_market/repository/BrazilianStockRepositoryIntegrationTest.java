package com.finance_control.integration.brazilian_market.repository;

import com.finance_control.brazilian_market.model.BrazilianStock;
import com.finance_control.brazilian_market.repository.BrazilianStockRepository;
import com.finance_control.integration.BaseIntegrationTest;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for BrazilianStockRepository.
 */
@ActiveProfiles("test")
class BrazilianStockRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BrazilianStockRepository stockRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private BrazilianStock testStock1;
    private BrazilianStock testStock2;
    private BrazilianStock testStock3;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        // Create test stocks
        testStock1 = createTestStock("PETR4", "Petrobras", BrazilianStock.StockType.ORDINARY,
                BrazilianStock.MarketSegment.NOVO_MERCADO, new BigDecimal("25.50"), testUser);

        testStock2 = createTestStock("VALE3", "Vale S.A.", BrazilianStock.StockType.ORDINARY,
                BrazilianStock.MarketSegment.NOVO_MERCADO, new BigDecimal("45.20"), testUser);

        testStock3 = createTestStock("ITUB4", "Itaú Unibanco", BrazilianStock.StockType.PREFERRED,
                BrazilianStock.MarketSegment.LEVEL_1, new BigDecimal("28.75"), testUser);

        stockRepository.saveAll(List.of(testStock1, testStock2, testStock3));
    }

    @Test
    void findByTickerAndUserId_WithValidTickerAndUser_ShouldReturnStock() {
        // When
        Optional<BrazilianStock> result = stockRepository.findByTickerAndUserId("PETR4", testUser.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTicker()).isEqualTo("PETR4");
        assertThat(result.get().getCompanyName()).isEqualTo("Petrobras");
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findByTickerAndUserId_WithInvalidTicker_ShouldReturnEmpty() {
        // When
        Optional<BrazilianStock> result = stockRepository.findByTickerAndUserId("INVALID", testUser.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_ShouldReturnAllUserStocks() {
        // When
        List<BrazilianStock> result = stockRepository.findByUserId(testUser.getId());

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(BrazilianStock::getTicker)
                .containsExactlyInAnyOrder("PETR4", "VALE3", "ITUB4");
    }

    @Test
    void findByUserId_WithPagination_ShouldReturnPaginatedResults() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 2);

        // When
        Page<BrazilianStock> result = stockRepository.findByUserId(testUser.getId(), pageRequest);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void findByStockTypeAndUserId_ShouldReturnFilteredStocks() {
        // When
        List<BrazilianStock> result = stockRepository.findByStockTypeAndUserId(
                BrazilianStock.StockType.ORDINARY, testUser.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(BrazilianStock::getTicker)
                .containsExactlyInAnyOrder("PETR4", "VALE3");
    }

    @Test
    void findBySegmentAndUserId_ShouldReturnFilteredStocks() {
        // When
        List<BrazilianStock> result = stockRepository.findBySegmentAndUserId(
                BrazilianStock.MarketSegment.NOVO_MERCADO, testUser.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(BrazilianStock::getTicker)
                .containsExactlyInAnyOrder("PETR4", "VALE3");
    }

    @Test
    void findByIsActiveTrueAndUserId_ShouldReturnActiveStocks() {
        // Given
        testStock3.setIsActive(false);
        stockRepository.save(testStock3);

        // When
        List<BrazilianStock> result = stockRepository.findByIsActiveTrueAndUserId(testUser.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(BrazilianStock::getTicker)
                .containsExactlyInAnyOrder("PETR4", "VALE3");
    }

    @Test
    void searchByUserAndQuery_WithTicker_ShouldReturnMatchingStocks() {
        // When
        List<BrazilianStock> result = stockRepository.searchByUserAndQuery(testUser.getId(), "PETR");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("PETR4");
    }

    @Test
    void searchByUserAndQuery_WithCompanyName_ShouldReturnMatchingStocks() {
        // When
        List<BrazilianStock> result = stockRepository.searchByUserAndQuery(testUser.getId(), "Petrobras");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("PETR4");
    }

    @Test
    void searchByUserAndQuery_WithPagination_ShouldReturnPaginatedResults() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 1);

        // When
        Page<BrazilianStock> result = stockRepository.searchByUserAndQuery(testUser.getId(), "VALE", pageRequest);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTicker()).isEqualTo("VALE3");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findByUserAndPriceChangeAbove_ShouldReturnStocksWithPositiveChange() {
        // Given
        testStock1.setDayChangePercent(new BigDecimal("5.25"));
        testStock2.setDayChangePercent(new BigDecimal("-2.10"));
        testStock3.setDayChangePercent(new BigDecimal("8.75"));
        stockRepository.saveAll(List.of(testStock1, testStock2, testStock3));

        // When
        List<BrazilianStock> result = stockRepository.findByUserAndPriceChangeAbove(
                testUser.getId(), new BigDecimal("5.0"));

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(BrazilianStock::getTicker)
                .containsExactlyInAnyOrder("PETR4", "ITUB4");
    }

    @Test
    void findByUserAndPriceChangeBelow_ShouldReturnStocksWithNegativeChange() {
        // Given
        testStock1.setDayChangePercent(new BigDecimal("5.25"));
        testStock2.setDayChangePercent(new BigDecimal("-2.10"));
        testStock3.setDayChangePercent(new BigDecimal("8.75"));
        stockRepository.saveAll(List.of(testStock1, testStock2, testStock3));

        // When
        List<BrazilianStock> result = stockRepository.findByUserAndPriceChangeBelow(
                testUser.getId(), new BigDecimal("0.0"));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("VALE3");
    }

    @Test
    void findTopByUserOrderByVolumeDesc_ShouldReturnStocksOrderedByVolume() {
        // Given
        testStock1.setVolume(1000000L);
        testStock2.setVolume(2000000L);
        testStock3.setVolume(1500000L);
        stockRepository.saveAll(List.of(testStock1, testStock2, testStock3));

        // When
        List<BrazilianStock> result = stockRepository.findTopByUserOrderByVolumeDesc(
                testUser.getId(), PageRequest.of(0, 2));

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTicker()).isEqualTo("VALE3");
        assertThat(result.get(1).getTicker()).isEqualTo("ITUB4");
    }

    @Test
    void findByUserAndMarketCapBetween_ShouldReturnStocksInRange() {
        // Given
        testStock1.setMarketCap(new BigDecimal("1000000000.00"));
        testStock2.setMarketCap(new BigDecimal("2000000000.00"));
        testStock3.setMarketCap(new BigDecimal("3000000000.00"));
        stockRepository.saveAll(List.of(testStock1, testStock2, testStock3));

        // When
        List<BrazilianStock> result = stockRepository.findByUserAndMarketCapBetween(
                testUser.getId(), new BigDecimal("1500000000.00"), new BigDecimal("2500000000.00"));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("VALE3");
    }

    @Test
    void countByStockTypeAndUserId_ShouldReturnCorrectCount() {
        // When
        long count = stockRepository.countByStockTypeAndUserId(BrazilianStock.StockType.ORDINARY, testUser.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countBySegmentAndUserId_ShouldReturnCorrectCount() {
        // When
        long count = stockRepository.countBySegmentAndUserId(BrazilianStock.MarketSegment.NOVO_MERCADO, testUser.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void existsByTickerAndUserId_WithExistingTicker_ShouldReturnTrue() {
        // When
        boolean exists = stockRepository.existsByTickerAndUserId("PETR4", testUser.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByTickerAndUserId_WithNonExistingTicker_ShouldReturnFalse() {
        // When
        boolean exists = stockRepository.existsByTickerAndUserId("INVALID", testUser.getId());

        // Then
        assertThat(exists).isFalse();
    }

    private BrazilianStock createTestStock(String ticker, String companyName,
                                         BrazilianStock.StockType stockType,
                                         BrazilianStock.MarketSegment segment,
                                         BigDecimal price, User user) {
        BrazilianStock stock = new BrazilianStock();
        stock.setTicker(ticker);
        stock.setCompanyName(companyName);
        stock.setStockType(stockType);
        stock.setSegment(segment);
        stock.setCurrentPrice(price);
        stock.setPreviousClose(price.subtract(new BigDecimal("1.00")));
        stock.setDayChange(new BigDecimal("1.00"));
        stock.setDayChangePercent(new BigDecimal("4.00"));
        stock.setVolume(1000000L);
        stock.setMarketCap(new BigDecimal("1000000000.00"));
        stock.setLastUpdated(LocalDateTime.now());
        stock.setIsActive(true);
        stock.setUser(user);
        return stock;
    }
}
