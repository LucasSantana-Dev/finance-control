package com.finance_control.brazilian_market.repository;

import com.finance_control.brazilian_market.model.BrazilianStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Brazilian stock data.
 */
@Repository
public interface BrazilianStockRepository extends JpaRepository<BrazilianStock, Long> {

    /**
     * Finds a stock by ticker and user ID.
     */
    Optional<BrazilianStock> findByTickerAndUserId(String ticker, Long userId);

    /**
     * Finds all stocks by user ID.
     */
    List<BrazilianStock> findByUserId(Long userId);

    /**
     * Finds all stocks by user ID with pagination.
     */
    Page<BrazilianStock> findByUserId(Long userId, Pageable pageable);

    /**
     * Finds stocks by stock type and user ID.
     */
    List<BrazilianStock> findByStockTypeAndUserId(BrazilianStock.StockType stockType, Long userId);

    /**
     * Finds stocks by market segment and user ID.
     */
    List<BrazilianStock> findBySegmentAndUserId(BrazilianStock.MarketSegment segment, Long userId);

    /**
     * Finds active stocks by user ID.
     */
    List<BrazilianStock> findByIsActiveTrueAndUserId(Long userId);

    /**
     * Searches stocks by company name or ticker for a specific user.
     */
    @Query("SELECT s FROM BrazilianStock s WHERE s.user.id = :userId AND " +
           "(LOWER(s.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.ticker) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<BrazilianStock> searchByUserAndQuery(@Param("userId") Long userId, @Param("search") String search);

    /**
     * Searches stocks by company name or ticker for a specific user with pagination.
     */
    @Query("SELECT s FROM BrazilianStock s WHERE s.user.id = :userId AND " +
           "(LOWER(s.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.ticker) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<BrazilianStock> searchByUserAndQuery(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);

    /**
     * Finds stocks with price changes above a threshold.
     */
    @Query("SELECT s FROM BrazilianStock s WHERE s.user.id = :userId AND " +
           "s.dayChangePercent > :threshold")
    List<BrazilianStock> findByUserAndPriceChangeAbove(@Param("userId") Long userId, @Param("threshold") java.math.BigDecimal threshold);

    /**
     * Finds stocks with price changes below a threshold.
     */
    @Query("SELECT s FROM BrazilianStock s WHERE s.user.id = :userId AND " +
           "s.dayChangePercent < :threshold")
    List<BrazilianStock> findByUserAndPriceChangeBelow(@Param("userId") Long userId, @Param("threshold") java.math.BigDecimal threshold);

    /**
     * Finds the most traded stocks by volume for a user.
     */
    @Query("SELECT s FROM BrazilianStock s WHERE s.user.id = :userId " +
           "ORDER BY s.volume DESC")
    List<BrazilianStock> findTopByUserOrderByVolumeDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Finds stocks by market cap range for a user.
     */
    @Query("SELECT s FROM BrazilianStock s WHERE s.user.id = :userId AND " +
           "s.marketCap BETWEEN :minCap AND :maxCap")
    List<BrazilianStock> findByUserAndMarketCapBetween(@Param("userId") Long userId,
                                                      @Param("minCap") java.math.BigDecimal minCap,
                                                      @Param("maxCap") java.math.BigDecimal maxCap);

    /**
     * Counts stocks by stock type for a user.
     */
    long countByStockTypeAndUserId(BrazilianStock.StockType stockType, Long userId);

    /**
     * Counts stocks by market segment for a user.
     */
    long countBySegmentAndUserId(BrazilianStock.MarketSegment segment, Long userId);

    /**
     * Checks if a stock exists by ticker and user ID.
     */
    boolean existsByTickerAndUserId(String ticker, Long userId);
}
