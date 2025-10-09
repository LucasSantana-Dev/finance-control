package com.finance_control.brazilian_market.repository;

import com.finance_control.brazilian_market.model.FII;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for FII (Real Estate Investment Fund) data.
 */
@Repository
public interface FIIRepository extends JpaRepository<FII, Long> {

    /**
     * Finds a FII by ticker and user ID.
     */
    Optional<FII> findByTickerAndUserId(String ticker, Long userId);

    /**
     * Finds all FIIs by user ID.
     */
    List<FII> findByUserId(Long userId);

    /**
     * Finds all FIIs by user ID with pagination.
     */
    Page<FII> findByUserId(Long userId, Pageable pageable);

    /**
     * Finds FIIs by FII type and user ID.
     */
    List<FII> findByFiiTypeAndUserId(FII.FIIType fiiType, Long userId);

    /**
     * Finds FIIs by segment and user ID.
     */
    List<FII> findBySegmentAndUserId(FII.FIISegment segment, Long userId);

    /**
     * Finds active FIIs by user ID.
     */
    List<FII> findByIsActiveTrueAndUserId(Long userId);

    /**
     * Searches FIIs by fund name or ticker for a specific user.
     */
    @Query("SELECT f FROM FII f WHERE f.userId = :userId AND " +
           "(LOWER(f.fundName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.ticker) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<FII> searchByUserAndQuery(@Param("userId") Long userId, @Param("search") String search);

    /**
     * Searches FIIs by fund name or ticker for a specific user with pagination.
     */
    @Query("SELECT f FROM FII f WHERE f.userId = :userId AND " +
           "(LOWER(f.fundName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.ticker) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<FII> searchByUserAndQuery(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);

    /**
     * Finds FIIs with dividend yield above a threshold.
     */
    @Query("SELECT f FROM FII f WHERE f.userId = :userId AND " +
           "f.dividendYield > :threshold")
    List<FII> findByUserAndDividendYieldAbove(@Param("userId") Long userId, @Param("threshold") java.math.BigDecimal threshold);

    /**
     * Finds FIIs with P/VP ratio below a threshold (potentially undervalued).
     */
    @Query("SELECT f FROM FII f WHERE f.userId = :userId AND " +
           "f.pvpRatio < :threshold")
    List<FII> findByUserAndPVPRatioBelow(@Param("userId") Long userId, @Param("threshold") java.math.BigDecimal threshold);

    /**
     * Finds FIIs with P/VP ratio above a threshold (potentially overvalued).
     */
    @Query("SELECT f FROM FII f WHERE f.userId = :userId AND " +
           "f.pvpRatio > :threshold")
    List<FII> findByUserAndPVPRatioAbove(@Param("userId") Long userId, @Param("threshold") java.math.BigDecimal threshold);

    /**
     * Finds the most traded FIIs by volume for a user.
     */
    @Query("SELECT f FROM FII f WHERE f.userId = :userId " +
           "ORDER BY f.volume DESC")
    List<FII> findTopByUserOrderByVolumeDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Finds FIIs by market cap range for a user.
     */
    @Query("SELECT f FROM FII f WHERE f.userId = :userId AND " +
           "f.marketCap BETWEEN :minCap AND :maxCap")
    List<FII> findByUserAndMarketCapBetween(@Param("userId") Long userId, 
                                          @Param("minCap") java.math.BigDecimal minCap, 
                                          @Param("maxCap") java.math.BigDecimal maxCap);

    /**
     * Finds FIIs with recent dividend payments.
     */
    @Query("SELECT f FROM FII f WHERE f.userId = :userId AND " +
           "f.lastDividendDate >= :sinceDate")
    List<FII> findByUserAndRecentDividends(@Param("userId") Long userId, @Param("sinceDate") java.time.LocalDate sinceDate);

    /**
     * Counts FIIs by FII type for a user.
     */
    long countByFiiTypeAndUserId(FII.FIIType fiiType, Long userId);

    /**
     * Counts FIIs by segment for a user.
     */
    long countBySegmentAndUserId(FII.FIISegment segment, Long userId);

    /**
     * Checks if a FII exists by ticker and user ID.
     */
    boolean existsByTickerAndUserId(String ticker, Long userId);

    /**
     * Finds FIIs with the highest dividend yields for a user.
     */
    @Query("SELECT f FROM FII f WHERE f.userId = :userId " +
           "ORDER BY f.dividendYield DESC")
    List<FII> findTopByUserOrderByDividendYieldDesc(@Param("userId") Long userId, Pageable pageable);
}
