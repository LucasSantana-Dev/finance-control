package com.finance_control.open_finance.repository;

import com.finance_control.open_finance.model.ConnectedAccount;
import com.finance_control.open_finance.model.OpenFinanceConsent;
import com.finance_control.open_finance.model.OpenFinanceInstitution;
import com.finance_control.shared.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for connected accounts.
 */
@Repository
public interface ConnectedAccountRepository extends BaseRepository<ConnectedAccount, Long> {

    @Query("SELECT a FROM ConnectedAccount a WHERE a.user.id = :userId")
    List<ConnectedAccount> findByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM ConnectedAccount a WHERE a.user.id = :userId AND a.syncStatus = :syncStatus")
    List<ConnectedAccount> findByUserIdAndSyncStatus(@Param("userId") Long userId, @Param("syncStatus") String syncStatus);

    List<ConnectedAccount> findByConsent(OpenFinanceConsent consent);

    List<ConnectedAccount> findByInstitution(OpenFinanceInstitution institution);

    Optional<ConnectedAccount> findByInstitutionIdAndExternalAccountId(Long institutionId, String externalAccountId);

    @Query("SELECT a FROM ConnectedAccount a WHERE a.user.id = :userId " +
           "AND a.syncStatus != 'DISABLED'")
    List<ConnectedAccount> findSyncableAccountsByUser(@Param("userId") Long userId);

    @Query("SELECT a FROM ConnectedAccount a WHERE a.syncStatus IN ('PENDING', 'FAILED') " +
           "AND a.consent.status = 'AUTHORIZED'")
    List<ConnectedAccount> findAccountsNeedingSync();

    @Query("SELECT a FROM ConnectedAccount a WHERE a.user.id = :userId " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(a.accountNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.accountHolderName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.institution.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.syncStatus) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ConnectedAccount> findByUserId(@Param("userId") Long userId,
                                        @Param("search") String search,
                                        Pageable pageable);

    @Override
    @Query("SELECT a FROM ConnectedAccount a WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(a.accountNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.accountHolderName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.institution.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ConnectedAccount> findAll(@Param("search") String search, Pageable pageable);

    long countBySyncStatus(String syncStatus);
}
