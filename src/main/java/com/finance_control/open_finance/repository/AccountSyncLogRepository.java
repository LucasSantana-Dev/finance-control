package com.finance_control.open_finance.repository;

import com.finance_control.open_finance.model.AccountSyncLog;
import com.finance_control.open_finance.model.ConnectedAccount;
import com.finance_control.shared.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for account sync logs.
 */
@Repository
public interface AccountSyncLogRepository extends BaseRepository<AccountSyncLog, Long> {

    List<AccountSyncLog> findByAccount(ConnectedAccount account);

    List<AccountSyncLog> findByAccountId(Long accountId);

    List<AccountSyncLog> findByAccountIdOrderBySyncedAtDesc(Long accountId);

    List<AccountSyncLog> findByStatus(String status);

    List<AccountSyncLog> findBySyncType(String syncType);

    @Query("SELECT l FROM AccountSyncLog l WHERE l.account.user.id = :userId " +
           "ORDER BY l.syncedAt DESC")
    List<AccountSyncLog> findByUserId(@Param("userId") Long userId);

    @Query("SELECT l FROM AccountSyncLog l WHERE l.account.id = :accountId " +
           "AND l.syncedAt >= :since " +
           "ORDER BY l.syncedAt DESC")
    List<AccountSyncLog> findByAccountIdSince(@Param("accountId") Long accountId,
                                              @Param("since") LocalDateTime since);

    @Query("SELECT l FROM AccountSyncLog l WHERE l.account.user.id = :userId " +
           "AND l.status = 'FAILED' " +
           "AND l.syncedAt >= :since")
    List<AccountSyncLog> findFailedSyncsByUserSince(@Param("userId") Long userId,
                                                     @Param("since") LocalDateTime since);

    @Override
    @Query("SELECT l FROM AccountSyncLog l WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(l.status) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.syncType) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.errorMessage) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<AccountSyncLog> findAll(@Param("search") String search, Pageable pageable);
}
