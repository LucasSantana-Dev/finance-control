package com.finance_control.integration.brazilian_market.repository;

import com.finance_control.brazilian_market.model.FII;
import com.finance_control.brazilian_market.repository.FIIRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for FIIRepository.
 */
@ActiveProfiles("test")
class FIIRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private FIIRepository fiiRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private FII testFII1;
    private FII testFII2;
    private FII testFII3;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        // Create test FIIs
        testFII1 = createTestFII("HGLG11", "CSHG Logística", FII.FIIType.TIJOLO, 
                FII.FIISegment.LOGISTICS, new BigDecimal("120.00"), testUser);
        
        testFII2 = createTestFII("XPML11", "XP Malls", FII.FIIType.TIJOLO, 
                FII.FIISegment.SHOPPING, new BigDecimal("95.50"), testUser);
        
        testFII3 = createTestFII("HABT11", "Habitat II", FII.FIIType.PAPEL, 
                FII.FIISegment.RESIDENTIAL, new BigDecimal("85.25"), testUser);

        fiiRepository.saveAll(List.of(testFII1, testFII2, testFII3));
    }

    @Test
    void findByTickerAndUserId_WithValidTickerAndUser_ShouldReturnFII() {
        // When
        Optional<FII> result = fiiRepository.findByTickerAndUserId("HGLG11", testUser.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTicker()).isEqualTo("HGLG11");
        assertThat(result.get().getFundName()).isEqualTo("CSHG Logística");
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findByTickerAndUserId_WithInvalidTicker_ShouldReturnEmpty() {
        // When
        Optional<FII> result = fiiRepository.findByTickerAndUserId("INVALID", testUser.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_ShouldReturnAllUserFIIs() {
        // When
        List<FII> result = fiiRepository.findByUserId(testUser.getId());

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(FII::getTicker)
                .containsExactlyInAnyOrder("HGLG11", "XPML11", "HABT11");
    }

    @Test
    void findByUserId_WithPagination_ShouldReturnPaginatedResults() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 2);

        // When
        Page<FII> result = fiiRepository.findByUserId(testUser.getId(), pageRequest);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void findByFiiTypeAndUserId_ShouldReturnFilteredFIIs() {
        // When
        List<FII> result = fiiRepository.findByFiiTypeAndUserId(FII.FIIType.TIJOLO, testUser.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FII::getTicker)
                .containsExactlyInAnyOrder("HGLG11", "XPML11");
    }

    @Test
    void findBySegmentAndUserId_ShouldReturnFilteredFIIs() {
        // When
        List<FII> result = fiiRepository.findBySegmentAndUserId(FII.FIISegment.LOGISTICS, testUser.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("HGLG11");
    }

    @Test
    void findByIsActiveTrueAndUserId_ShouldReturnActiveFIIs() {
        // Given
        testFII3.setIsActive(false);
        fiiRepository.save(testFII3);

        // When
        List<FII> result = fiiRepository.findByIsActiveTrueAndUserId(testUser.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FII::getTicker)
                .containsExactlyInAnyOrder("HGLG11", "XPML11");
    }

    @Test
    void searchByUserAndQuery_WithTicker_ShouldReturnMatchingFIIs() {
        // When
        List<FII> result = fiiRepository.searchByUserAndQuery(testUser.getId(), "HGLG");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("HGLG11");
    }

    @Test
    void searchByUserAndQuery_WithFundName_ShouldReturnMatchingFIIs() {
        // When
        List<FII> result = fiiRepository.searchByUserAndQuery(testUser.getId(), "CSHG");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("HGLG11");
    }

    @Test
    void searchByUserAndQuery_WithPagination_ShouldReturnPaginatedResults() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 1);

        // When
        Page<FII> result = fiiRepository.searchByUserAndQuery(testUser.getId(), "XP", pageRequest);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTicker()).isEqualTo("XPML11");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findByUserAndDividendYieldAbove_ShouldReturnFIIsWithHighYield() {
        // Given
        testFII1.setDividendYield(new BigDecimal("8.50"));
        testFII2.setDividendYield(new BigDecimal("6.25"));
        testFII3.setDividendYield(new BigDecimal("9.75"));
        fiiRepository.saveAll(List.of(testFII1, testFII2, testFII3));

        // When
        List<FII> result = fiiRepository.findByUserAndDividendYieldAbove(
                testUser.getId(), new BigDecimal("8.0"));

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FII::getTicker)
                .containsExactlyInAnyOrder("HGLG11", "HABT11");
    }

    @Test
    void findByUserAndPVPRatioBelow_ShouldReturnUndervaluedFIIs() {
        // Given
        testFII1.setPvpRatio(new BigDecimal("0.95"));
        testFII2.setPvpRatio(new BigDecimal("1.05"));
        testFII3.setPvpRatio(new BigDecimal("0.88"));
        fiiRepository.saveAll(List.of(testFII1, testFII2, testFII3));

        // When
        List<FII> result = fiiRepository.findByUserAndPVPRatioBelow(
                testUser.getId(), new BigDecimal("1.0"));

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FII::getTicker)
                .containsExactlyInAnyOrder("HGLG11", "HABT11");
    }

    @Test
    void findByUserAndPVPRatioAbove_ShouldReturnOvervaluedFIIs() {
        // Given
        testFII1.setPvpRatio(new BigDecimal("0.95"));
        testFII2.setPvpRatio(new BigDecimal("1.05"));
        testFII3.setPvpRatio(new BigDecimal("0.88"));
        fiiRepository.saveAll(List.of(testFII1, testFII2, testFII3));

        // When
        List<FII> result = fiiRepository.findByUserAndPVPRatioAbove(
                testUser.getId(), new BigDecimal("1.0"));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("XPML11");
    }

    @Test
    void findTopByUserOrderByVolumeDesc_ShouldReturnFIIsOrderedByVolume() {
        // Given
        testFII1.setVolume(1000000L);
        testFII2.setVolume(2000000L);
        testFII3.setVolume(1500000L);
        fiiRepository.saveAll(List.of(testFII1, testFII2, testFII3));

        // When
        List<FII> result = fiiRepository.findTopByUserOrderByVolumeDesc(
                testUser.getId(), PageRequest.of(0, 2));

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTicker()).isEqualTo("XPML11");
        assertThat(result.get(1).getTicker()).isEqualTo("HABT11");
    }

    @Test
    void findByUserAndMarketCapBetween_ShouldReturnFIIsInRange() {
        // Given
        testFII1.setMarketCap(new BigDecimal("1000000000.00"));
        testFII2.setMarketCap(new BigDecimal("2000000000.00"));
        testFII3.setMarketCap(new BigDecimal("3000000000.00"));
        fiiRepository.saveAll(List.of(testFII1, testFII2, testFII3));

        // When
        List<FII> result = fiiRepository.findByUserAndMarketCapBetween(
                testUser.getId(), new BigDecimal("1500000000.00"), new BigDecimal("2500000000.00"));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("XPML11");
    }

    @Test
    void findByUserAndRecentDividends_ShouldReturnFIIsWithRecentDividends() {
        // Given
        testFII1.setLastDividendDate(LocalDate.now().minusDays(10));
        testFII2.setLastDividendDate(LocalDate.now().minusDays(40));
        testFII3.setLastDividendDate(LocalDate.now().minusDays(5));
        fiiRepository.saveAll(List.of(testFII1, testFII2, testFII3));

        // When
        List<FII> result = fiiRepository.findByUserAndRecentDividends(
                testUser.getId(), LocalDate.now().minusDays(30));

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(FII::getTicker)
                .containsExactlyInAnyOrder("HGLG11", "HABT11");
    }

    @Test
    void countByFiiTypeAndUserId_ShouldReturnCorrectCount() {
        // When
        long count = fiiRepository.countByFiiTypeAndUserId(FII.FIIType.TIJOLO, testUser.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countBySegmentAndUserId_ShouldReturnCorrectCount() {
        // When
        long count = fiiRepository.countBySegmentAndUserId(FII.FIISegment.LOGISTICS, testUser.getId());

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void existsByTickerAndUserId_WithExistingTicker_ShouldReturnTrue() {
        // When
        boolean exists = fiiRepository.existsByTickerAndUserId("HGLG11", testUser.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByTickerAndUserId_WithNonExistingTicker_ShouldReturnFalse() {
        // When
        boolean exists = fiiRepository.existsByTickerAndUserId("INVALID", testUser.getId());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findTopByUserOrderByDividendYieldDesc_ShouldReturnFIIsOrderedByYield() {
        // Given
        testFII1.setDividendYield(new BigDecimal("8.50"));
        testFII2.setDividendYield(new BigDecimal("6.25"));
        testFII3.setDividendYield(new BigDecimal("9.75"));
        fiiRepository.saveAll(List.of(testFII1, testFII2, testFII3));

        // When
        List<FII> result = fiiRepository.findTopByUserOrderByDividendYieldDesc(
                testUser.getId(), PageRequest.of(0, 2));

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTicker()).isEqualTo("HABT11");
        assertThat(result.get(1).getTicker()).isEqualTo("HGLG11");
    }

    private FII createTestFII(String ticker, String fundName, FII.FIIType fiiType, 
                            FII.FIISegment segment, BigDecimal price, User user) {
        FII fii = new FII();
        fii.setTicker(ticker);
        fii.setFundName(fundName);
        fii.setFiiType(fiiType);
        fii.setSegment(segment);
        fii.setCurrentPrice(price);
        fii.setPreviousClose(price.subtract(new BigDecimal("1.00")));
        fii.setDayChange(new BigDecimal("1.00"));
        fii.setDayChangePercent(new BigDecimal("0.84"));
        fii.setVolume(1000000L);
        fii.setMarketCap(new BigDecimal("1000000000.00"));
        fii.setDividendYield(new BigDecimal("7.50"));
        fii.setLastDividend(new BigDecimal("0.80"));
        fii.setLastDividendDate(LocalDate.now().minusDays(15));
        fii.setNetWorth(new BigDecimal("115.00"));
        fii.setPvpRatio(new BigDecimal("1.04"));
        fii.setLastUpdated(LocalDateTime.now());
        fii.setIsActive(true);
        fii.setUser(user);
        return fii;
    }
}
