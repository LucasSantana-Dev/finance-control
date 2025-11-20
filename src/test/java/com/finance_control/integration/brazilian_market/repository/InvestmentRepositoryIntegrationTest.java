package com.finance_control.integration.brazilian_market.repository;

import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.model.InvestmentType;
import com.finance_control.brazilian_market.model.InvestmentSubtype;
import com.finance_control.brazilian_market.repository.InvestmentRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for InvestmentRepository.
 * Tests the database operations and queries for the Investment entity.
 */
@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.supabase.enabled=false"
})
class InvestmentRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InvestmentRepository investmentRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Investment testInvestment;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        // Create test investment
        testInvestment = new Investment();
        testInvestment.setTicker("PETR4");
        testInvestment.setName("Petrobras");
        testInvestment.setInvestmentType(InvestmentType.STOCK);
        testInvestment.setInvestmentSubtype(InvestmentSubtype.ORDINARY);
        testInvestment.setCurrentPrice(BigDecimal.valueOf(26.00));
        testInvestment.setIsActive(true);
        testInvestment.setUser(testUser);
        testInvestment.setCreatedAt(LocalDateTime.now());
        testInvestment.setUpdatedAt(LocalDateTime.now());
        testInvestment = investmentRepository.save(testInvestment);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByUser_IdAndIsActiveTrue_ShouldReturnActiveInvestmentsForUser() {
        // When
        List<Investment> result = investmentRepository.findByUser_IdAndIsActiveTrue(testUser.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("PETR4");
        assertThat(result.get(0).getUser().getId()).isEqualTo(testUser.getId());
        assertThat(result.get(0).getIsActive()).isTrue();
    }

    @Test
    void findByUser_IdAndIsActiveTrue_WithPagination_ShouldReturnPaginatedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Investment> result = investmentRepository.findByUser_IdAndIsActiveTrue(testUser.getId(), pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTicker()).isEqualTo("PETR4");
    }

    @Test
    void findByTickerAndUser_IdAndIsActiveTrue_ShouldReturnInvestmentWhenFound() {
        // When
        Optional<Investment> result = investmentRepository.findByTickerAndUser_IdAndIsActiveTrue("PETR4", testUser.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTicker()).isEqualTo("PETR4");
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findByTickerAndUser_IdAndIsActiveTrue_ShouldReturnEmptyWhenNotFound() {
        // When
        Optional<Investment> result = investmentRepository.findByTickerAndUser_IdAndIsActiveTrue("INVALID", testUser.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUser_IdAndInvestmentTypeAndIsActiveTrue_ShouldReturnInvestmentsOfSpecificType() {
        // When
        List<Investment> result = investmentRepository.findByUser_IdAndInvestmentTypeAndIsActiveTrue(
                testUser.getId(), InvestmentType.STOCK);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInvestmentType()).isEqualTo(InvestmentType.STOCK);
    }

    @Test
    void findByUser_IdAndInvestmentTypeAndInvestmentSubtypeAndIsActiveTrue_ShouldReturnInvestmentsOfSpecificTypeAndSubtype() {
        // When
        List<Investment> result = investmentRepository.findByUser_IdAndInvestmentTypeAndInvestmentSubtypeAndIsActiveTrue(
                testUser.getId(), InvestmentType.STOCK, InvestmentSubtype.ORDINARY);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInvestmentType()).isEqualTo(InvestmentType.STOCK);
        assertThat(result.get(0).getInvestmentSubtype()).isEqualTo(InvestmentSubtype.ORDINARY);
    }

    @Test
    void existsByTickerAndUser_IdAndIsActiveTrue_ShouldReturnTrueWhenInvestmentExists() {
        // When
        boolean result = investmentRepository.existsByTickerAndUser_IdAndIsActiveTrue("PETR4", testUser.getId());

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void existsByTickerAndUser_IdAndIsActiveTrue_ShouldReturnFalseWhenInvestmentDoesNotExist() {
        // When
        boolean result = investmentRepository.existsByTickerAndUser_IdAndIsActiveTrue("INVALID", testUser.getId());

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void findByUser_IdAndIsActiveTrue_ShouldNotReturnInactiveInvestments() {
        // Given
        testInvestment.setIsActive(false);
        investmentRepository.save(testInvestment);
        entityManager.flush();
        entityManager.clear();

        // When
        List<Investment> result = investmentRepository.findByUser_IdAndIsActiveTrue(testUser.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUser_IdAndIsActiveTrue_ShouldNotReturnInvestmentsFromOtherUsers() {
        // Given
        User otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password");
        otherUser.setIsActive(true);
        otherUser = userRepository.save(otherUser);

        // When
        List<Investment> result = investmentRepository.findByUser_IdAndIsActiveTrue(otherUser.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUser_IdAndInvestmentTypeAndIsActiveTrue_ShouldReturnEmptyForNonExistentType() {
        // When
        List<Investment> result = investmentRepository.findByUser_IdAndInvestmentTypeAndIsActiveTrue(
                testUser.getId(), InvestmentType.FII);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUser_IdAndInvestmentTypeAndInvestmentSubtypeAndIsActiveTrue_ShouldReturnEmptyForNonExistentSubtype() {
        // When
        List<Investment> result = investmentRepository.findByUser_IdAndInvestmentTypeAndInvestmentSubtypeAndIsActiveTrue(
                testUser.getId(), InvestmentType.STOCK, InvestmentSubtype.PREFERRED);

        // Then
        assertThat(result).isEmpty();
    }
}
