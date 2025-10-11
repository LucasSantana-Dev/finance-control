package com.finance_control.brazilian_market.service;

import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.brazilian_market.repository.InvestmentRepository;
import com.finance_control.users.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing investments.
 * Handles CRUD operations and market data updates for the unified investments table.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final ExternalMarketDataService externalMarketDataService;

    /**
     * Create a new investment
     */
    public Investment createInvestment(Investment investment, User user) {
        log.debug("Creating investment: {} for user: {}", investment.getTicker(), user.getId());

        // Set user
        investment.setUser(user);

        // Set timestamps
        investment.setCreatedAt(LocalDateTime.now());
        investment.setUpdatedAt(LocalDateTime.now());

        // Fetch initial market data
        updateMarketData(investment);

        Investment savedInvestment = investmentRepository.save(investment);
        log.info("Created investment: {} for user: {}", savedInvestment.getTicker(), user.getId());

        return savedInvestment;
    }

    /**
     * Update an existing investment
     */
    public Investment updateInvestment(Long investmentId, Investment updatedInvestment, User user) {
        log.debug("Updating investment: {} for user: {}", investmentId, user.getId());

        Investment existingInvestment = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new IllegalArgumentException("Investment not found: " + investmentId));

        // Verify ownership
        if (!existingInvestment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Investment does not belong to user: " + user.getId());
        }

        // Update fields
        existingInvestment.setName(updatedInvestment.getName());
        existingInvestment.setDescription(updatedInvestment.getDescription());
        existingInvestment.setInvestmentType(updatedInvestment.getInvestmentType());
        existingInvestment.setInvestmentSubtype(updatedInvestment.getInvestmentSubtype());
        existingInvestment.setMarketSegment(updatedInvestment.getMarketSegment());
        existingInvestment.setSector(updatedInvestment.getSector());
        existingInvestment.setIndustry(updatedInvestment.getIndustry());
        existingInvestment.setExchange(updatedInvestment.getExchange());
        existingInvestment.setCurrency(updatedInvestment.getCurrency());
        existingInvestment.setUpdatedAt(LocalDateTime.now());

        Investment savedInvestment = investmentRepository.save(existingInvestment);
        log.info("Updated investment: {} for user: {}", savedInvestment.getTicker(), user.getId());

        return savedInvestment;
    }

    /**
     * Get investment by ID
     */
    @Transactional(readOnly = true)
    public Optional<Investment> getInvestmentById(Long investmentId, User user) {
        return investmentRepository.findById(investmentId)
                .filter(investment -> investment.getUser().getId().equals(user.getId()) && investment.getIsActive());
    }

    /**
     * Get investment by ticker
     */
    @Transactional(readOnly = true)
    public Optional<Investment> getInvestmentByTicker(String ticker, User user) {
        return investmentRepository.findByTickerAndUserIdAndIsActiveTrue(ticker, user.getId());
    }

    /**
     * Get all investments for a user
     */
    @Transactional(readOnly = true)
    public List<Investment> getAllInvestments(User user) {
        return investmentRepository.findByUserIdAndIsActiveTrue(user.getId());
    }

    /**
     * Get all investments for a user with pagination
     */
    @Transactional(readOnly = true)
    public Page<Investment> getAllInvestments(User user, Pageable pageable) {
        return investmentRepository.findByUserIdAndIsActiveTrue(user.getId(), pageable);
    }

    /**
     * Get investments by type
     */
    @Transactional(readOnly = true)
    public List<Investment> getInvestmentsByType(User user, Investment.InvestmentType investmentType) {
        return investmentRepository.findByUserIdAndInvestmentTypeAndIsActiveTrue(user.getId(), investmentType);
    }

    /**
     * Get investments by type and subtype
     */
    @Transactional(readOnly = true)
    public List<Investment> getInvestmentsByTypeAndSubtype(User user, Investment.InvestmentType investmentType, Investment.InvestmentSubtype investmentSubtype) {
        return investmentRepository.findByUserIdAndInvestmentTypeAndInvestmentSubtypeAndIsActiveTrue(user.getId(), investmentType, investmentSubtype);
    }

    /**
     * Get investments by sector
     */
    @Transactional(readOnly = true)
    public List<Investment> getInvestmentsBySector(User user, String sector) {
        return investmentRepository.findByUserIdAndSectorAndIsActiveTrue(user.getId(), sector);
    }

    /**
     * Get investments by industry
     */
    @Transactional(readOnly = true)
    public List<Investment> getInvestmentsByIndustry(User user, String industry) {
        return investmentRepository.findByUserIdAndIndustryAndIsActiveTrue(user.getId(), industry);
    }

    /**
     * Search investments
     */
    @Transactional(readOnly = true)
    public List<Investment> searchInvestments(User user, String searchTerm) {
        return investmentRepository.searchInvestments(user.getId(), searchTerm);
    }

    /**
     * Get all unique sectors for a user
     */
    @Transactional(readOnly = true)
    public List<String> getSectors(User user) {
        return investmentRepository.findDistinctSectorsByUserId(user.getId());
    }

    /**
     * Get all unique industries for a user
     */
    @Transactional(readOnly = true)
    public List<String> getIndustries(User user) {
        return investmentRepository.findDistinctIndustriesByUserId(user.getId());
    }

    /**
     * Get all unique investment types for a user
     */
    @Transactional(readOnly = true)
    public List<Investment.InvestmentType> getInvestmentTypes(User user) {
        return investmentRepository.findDistinctInvestmentTypesByUserId(user.getId());
    }

    /**
     * Get all unique investment subtypes for a user and type
     */
    @Transactional(readOnly = true)
    public List<Investment.InvestmentSubtype> getInvestmentSubtypes(User user, Investment.InvestmentType investmentType) {
        return investmentRepository.findDistinctInvestmentSubtypesByUserIdAndType(user.getId(), investmentType);
    }

    /**
     * Update market data for an investment
     */
    public Investment updateMarketData(Investment investment) {
        log.debug("Updating market data for investment: {}", investment.getTicker());

        if (externalMarketDataService.needsUpdate(investment.getLastUpdated())) {
            externalMarketDataService.fetchMarketData(investment.getTicker(), investment.getInvestmentType())
                    .ifPresent(marketData -> {
                        investment.setCurrentPrice(marketData.getCurrentPrice());
                        investment.setPreviousClose(marketData.getPreviousClose());
                        investment.setDayChange(marketData.getDayChange());
                        investment.setDayChangePercent(marketData.getDayChangePercent());
                        investment.setVolume(marketData.getVolume());
                        investment.setLastUpdated(marketData.getLastUpdated());
                        investment.setUpdatedAt(LocalDateTime.now());

                        log.debug("Updated market data for investment: {}", investment.getTicker());
                    });
        }

        return investmentRepository.save(investment);
    }

    /**
     * Update market data for all investments of a user
     */
    public void updateAllMarketData(User user) {
        log.debug("Updating market data for all investments of user: {}", user.getId());

        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(15);
        List<Investment> investmentsToUpdate = investmentRepository.findInvestmentsNeedingPriceUpdate(user.getId(), cutoffTime);

        log.info("Found {} investments needing market data update for user: {}", investmentsToUpdate.size(), user.getId());

        for (Investment investment : investmentsToUpdate) {
            try {
                updateMarketData(investment);
                // Add small delay to avoid rate limiting
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Market data update interrupted for user: {}", user.getId());
                break;
            } catch (Exception e) {
                log.error("Error updating market data for investment: {}", investment.getTicker(), e);
            }
        }

        log.info("Completed market data update for user: {}", user.getId());
    }

    /**
     * Delete an investment (soft delete)
     */
    public void deleteInvestment(Long investmentId, User user) {
        log.debug("Deleting investment: {} for user: {}", investmentId, user.getId());

        Investment investment = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new IllegalArgumentException("Investment not found: " + investmentId));

        // Verify ownership
        if (!investment.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Investment does not belong to user: " + user.getId());
        }

        // Soft delete
        investment.setIsActive(false);
        investment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment);

        log.info("Deleted investment: {} for user: {}", investment.getTicker(), user.getId());
    }

    /**
     * Check if investment exists
     */
    @Transactional(readOnly = true)
    public boolean investmentExists(String ticker, User user) {
        return investmentRepository.existsByTickerAndUserIdAndIsActiveTrue(ticker, user.getId());
    }

    /**
     * Get top performing investments by day change
     */
    @Transactional(readOnly = true)
    public List<Investment> getTopPerformers(User user, Pageable pageable) {
        return investmentRepository.findTopInvestmentsByDayChange(user.getId(), pageable);
    }

    /**
     * Get worst performing investments by day change
     */
    @Transactional(readOnly = true)
    public List<Investment> getWorstPerformers(User user, Pageable pageable) {
        return investmentRepository.findBottomInvestmentsByDayChange(user.getId(), pageable);
    }

    /**
     * Get investments with highest dividend yield
     */
    @Transactional(readOnly = true)
    public List<Investment> getTopDividendYield(User user, Pageable pageable) {
        return investmentRepository.findTopInvestmentsByDividendYield(user.getId(), pageable);
    }

    /**
     * Get total market value for a user
     */
    @Transactional(readOnly = true)
    public Optional<Double> getTotalMarketValue(User user) {
        return investmentRepository.getTotalMarketValue(user.getId());
    }

    /**
     * Get market value by investment type for a user
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMarketValueByType(User user) {
        return investmentRepository.getTotalMarketValueByType(user.getId());
    }
}
