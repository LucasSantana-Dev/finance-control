package com.finance_control.unit.brazilian_market.repository;

import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.repository.InvestmentRepository;
import com.finance_control.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @DataJpaTest repository layer test for InvestmentRepository.
 * Tests JPA repository operations and custom queries with in-memory H2 database.
 */
@DataJpaTest
@ActiveProfiles("test")
class InvestmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InvestmentRepository investmentRepository;

    private User testUser;
    private Investment testInvestment;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = entityManager.persistAndFlush(testUser);

        // Create test investment
        testInvestment = new Investment();
        testInvestment.setUser(testUser);
        testInvestment.setName("Test Investment");
        testInvestment.setTicker("TEST4");
        testInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        testInvestment.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        testInvestment.setSector("Technology");
        testInvestment.setIndustry("Software");
        testInvestment.setExchange("B3");
        testInvestment.setCurrentPrice(BigDecimal.valueOf(100.00));
        testInvestment.setVolume(1000L);
        testInvestment.setDividendYield(BigDecimal.valueOf(5.5));
        testInvestment.setDayChangePercent(BigDecimal.valueOf(2.3));
        testInvestment.setIsActive(true);
        testInvestment.setLastUpdated(LocalDateTime.now().minusHours(1));
        testInvestment.setCreatedAt(LocalDateTime.now());
        testInvestment.setUpdatedAt(LocalDateTime.now());
        testInvestment = entityManager.persistAndFlush(testInvestment);
    }

    @Test
    void shouldFindInvestmentById() {
        // When
        Optional<Investment> found = investmentRepository.findById(testInvestment.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Investment");
        assertThat(found.get().getTicker()).isEqualTo("TEST4");
        assertThat(found.get().getCurrentPrice()).isEqualTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void shouldFindInvestmentsByUserId() {
        // Given - Create another user with investment
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi");
        anotherUser.setIsActive(true);
        anotherUser.setCreatedAt(LocalDateTime.now());
        anotherUser.setUpdatedAt(LocalDateTime.now());
        anotherUser = entityManager.persistAndFlush(anotherUser);

        Investment anotherInvestment = new Investment();
        anotherInvestment.setUser(anotherUser);
        anotherInvestment.setName("Another Investment");
        anotherInvestment.setTicker("ANOTH4");
        anotherInvestment.setInvestmentType(Investment.InvestmentType.FII);
        anotherInvestment.setIsActive(true);
        anotherInvestment.setCreatedAt(LocalDateTime.now());
        anotherInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(anotherInvestment);

        // When
        List<Investment> userInvestments = investmentRepository.findByUser_Id(testUser.getId());

        // Then
        assertThat(userInvestments).hasSize(1);
        assertThat(userInvestments.get(0).getName()).isEqualTo("Test Investment");
        assertThat(userInvestments.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFindActiveInvestmentsByUserId() {
        // Given - Create inactive investment
        Investment inactiveInvestment = new Investment();
        inactiveInvestment.setUser(testUser);
        inactiveInvestment.setName("Inactive Investment");
        inactiveInvestment.setTicker("INACT4");
        inactiveInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        inactiveInvestment.setIsActive(false);
        inactiveInvestment.setCreatedAt(LocalDateTime.now());
        inactiveInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(inactiveInvestment);

        // When
        List<Investment> activeInvestments = investmentRepository.findByUser_IdAndIsActiveTrue(testUser.getId());

        // Then
        assertThat(activeInvestments).hasSize(1);
        assertThat(activeInvestments.get(0).getName()).isEqualTo("Test Investment");
        assertThat(activeInvestments.get(0).getIsActive()).isTrue();
    }

    @Test
    void shouldFindInvestmentsByUserIdAndType() {
        // Given - Create different type investment
        Investment fiiInvestment = new Investment();
        fiiInvestment.setUser(testUser);
        fiiInvestment.setName("FII Investment");
        fiiInvestment.setTicker("FII11");
        fiiInvestment.setInvestmentType(Investment.InvestmentType.FII);
        fiiInvestment.setIsActive(true);
        fiiInvestment.setCreatedAt(LocalDateTime.now());
        fiiInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(fiiInvestment);

        // When
        List<Investment> stockInvestments = investmentRepository.findByUser_IdAndInvestmentTypeAndIsActiveTrue(
            testUser.getId(), Investment.InvestmentType.STOCK);

        // Then
        assertThat(stockInvestments).hasSize(1);
        assertThat(stockInvestments.get(0).getInvestmentType()).isEqualTo(Investment.InvestmentType.STOCK);
        assertThat(stockInvestments.get(0).getName()).isEqualTo("Test Investment");
    }

    @Test
    void shouldFindInvestmentsByUserIdAndTypeAndSubtype() {
        // Given - Create different subtype investment
        Investment preferredInvestment = new Investment();
        preferredInvestment.setUser(testUser);
        preferredInvestment.setName("Preferred Investment");
        preferredInvestment.setTicker("PREF4");
        preferredInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        preferredInvestment.setInvestmentSubtype(Investment.InvestmentSubtype.PREFERRED);
        preferredInvestment.setIsActive(true);
        preferredInvestment.setCreatedAt(LocalDateTime.now());
        preferredInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(preferredInvestment);

        // When
        List<Investment> commonInvestments = investmentRepository.findByUser_IdAndInvestmentTypeAndInvestmentSubtypeAndIsActiveTrue(
            testUser.getId(), Investment.InvestmentType.STOCK, Investment.InvestmentSubtype.ORDINARY);

        // Then
        assertThat(commonInvestments).hasSize(1);
        assertThat(commonInvestments.get(0).getInvestmentSubtype()).isEqualTo(Investment.InvestmentSubtype.ORDINARY);
        assertThat(commonInvestments.get(0).getName()).isEqualTo("Test Investment");
    }

    @Test
    void shouldFindInvestmentsBySector() {
        // Given - Create different sector investment
        Investment financeInvestment = new Investment();
        financeInvestment.setUser(testUser);
        financeInvestment.setName("Finance Investment");
        financeInvestment.setTicker("FINC4");
        financeInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        financeInvestment.setSector("Finance");
        financeInvestment.setIsActive(true);
        financeInvestment.setCreatedAt(LocalDateTime.now());
        financeInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(financeInvestment);

        // When
        List<Investment> techInvestments = investmentRepository.findByUser_IdAndSectorAndIsActiveTrue(
            testUser.getId(), "Technology");

        // Then
        assertThat(techInvestments).hasSize(1);
        assertThat(techInvestments.get(0).getSector()).isEqualTo("Technology");
        assertThat(techInvestments.get(0).getName()).isEqualTo("Test Investment");
    }

    @Test
    void shouldFindInvestmentsByIndustry() {
        // Given - Create different industry investment
        Investment retailInvestment = new Investment();
        retailInvestment.setUser(testUser);
        retailInvestment.setName("Retail Investment");
        retailInvestment.setTicker("RETAIL4");
        retailInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        retailInvestment.setIndustry("Retail");
        retailInvestment.setIsActive(true);
        retailInvestment.setCreatedAt(LocalDateTime.now());
        retailInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(retailInvestment);

        // When
        List<Investment> softwareInvestments = investmentRepository.findByUser_IdAndIndustryAndIsActiveTrue(
            testUser.getId(), "Software");

        // Then
        assertThat(softwareInvestments).hasSize(1);
        assertThat(softwareInvestments.get(0).getIndustry()).isEqualTo("Software");
        assertThat(softwareInvestments.get(0).getName()).isEqualTo("Test Investment");
    }

    @Test
    void shouldFindInvestmentByTickerAndUser() {
        // When
        Optional<Investment> found = investmentRepository.findByTickerAndUser_IdAndIsActiveTrue(
            "TEST4", testUser.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTicker()).isEqualTo("TEST4");
        assertThat(found.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldFindInvestmentsNeedingPriceUpdate() {
        // Given - Create investment that needs update
        Investment oldInvestment = new Investment();
        oldInvestment.setUser(testUser);
        oldInvestment.setName("Old Investment");
        oldInvestment.setTicker("OLD4");
        oldInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        oldInvestment.setIsActive(true);
        oldInvestment.setLastUpdated(LocalDateTime.now().minusHours(25)); // Older than cutoff
        oldInvestment.setCreatedAt(LocalDateTime.now());
        oldInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(oldInvestment);

        // When
        List<Investment> needingUpdate = investmentRepository.findInvestmentsNeedingPriceUpdate(
            testUser.getId(), LocalDateTime.now().minusHours(2));

        // Then
        assertThat(needingUpdate).hasSize(1);
        assertThat(needingUpdate.get(0).getTicker()).isEqualTo("OLD4");
    }

    @Test
    void shouldFindDistinctSectorsByUserId() {
        // Given - Create investment with same sector
        Investment sameSectorInvestment = new Investment();
        sameSectorInvestment.setUser(testUser);
        sameSectorInvestment.setName("Same Sector Investment");
        sameSectorInvestment.setTicker("SAME4");
        sameSectorInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        sameSectorInvestment.setSector("Technology");
        sameSectorInvestment.setIsActive(true);
        sameSectorInvestment.setCreatedAt(LocalDateTime.now());
        sameSectorInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(sameSectorInvestment);

        // When
        List<String> sectors = investmentRepository.findDistinctSectorsByUserId(testUser.getId());

        // Then
        assertThat(sectors).hasSize(1);
        assertThat(sectors.get(0)).isEqualTo("Technology");
    }

    @Test
    void shouldFindDistinctIndustriesByUserId() {
        // Given - Create investment with same industry
        Investment sameIndustryInvestment = new Investment();
        sameIndustryInvestment.setUser(testUser);
        sameIndustryInvestment.setName("Same Industry Investment");
        sameIndustryInvestment.setTicker("INDUS4");
        sameIndustryInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        sameIndustryInvestment.setIndustry("Software");
        sameIndustryInvestment.setIsActive(true);
        sameIndustryInvestment.setCreatedAt(LocalDateTime.now());
        sameIndustryInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(sameIndustryInvestment);

        // When
        List<String> industries = investmentRepository.findDistinctIndustriesByUserId(testUser.getId());

        // Then
        assertThat(industries).hasSize(1);
        assertThat(industries.get(0)).isEqualTo("Software");
    }

    @Test
    void shouldFindDistinctInvestmentTypesByUserId() {
        // Given - Create FII investment
        Investment fiiInvestment = new Investment();
        fiiInvestment.setUser(testUser);
        fiiInvestment.setName("FII Investment");
        fiiInvestment.setTicker("FII11");
        fiiInvestment.setInvestmentType(Investment.InvestmentType.FII);
        fiiInvestment.setIsActive(true);
        fiiInvestment.setCreatedAt(LocalDateTime.now());
        fiiInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(fiiInvestment);

        // When
        List<Investment.InvestmentType> types = investmentRepository.findDistinctInvestmentTypesByUserId(testUser.getId());

        // Then
        assertThat(types).hasSize(2);
        assertThat(types).contains(Investment.InvestmentType.STOCK, Investment.InvestmentType.FII);
    }

    @Test
    void shouldFindDistinctInvestmentSubtypesByUserIdAndType() {
        // Given - Create preferred stock investment
        Investment preferredInvestment = new Investment();
        preferredInvestment.setUser(testUser);
        preferredInvestment.setName("Preferred Investment");
        preferredInvestment.setTicker("PREF4");
        preferredInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        preferredInvestment.setInvestmentSubtype(Investment.InvestmentSubtype.PREFERRED);
        preferredInvestment.setIsActive(true);
        preferredInvestment.setCreatedAt(LocalDateTime.now());
        preferredInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(preferredInvestment);

        // When
        List<Investment.InvestmentSubtype> subtypes = investmentRepository.findDistinctInvestmentSubtypesByUserIdAndType(
            testUser.getId(), Investment.InvestmentType.STOCK);

        // Then
        assertThat(subtypes).hasSize(2);
        assertThat(subtypes).contains(Investment.InvestmentSubtype.ORDINARY, Investment.InvestmentSubtype.PREFERRED);
    }

    @Test
    void shouldCountInvestmentsByType() {
        // Given - Create FII investment
        Investment fiiInvestment = new Investment();
        fiiInvestment.setUser(testUser);
        fiiInvestment.setName("FII Investment");
        fiiInvestment.setTicker("FII11");
        fiiInvestment.setInvestmentType(Investment.InvestmentType.FII);
        fiiInvestment.setCurrentPrice(BigDecimal.valueOf(50.00));
        fiiInvestment.setVolume(500L);
        fiiInvestment.setIsActive(true);
        fiiInvestment.setCreatedAt(LocalDateTime.now());
        fiiInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(fiiInvestment);

        // When
        List<Object[]> typeCounts = investmentRepository.countInvestmentsByType(testUser.getId());

        // Then
        assertThat(typeCounts).hasSize(2);
        // Each Object[] contains [InvestmentType, count]
        assertThat(typeCounts).anySatisfy(array -> {
            assertThat(array[0]).isEqualTo(Investment.InvestmentType.STOCK);
            assertThat(array[1]).isEqualTo(1L);
        });
        assertThat(typeCounts).anySatisfy(array -> {
            assertThat(array[0]).isEqualTo(Investment.InvestmentType.FII);
            assertThat(array[1]).isEqualTo(1L);
        });
    }

    @Test
    void shouldFindTopInvestmentsByDividendYield() {
        // Given - Create investments with different yields
        Investment highYieldInvestment = new Investment();
        highYieldInvestment.setUser(testUser);
        highYieldInvestment.setName("High Yield Investment");
        highYieldInvestment.setTicker("HIGH4");
        highYieldInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        highYieldInvestment.setDividendYield(BigDecimal.valueOf(8.5));
        highYieldInvestment.setIsActive(true);
        highYieldInvestment.setCreatedAt(LocalDateTime.now());
        highYieldInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(highYieldInvestment);

        // When
        Pageable pageable = PageRequest.of(0, 5);
        List<Investment> topYieldInvestments = investmentRepository.findTopInvestmentsByDividendYield(
            testUser.getId(), pageable);

        // Then
        assertThat(topYieldInvestments).hasSize(2);
        assertThat(topYieldInvestments.get(0).getDividendYield()).isGreaterThanOrEqualTo(
            topYieldInvestments.get(1).getDividendYield());
    }

    @Test
    void shouldFindTopInvestmentsByDayChange() {
        // Given - Create investment with positive change
        Investment positiveChangeInvestment = new Investment();
        positiveChangeInvestment.setUser(testUser);
        positiveChangeInvestment.setName("Positive Change Investment");
        positiveChangeInvestment.setTicker("POS4");
        positiveChangeInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        positiveChangeInvestment.setDayChangePercent(BigDecimal.valueOf(5.2));
        positiveChangeInvestment.setIsActive(true);
        positiveChangeInvestment.setCreatedAt(LocalDateTime.now());
        positiveChangeInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(positiveChangeInvestment);

        // When
        Pageable pageable = PageRequest.of(0, 5);
        List<Investment> topChangeInvestments = investmentRepository.findTopInvestmentsByDayChange(
            testUser.getId(), pageable);

        // Then
        assertThat(topChangeInvestments).hasSize(2);
        assertThat(topChangeInvestments.get(0).getDayChangePercent()).isGreaterThanOrEqualTo(
            topChangeInvestments.get(1).getDayChangePercent());
    }

    @Test
    void shouldFindBottomInvestmentsByDayChange() {
        // Given - Create investment with negative change
        Investment negativeChangeInvestment = new Investment();
        negativeChangeInvestment.setUser(testUser);
        negativeChangeInvestment.setName("Negative Change Investment");
        negativeChangeInvestment.setTicker("NEG4");
        negativeChangeInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        negativeChangeInvestment.setDayChangePercent(BigDecimal.valueOf(-2.1));
        negativeChangeInvestment.setIsActive(true);
        negativeChangeInvestment.setCreatedAt(LocalDateTime.now());
        negativeChangeInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(negativeChangeInvestment);

        // When
        Pageable pageable = PageRequest.of(0, 5);
        List<Investment> bottomChangeInvestments = investmentRepository.findBottomInvestmentsByDayChange(
            testUser.getId(), pageable);

        // Then
        assertThat(bottomChangeInvestments).hasSize(2);
        assertThat(bottomChangeInvestments.get(0).getDayChangePercent()).isLessThanOrEqualTo(
            bottomChangeInvestments.get(1).getDayChangePercent());
    }

    @Test
    void shouldSearchInvestments() {
        // Given - Create investment with different name
        Investment otherInvestment = new Investment();
        otherInvestment.setUser(testUser);
        otherInvestment.setName("Other Company Stock");
        otherInvestment.setTicker("OTHER4");
        otherInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        otherInvestment.setIsActive(true);
        otherInvestment.setCreatedAt(LocalDateTime.now());
        otherInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(otherInvestment);

        // When
        List<Investment> searchResults = investmentRepository.searchInvestments(testUser.getId(), "Test");

        // Then
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getName()).isEqualTo("Test Investment");
    }

    @Test
    void shouldFindInvestmentsByExchange() {
        // Given - Create NASDAQ investment
        Investment nasdaqInvestment = new Investment();
        nasdaqInvestment.setUser(testUser);
        nasdaqInvestment.setName("NASDAQ Investment");
        nasdaqInvestment.setTicker("NASDAQ");
        nasdaqInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        nasdaqInvestment.setExchange("NASDAQ");
        nasdaqInvestment.setIsActive(true);
        nasdaqInvestment.setCreatedAt(LocalDateTime.now());
        nasdaqInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(nasdaqInvestment);

        // When
        List<Investment> b3Investments = investmentRepository.findByUser_IdAndExchangeAndIsActiveTrue(
            testUser.getId(), "B3");

        // Then
        assertThat(b3Investments).hasSize(1);
        assertThat(b3Investments.get(0).getExchange()).isEqualTo("B3");
        assertThat(b3Investments.get(0).getName()).isEqualTo("Test Investment");
    }

    @Test
    void shouldCheckIfInvestmentExistsByTickerAndUser() {
        // When
        boolean exists = investmentRepository.existsByTickerAndUser_IdAndIsActiveTrue("TEST4", testUser.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldCheckIfInvestmentDoesNotExistByTickerAndUser() {
        // When
        boolean exists = investmentRepository.existsByTickerAndUser_IdAndIsActiveTrue("NONEXISTENT", testUser.getId());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindStaleInvestments() {
        // Given - Create very old investment
        Investment veryOldInvestment = new Investment();
        veryOldInvestment.setUser(testUser);
        veryOldInvestment.setName("Very Old Investment");
        veryOldInvestment.setTicker("OLD4");
        veryOldInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        veryOldInvestment.setIsActive(true);
        veryOldInvestment.setLastUpdated(LocalDateTime.now().minusDays(10));
        veryOldInvestment.setCreatedAt(LocalDateTime.now());
        veryOldInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(veryOldInvestment);

        // When
        List<Investment> staleInvestments = investmentRepository.findStaleInvestments(
            testUser.getId(), LocalDateTime.now().minusDays(5));

        // Then
        assertThat(staleInvestments).hasSize(1);
        assertThat(staleInvestments.get(0).getTicker()).isEqualTo("OLD4");
    }

    @Test
    void shouldGetTotalMarketValue() {
        // Given - Update test investment with volume
        testInvestment.setVolume(100L);
        testInvestment.setCurrentPrice(BigDecimal.valueOf(50.00));
        entityManager.persistAndFlush(testInvestment);

        // When
        Optional<Double> totalValue = investmentRepository.getTotalMarketValue(testUser.getId());

        // Then
        assertThat(totalValue).isPresent();
        assertThat(totalValue.get()).isEqualTo(5000.0); // 100 * 50.00
    }

    @Test
    void shouldGetTotalMarketValueByType() {
        // Given - Create FII investment
        Investment fiiInvestment = new Investment();
        fiiInvestment.setUser(testUser);
        fiiInvestment.setName("FII Investment");
        fiiInvestment.setTicker("FII11");
        fiiInvestment.setInvestmentType(Investment.InvestmentType.FII);
        fiiInvestment.setCurrentPrice(BigDecimal.valueOf(100.00));
        fiiInvestment.setVolume(200L);
        fiiInvestment.setIsActive(true);
        fiiInvestment.setCreatedAt(LocalDateTime.now());
        fiiInvestment.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(fiiInvestment);

        // When
        List<Object[]> marketValueByType = investmentRepository.getTotalMarketValueByType(testUser.getId());


        // Then
        assertThat(marketValueByType).hasSize(2);

        // Collect results into a map for easier verification
        Map<Investment.InvestmentType, BigDecimal> results = new HashMap<>();
        for (Object[] result : marketValueByType) {
            results.put((Investment.InvestmentType) result[0], (BigDecimal) result[1]);
        }

        // Verify STOCK total
        assertThat(results).containsKey(Investment.InvestmentType.STOCK);
        assertThat(results.get(Investment.InvestmentType.STOCK)).isEqualByComparingTo(BigDecimal.valueOf(100000)); // 1000 * 100.00

        // Verify FII total
        assertThat(results).containsKey(Investment.InvestmentType.FII);
        assertThat(results.get(Investment.InvestmentType.FII)).isEqualByComparingTo(BigDecimal.valueOf(20000)); // 200 * 100.00
    }

    @Test
    void shouldSaveInvestment() {
        // Given
        Investment newInvestment = new Investment();
        newInvestment.setUser(testUser);
        newInvestment.setName("New Investment");
        newInvestment.setTicker("NEW4");
        newInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        newInvestment.setCurrentPrice(BigDecimal.valueOf(75.00));
        newInvestment.setVolume(50L);
        newInvestment.setIsActive(true);
        newInvestment.setCreatedAt(LocalDateTime.now());
        newInvestment.setUpdatedAt(LocalDateTime.now());

        // When
        Investment saved = investmentRepository.save(newInvestment);
        entityManager.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Investment");
        assertThat(saved.getTicker()).isEqualTo("NEW4");
        assertThat(saved.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void shouldUpdateInvestment() {
        // Given
        testInvestment.setName("Updated Investment");
        testInvestment.setCurrentPrice(BigDecimal.valueOf(120.00));

        // When
        Investment updated = investmentRepository.save(testInvestment);
        entityManager.flush();

        // Then
        assertThat(updated.getName()).isEqualTo("Updated Investment");
        assertThat(updated.getCurrentPrice()).isEqualTo(BigDecimal.valueOf(120.00));
        assertThat(updated.getId()).isEqualTo(testInvestment.getId());
    }

    @Test
    void shouldDeleteInvestment() {
        // Given
        Long investmentId = testInvestment.getId();

        // When
        investmentRepository.delete(testInvestment);
        entityManager.flush();

        // Then
        Optional<Investment> deleted = investmentRepository.findById(investmentId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void shouldFindInvestmentsWithPagination() {
        // Given - Create multiple investments
        for (int i = 1; i <= 5; i++) {
            Investment investment = new Investment();
            investment.setUser(testUser);
            investment.setName("Investment " + i);
            investment.setTicker("INV" + i + "4");
            investment.setInvestmentType(Investment.InvestmentType.STOCK);
            investment.setCurrentPrice(BigDecimal.valueOf(100.00 * i));
            investment.setVolume(100L * i);
            investment.setIsActive(true);
            investment.setCreatedAt(LocalDateTime.now());
            investment.setUpdatedAt(LocalDateTime.now());
            entityManager.persistAndFlush(investment);
        }

        // When
        Pageable pageable = PageRequest.of(0, 3);
        Page<Investment> page = investmentRepository.findByUser_IdAndIsActiveTrue(testUser.getId(), pageable);

        // Then
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(6); // 5 new + 1 original
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(3);
    }
}
