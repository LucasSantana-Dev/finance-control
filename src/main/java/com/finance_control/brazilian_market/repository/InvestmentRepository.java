package com.finance_control.brazilian_market.repository;

import com.finance_control.brazilian_market.model.Investment;
import com.finance_control.shared.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Investment entity operations.
 * Provides methods for querying investments by various criteria.
 */
@Repository
public interface InvestmentRepository extends BaseRepository<Investment, Long> {

    /**
     * Find all investments for a specific user (both active and inactive)
     */
    List<Investment> findByUser_Id(Long userId);

    /**
     * Find all active investments for a specific user
     */
    List<Investment> findByUser_IdAndIsActiveTrue(Long userId);

    /**
     * Find all investments for a specific user with pagination
     */
    Page<Investment> findByUser_IdAndIsActiveTrue(Long userId, Pageable pageable);

    /**
     * Find investment by ticker and user
     */
    Optional<Investment> findByTickerAndUser_IdAndIsActiveTrue(String ticker, Long userId);

    /**
     * Find investments by type for a specific user
     */
    List<Investment> findByUser_IdAndInvestmentTypeAndIsActiveTrue(Long userId, Investment.InvestmentType investmentType);

    /**
     * Find investments by type and subtype for a specific user
     */
    List<Investment> findByUser_IdAndInvestmentTypeAndInvestmentSubtypeAndIsActiveTrue(
            Long userId,
            Investment.InvestmentType investmentType,
            Investment.InvestmentSubtype investmentSubtype
    );

    /**
     * Find investments by sector for a specific user
     */
    List<Investment> findByUser_IdAndSectorAndIsActiveTrue(Long userId, String sector);

    /**
     * Find investments by industry for a specific user
     */
    List<Investment> findByUser_IdAndIndustryAndIsActiveTrue(Long userId, String industry);

    /**
     * Find investments that need price updates (last_updated is null or older than specified time)
     */
    @Query("SELECT i FROM Investment i WHERE i.user.id = :userId AND i.isActive = true AND " +
           "(i.lastUpdated IS NULL OR i.lastUpdated < :cutoffTime)")
    List<Investment> findInvestmentsNeedingPriceUpdate(@Param("userId") Long userId, @Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find all unique sectors for a specific user
     */
    @Query("SELECT DISTINCT i.sector FROM Investment i WHERE i.user.id = :userId AND i.isActive = true AND i.sector IS NOT NULL ORDER BY i.sector")
    List<String> findDistinctSectorsByUserId(@Param("userId") Long userId);

    /**
     * Find all unique industries for a specific user
     */
    @Query("SELECT DISTINCT i.industry FROM Investment i WHERE i.user.id = :userId AND i.isActive = true AND i.industry IS NOT NULL ORDER BY i.industry")
    List<String> findDistinctIndustriesByUserId(@Param("userId") Long userId);

    /**
     * Find all unique investment types for a specific user
     */
    @Query("SELECT DISTINCT i.investmentType FROM Investment i WHERE i.user.id = :userId AND i.isActive = true ORDER BY i.investmentType")
    List<Investment.InvestmentType> findDistinctInvestmentTypesByUserId(@Param("userId") Long userId);

    /**
     * Find all unique investment subtypes for a specific user and investment type
     */
    @Query("SELECT DISTINCT i.investmentSubtype FROM Investment i WHERE i.user.id = :userId " +
            "AND i.investmentType = :investmentType AND i.isActive = true AND i.investmentSubtype IS NOT NULL " +
            "ORDER BY i.investmentSubtype")
    List<Investment.InvestmentSubtype> findDistinctInvestmentSubtypesByUserIdAndType(
            @Param("userId") Long userId,
            @Param("investmentType") Investment.InvestmentType investmentType
    );

    /**
     * Count investments by type for a specific user
     */
    @Query("SELECT i.investmentType, COUNT(i) FROM Investment i WHERE i.user.id = :userId AND i.isActive = true GROUP BY i.investmentType")
    List<Object[]> countInvestmentsByType(@Param("userId") Long userId);

    /**
     * Find investments with highest dividend yield for a specific user
     */
    @Query("SELECT i FROM Investment i WHERE i.user.id = :userId AND i.isActive = true AND i.dividendYield IS NOT NULL ORDER BY i.dividendYield DESC")
    List<Investment> findTopInvestmentsByDividendYield(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find investments with highest day change for a specific user
     */
    @Query("SELECT i FROM Investment i WHERE i.user.id = :userId AND i.isActive = true AND i.dayChangePercent IS NOT NULL ORDER BY i.dayChangePercent DESC")
    List<Investment> findTopInvestmentsByDayChange(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find investments with lowest day change for a specific user
     */
    @Query("SELECT i FROM Investment i WHERE i.user.id = :userId AND i.isActive = true AND i.dayChangePercent IS NOT NULL ORDER BY i.dayChangePercent ASC")
    List<Investment> findBottomInvestmentsByDayChange(@Param("userId") Long userId, Pageable pageable);

    /**
     * Search investments by name or ticker for a specific user
     */
    @Query("SELECT i FROM Investment i WHERE i.user.id = :userId AND i.isActive = true AND " +
           "(LOWER(i.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.ticker) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Investment> searchInvestments(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);

    /**
     * Find investments by exchange for a specific user
     */
    List<Investment> findByUser_IdAndExchangeAndIsActiveTrue(Long userId, String exchange);

    /**
     * Check if investment exists by ticker and user
     */
    boolean existsByTickerAndUser_IdAndIsActiveTrue(String ticker, Long userId);

    /**
     * Find investments that haven't been updated recently
     */
    @Query("SELECT i FROM Investment i WHERE i.user.id = :userId AND i.isActive = true AND " +
           "(i.lastUpdated IS NULL OR i.lastUpdated < :cutoffTime) ORDER BY i.lastUpdated ASC NULLS FIRST")
    List<Investment> findStaleInvestments(@Param("userId") Long userId, @Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Get total market value for a specific user
     */
    @Query("SELECT SUM(i.currentPrice * i.volume) FROM Investment i WHERE i.user.id = :userId " +
            "AND i.isActive = true AND i.currentPrice IS NOT NULL AND i.volume IS NOT NULL")
    Optional<Double> getTotalMarketValue(@Param("userId") Long userId);

    /**
     * Get total market value by investment type for a specific user
     */
    @Query("SELECT i.investmentType, SUM(i.currentPrice * i.volume) FROM Investment i WHERE i.user.id = :userId " +
            "AND i.isActive = true AND i.currentPrice IS NOT NULL AND i.volume IS NOT NULL GROUP BY i.investmentType")
    List<Object[]> getTotalMarketValueByType(@Param("userId") Long userId);
}
