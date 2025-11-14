package com.finance_control.open_finance.repository;

import com.finance_control.open_finance.model.OpenFinanceInstitution;
import com.finance_control.shared.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Open Finance institutions.
 */
@Repository
public interface OpenFinanceInstitutionRepository extends BaseRepository<OpenFinanceInstitution, Long> {

    Optional<OpenFinanceInstitution> findByCode(String code);

    Optional<OpenFinanceInstitution> findByCodeIgnoreCase(String code);

    boolean existsByCode(String code);

    @Query("SELECT i FROM OpenFinanceInstitution i WHERE i.isActive = true")
    Page<OpenFinanceInstitution> findActiveInstitutions(Pageable pageable);

    @Query("SELECT i FROM OpenFinanceInstitution i WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.code) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:activeOnly IS NULL OR :activeOnly = false OR i.isActive = true)")
    Page<OpenFinanceInstitution> findAll(@Param("search") String search,
                                         @Param("activeOnly") Boolean activeOnly,
                                         Pageable pageable);

    @Override
    @Query("SELECT i FROM OpenFinanceInstitution i WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<OpenFinanceInstitution> findAll(@Param("search") String search, Pageable pageable);
}
