package com.finance_control.transactions.service.responsibles;

import com.finance_control.shared.service.NameBasedService;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.repository.responsibles.TransactionResponsiblesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionResponsiblesService extends NameBasedService<TransactionResponsibles, Long, TransactionResponsiblesDTO> {

    private final TransactionResponsiblesRepository transactionResponsibleRepository;

    public TransactionResponsiblesService(TransactionResponsiblesRepository transactionResponsibleRepository) {
        super(transactionResponsibleRepository);
        this.transactionResponsibleRepository = transactionResponsibleRepository;
    }

    @Override
    protected NameBasedRepository<TransactionResponsibles, Long> getRepository() {
        return transactionResponsibleRepository;
    }

    @Override
    protected String getEntityName() {
        return "TransactionResponsible";
    }

    @Override
    protected TransactionResponsibles createEntityInstance() {
        return new TransactionResponsibles();
    }

    @Override
    protected TransactionResponsiblesDTO createResponseDTOInstance() {
        return new TransactionResponsiblesDTO();
    }
} 