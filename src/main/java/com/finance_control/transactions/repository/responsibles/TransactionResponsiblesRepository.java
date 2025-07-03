package com.finance_control.transactions.repository.responsibles;

import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionResponsiblesRepository
        extends BaseRepository<TransactionResponsibles, Long> {

    List<TransactionResponsibles> findAllByOrderByNameAsc();

    Optional<TransactionResponsibles> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}