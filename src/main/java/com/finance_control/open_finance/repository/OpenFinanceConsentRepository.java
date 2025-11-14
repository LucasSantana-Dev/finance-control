package com.finance_control.open_finance.repository;

import com.finance_control.open_finance.model.OpenFinanceConsent;
import com.finance_control.open_finance.model.OpenFinanceInstitution;
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
 * Repository for Open Finance consents.
 */
@Repository
public interface OpenFinanceConsentRepository extends BaseRepository<OpenFinanceConsent, Long> {

    @Query("SELECT c FROM OpenFinanceConsent c WHERE c.user.id = :userId")
    List<OpenFinanceConsent> findByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM OpenFinanceConsent c WHERE c.user.id = :userId AND c.status = :status")
    List<OpenFinanceConsent> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    long countByStatus(String status);

    @Query("SELECT c FROM OpenFinanceConsent c WHERE c.user.id = :userId AND c.institution.id = :institutionId")
    Optional<OpenFinanceConsent> findByUserIdAndInstitutionId(@Param("userId") Long userId, @Param("institutionId") Long institutionId);

    List<OpenFinanceConsent> findByInstitution(OpenFinanceInstitution institution);

    @Query("SELECT c FROM OpenFinanceConsent c WHERE c.status = 'AUTHORIZED' " +
           "AND (c.expiresAt IS NULL OR c.expiresAt > :now) " +
           "AND c.revokedAt IS NULL")
    List<OpenFinanceConsent> findActiveConsents(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM OpenFinanceConsent c WHERE c.user.id = :userId " +
           "AND c.status = 'AUTHORIZED' " +
           "AND (c.expiresAt IS NULL OR c.expiresAt > :now) " +
           "AND c.revokedAt IS NULL")
    List<OpenFinanceConsent> findActiveConsentsByUser(@Param("userId") Long userId,
                                                       @Param("now") LocalDateTime now);

    @Query("SELECT c FROM OpenFinanceConsent c WHERE c.expiresAt IS NOT NULL " +
           "AND c.expiresAt BETWEEN :startTime AND :endTime " +
           "AND c.status = 'AUTHORIZED'")
    List<OpenFinanceConsent> findConsentsExpiringBetween(@Param("startTime") LocalDateTime startTime,
                                                         @Param("endTime") LocalDateTime endTime);

    @Query("SELECT c FROM OpenFinanceConsent c WHERE c.user.id = :userId " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(c.institution.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.status) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<OpenFinanceConsent> findByUserId(@Param("userId") Long userId,
                                          @Param("search") String search,
                                          Pageable pageable);

    @Override
    @Query("SELECT c FROM OpenFinanceConsent c WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(c.institution.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.status) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<OpenFinanceConsent> findAll(@Param("search") String search, Pageable pageable);
}
