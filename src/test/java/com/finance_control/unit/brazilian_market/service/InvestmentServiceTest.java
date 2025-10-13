package com.finance_control.unit.brazilian_market.service;

import com.finance_control.brazilian_market.client.MarketQuote;
import com.finance_control.brazilian_market.dto.InvestmentDTO;
import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.repository.InvestmentRepository;
import com.finance_control.brazilian_market.service.ExternalMarketDataService;
import com.finance_control.brazilian_market.service.InvestmentService;
import com.finance_control.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InvestmentService.
 * Tests the business logic for investment management operations.
 */
@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private ExternalMarketDataService externalMarketDataService;

    @InjectMocks
    private InvestmentService investmentService;

    private User testUser;
    private Investment testInvestment;
    private InvestmentDTO testInvestmentDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setIsActive(true);

        testInvestment = new Investment();
        testInvestment.setId(1L);
        testInvestment.setTicker("PETR4");
        testInvestment.setName("Petrobras");
        testInvestment.setInvestmentType(Investment.InvestmentType.STOCK);
        testInvestment.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        testInvestment.setCurrentPrice(BigDecimal.valueOf(26.00));
        testInvestment.setIsActive(true);
        testInvestment.setCreatedAt(LocalDateTime.now());
        testInvestment.setUpdatedAt(LocalDateTime.now());
        testInvestment.setUser(testUser);

        testInvestmentDTO = new InvestmentDTO();
        testInvestmentDTO.setTicker("PETR4");
        testInvestmentDTO.setName("Petrobras");
        testInvestmentDTO.setInvestmentType(Investment.InvestmentType.STOCK);
        testInvestmentDTO.setInvestmentSubtype(Investment.InvestmentSubtype.ORDINARY);
        testInvestmentDTO.setCurrentPrice(BigDecimal.valueOf(26.00));
        testInvestmentDTO.setIsActive(true);
    }

    @Test
    void createInvestment_ShouldCreateInvestmentSuccessfully() {
        // Given
        when(investmentRepository.save(any(Investment.class))).thenReturn(testInvestment);
        when(externalMarketDataService.needsUpdate(any())).thenReturn(true);
        when(externalMarketDataService.fetchMarketData(anyString(), any()))
                .thenReturn(Optional.of(createMarketQuote()));

        // When
        Investment result = investmentService.createInvestment(testInvestmentDTO, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTicker()).isEqualTo("PETR4");
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        verify(investmentRepository, times(2)).save(any(Investment.class));
        verify(externalMarketDataService).fetchMarketData("PETR4", Investment.InvestmentType.STOCK);
    }

    @Test
    void updateInvestment_ShouldUpdateInvestmentSuccessfully() {
        // Given
        InvestmentDTO updatedInvestmentDTO = new InvestmentDTO();
        updatedInvestmentDTO.setName("Petrobras Updated");
        updatedInvestmentDTO.setDescription("Updated description");

        when(investmentRepository.findById(1L)).thenReturn(Optional.of(testInvestment));
        when(investmentRepository.save(any(Investment.class))).thenReturn(testInvestment);

        // When
        Investment result = investmentService.updateInvestment(1L, updatedInvestmentDTO, testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Petrobras Updated");
        assertThat(result.getDescription()).isEqualTo("Updated description");

        verify(investmentRepository).findById(1L);
        verify(investmentRepository).save(testInvestment);
    }

    @Test
    void updateInvestment_ShouldThrowExceptionWhenInvestmentNotFound() {
        // Given
        InvestmentDTO updatedInvestmentDTO = new InvestmentDTO();
        when(investmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> investmentService.updateInvestment(999L, updatedInvestmentDTO, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Investment not found: 999");

        verify(investmentRepository).findById(999L);
        verify(investmentRepository, never()).save(any());
    }

    @Test
    void updateInvestment_ShouldThrowExceptionWhenUserNotOwner() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testInvestment.setUser(otherUser);

        InvestmentDTO updatedInvestmentDTO = new InvestmentDTO();
        when(investmentRepository.findById(1L)).thenReturn(Optional.of(testInvestment));

        // When & Then
        assertThatThrownBy(() -> investmentService.updateInvestment(1L, updatedInvestmentDTO, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Investment does not belong to user: 1");

        verify(investmentRepository).findById(1L);
        verify(investmentRepository, never()).save(any());
    }

    @Test
    void getInvestmentById_ShouldReturnInvestmentWhenFound() {
        // Given
        when(investmentRepository.findById(1L)).thenReturn(Optional.of(testInvestment));

        // When
        Optional<Investment> result = investmentService.getInvestmentById(1L, testUser);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTicker()).isEqualTo("PETR4");
        assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());

        verify(investmentRepository).findById(1L);
    }

    @Test
    void getInvestmentById_ShouldReturnEmptyWhenInvestmentNotFound() {
        // Given
        when(investmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Investment> result = investmentService.getInvestmentById(999L, testUser);

        // Then
        assertThat(result).isEmpty();

        verify(investmentRepository).findById(999L);
    }

    @Test
    void getInvestmentById_ShouldReturnEmptyWhenUserNotOwner() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testInvestment.setUser(otherUser);

        when(investmentRepository.findById(1L)).thenReturn(Optional.of(testInvestment));

        // When
        Optional<Investment> result = investmentService.getInvestmentById(1L, testUser);

        // Then
        assertThat(result).isEmpty();

        verify(investmentRepository).findById(1L);
    }

    @Test
    void getInvestmentByTicker_ShouldReturnInvestmentWhenFound() {
        // Given
        when(investmentRepository.findByTickerAndUser_IdAndIsActiveTrue("PETR4", 1L))
                .thenReturn(Optional.of(testInvestment));

        // When
        Optional<Investment> result = investmentService.getInvestmentByTicker("PETR4", testUser);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTicker()).isEqualTo("PETR4");

        verify(investmentRepository).findByTickerAndUser_IdAndIsActiveTrue("PETR4", 1L);
    }

    @Test
    void getAllInvestments_ShouldReturnAllActiveInvestments() {
        // Given
        when(investmentRepository.findByUser_IdAndIsActiveTrue(1L))
                .thenReturn(List.of(testInvestment));

        // When
        List<Investment> result = investmentService.getAllInvestments(testUser);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTicker()).isEqualTo("PETR4");

        verify(investmentRepository).findByUser_IdAndIsActiveTrue(1L);
    }

    @Test
    void getAllInvestments_WithPagination_ShouldReturnPaginatedInvestments() {
        // Given
        Page<Investment> investmentPage = new PageImpl<>(List.of(testInvestment));
        Pageable pageable = PageRequest.of(0, 10);
        when(investmentRepository.findByUser_IdAndIsActiveTrue(1L, pageable))
                .thenReturn(investmentPage);

        // When
        Page<Investment> result = investmentService.getAllInvestments(testUser, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTicker()).isEqualTo("PETR4");

        verify(investmentRepository).findByUser_IdAndIsActiveTrue(1L, pageable);
    }

    @Test
    void getInvestmentsByType_ShouldReturnInvestmentsOfSpecificType() {
        // Given
        when(investmentRepository.findByUser_IdAndInvestmentTypeAndIsActiveTrue(
                1L, Investment.InvestmentType.STOCK))
                .thenReturn(List.of(testInvestment));

        // When
        List<Investment> result = investmentService.getInvestmentsByType(testUser, Investment.InvestmentType.STOCK);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInvestmentType()).isEqualTo(Investment.InvestmentType.STOCK);

        verify(investmentRepository).findByUser_IdAndInvestmentTypeAndIsActiveTrue(
                1L, Investment.InvestmentType.STOCK);
    }

    @Test
    void getInvestmentsByTypeAndSubtype_ShouldReturnFilteredInvestments() {
        // Given
        when(investmentRepository.findByUser_IdAndInvestmentTypeAndInvestmentSubtypeAndIsActiveTrue(
                1L, Investment.InvestmentType.STOCK, Investment.InvestmentSubtype.ORDINARY))
                .thenReturn(List.of(testInvestment));

        // When
        List<Investment> result = investmentService.getInvestmentsByTypeAndSubtype(
                testUser, Investment.InvestmentType.STOCK, Investment.InvestmentSubtype.ORDINARY);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInvestmentType()).isEqualTo(Investment.InvestmentType.STOCK);
        assertThat(result.get(0).getInvestmentSubtype()).isEqualTo(Investment.InvestmentSubtype.ORDINARY);

        verify(investmentRepository).findByUser_IdAndInvestmentTypeAndInvestmentSubtypeAndIsActiveTrue(
                1L, Investment.InvestmentType.STOCK, Investment.InvestmentSubtype.ORDINARY);
    }

    @Test
    void updateMarketData_ShouldUpdateMarketDataWhenNeeded() {
        // Given
        when(externalMarketDataService.needsUpdate(any())).thenReturn(true);
        when(externalMarketDataService.fetchMarketData(anyString(), any()))
                .thenReturn(Optional.of(createMarketQuote()));
        when(investmentRepository.save(any(Investment.class))).thenReturn(testInvestment);

        // When
        Investment result = investmentService.updateMarketData(testInvestment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCurrentPrice()).isEqualTo(BigDecimal.valueOf(26.50));

        verify(externalMarketDataService).needsUpdate(null);
        verify(externalMarketDataService).fetchMarketData("PETR4", Investment.InvestmentType.STOCK);
        verify(investmentRepository).save(testInvestment);
    }

    @Test
    void updateMarketData_ShouldNotUpdateWhenNotNeeded() {
        // Given
        when(externalMarketDataService.needsUpdate(any())).thenReturn(false);
        when(investmentRepository.save(any(Investment.class))).thenReturn(testInvestment);

        // When
        Investment result = investmentService.updateMarketData(testInvestment);

        // Then
        assertThat(result).isNotNull();

        verify(externalMarketDataService).needsUpdate(testInvestment.getLastUpdated());
        verify(externalMarketDataService, never()).fetchMarketData(anyString(), any());
        verify(investmentRepository).save(testInvestment);
    }

    @Test
    void deleteInvestment_ShouldSoftDeleteInvestment() {
        // Given
        when(investmentRepository.findById(1L)).thenReturn(Optional.of(testInvestment));
        when(investmentRepository.save(any(Investment.class))).thenReturn(testInvestment);

        // When
        investmentService.deleteInvestment(1L, testUser);

        // Then
        assertThat(testInvestment.getIsActive()).isFalse();
        assertThat(testInvestment.getUpdatedAt()).isNotNull();

        verify(investmentRepository).findById(1L);
        verify(investmentRepository).save(testInvestment);
    }

    @Test
    void deleteInvestment_ShouldThrowExceptionWhenInvestmentNotFound() {
        // Given
        when(investmentRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> investmentService.deleteInvestment(999L, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Investment not found: 999");

        verify(investmentRepository).findById(999L);
        verify(investmentRepository, never()).save(any());
    }

    @Test
    void deleteInvestment_ShouldThrowExceptionWhenUserNotOwner() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        testInvestment.setUser(otherUser);

        when(investmentRepository.findById(1L)).thenReturn(Optional.of(testInvestment));

        // When & Then
        assertThatThrownBy(() -> investmentService.deleteInvestment(1L, testUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Investment does not belong to user: 1");

        verify(investmentRepository).findById(1L);
        verify(investmentRepository, never()).save(any());
    }

    @Test
    void investmentExists_ShouldReturnTrueWhenInvestmentExists() {
        // Given
        when(investmentRepository.existsByTickerAndUser_IdAndIsActiveTrue("PETR4", 1L))
                .thenReturn(true);

        // When
        boolean result = investmentService.investmentExists("PETR4", testUser);

        // Then
        assertThat(result).isTrue();

        verify(investmentRepository).existsByTickerAndUser_IdAndIsActiveTrue("PETR4", 1L);
    }

    @Test
    void investmentExists_ShouldReturnFalseWhenInvestmentDoesNotExist() {
        // Given
        when(investmentRepository.existsByTickerAndUser_IdAndIsActiveTrue("INVALID", 1L))
                .thenReturn(false);

        // When
        boolean result = investmentService.investmentExists("INVALID", testUser);

        // Then
        assertThat(result).isFalse();

        verify(investmentRepository).existsByTickerAndUser_IdAndIsActiveTrue("INVALID", 1L);
    }

    @Test
    void updateAllMarketData_ShouldUpdateAllInvestmentsNeedingUpdate() {
        // Given
        List<Investment> investmentsToUpdate = List.of(testInvestment);
        when(investmentRepository.findInvestmentsNeedingPriceUpdate(any(), any()))
                .thenReturn(investmentsToUpdate);
        when(externalMarketDataService.needsUpdate(any())).thenReturn(true);
        when(externalMarketDataService.fetchMarketData(anyString(), any()))
                .thenReturn(Optional.of(createMarketQuote()));
        when(investmentRepository.save(any(Investment.class))).thenReturn(testInvestment);

        // When
        investmentService.updateAllMarketData(testUser);

        // Then
        verify(investmentRepository).findInvestmentsNeedingPriceUpdate(eq(1L), any());
        verify(externalMarketDataService).fetchMarketData("PETR4", Investment.InvestmentType.STOCK);
        verify(investmentRepository).save(testInvestment);
    }

    private MarketQuote createMarketQuote() {
        return MarketQuote.builder()
                .symbol("PETR4")
                .currentPrice(BigDecimal.valueOf(26.50))
                .previousClose(BigDecimal.valueOf(26.00))
                .dayChange(BigDecimal.valueOf(0.50))
                .dayChangePercent(BigDecimal.valueOf(1.92))
                .volume(1000000L)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}
