package com.finance_control.brazilian_market.repository;

import com.finance_control.brazilian_market.model.MarketIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

/**
 * Repository for market indicators and economic data.
 */
@Repository
public interface MarketIndicatorRepository extends JpaRepository<MarketIndicator, Long> {

    /**
     * Finds an indicator by code.
     */
    Optional<MarketIndicator> findByCode(String code);

    /**
     * Finds indicators by type.
     */
    List<MarketIndicator> findByIndicatorType(MarketIndicator.IndicatorType indicatorType);

    /**
     * Finds indicators by frequency.
     */
    List<MarketIndicator> findByFrequency(MarketIndicator.Frequency frequency);

    /**
     * Finds active indicators.
     */
    List<MarketIndicator> findByIsActiveTrue();

    /**
     * Finds indicators by type and frequency.
     */
    List<MarketIndicator> findByIndicatorTypeAndFrequency(MarketIndicator.IndicatorType indicatorType, 
                                                         MarketIndicator.Frequency frequency);

    /**
     * Searches indicators by name or code.
     */
    @Query("SELECT m FROM MarketIndicator m WHERE " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<MarketIndicator> searchByNameOrCode(@Param("search") String search);

    /**
     * Finds key economic indicators.
     */
    @Query("SELECT m FROM MarketIndicator m WHERE m.isActive = true AND " +
           "(m.code IN ('SELIC', 'CDI', 'IPCA', 'IGP-M') OR " +
           "m.indicatorType IN ('INTEREST_RATE', 'INFLATION'))")
    List<MarketIndicator> findKeyIndicators();

    /**
     * Finds indicators with recent updates.
     */
    @Query("SELECT m FROM MarketIndicator m WHERE m.lastUpdated >= :sinceDate")
    List<MarketIndicator> findByLastUpdatedAfter(@Param("sinceDate") java.time.LocalDateTime sinceDate);

    /**
     * Finds indicators by reference date range.
     */
    @Query("SELECT m FROM MarketIndicator m WHERE m.referenceDate BETWEEN :startDate AND :endDate")
    List<MarketIndicator> findByReferenceDateBetween(@Param("startDate") java.time.LocalDate startDate, 
                                                    @Param("endDate") java.time.LocalDate endDate);

    /**
     * Counts indicators by type.
     */
    long countByIndicatorType(MarketIndicator.IndicatorType indicatorType);

    /**
     * Counts indicators by frequency.
     */
    long countByFrequency(MarketIndicator.Frequency frequency);

    /**
     * Checks if an indicator exists by code.
     */
    boolean existsByCode(String code);

    /**
     * Finds indicators with significant changes.
     */
    @Query("SELECT m FROM MarketIndicator m WHERE m.isActive = true AND " +
           "ABS(m.changePercent) > :threshold")
    List<MarketIndicator> findBySignificantChange(@Param("threshold") java.math.BigDecimal threshold);

    /**
     * Finds the most recently updated indicators.
     */
    @Query("SELECT m FROM MarketIndicator m WHERE m.isActive = true " +
           "ORDER BY m.lastUpdated DESC")
    List<MarketIndicator> findMostRecentlyUpdated(Pageable pageable);
}
