package com.finance_control.transactions.service.responsibles;

import com.finance_control.shared.service.BaseService;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.repository.responsibles.TransactionResponsiblesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionResponsiblesService
        extends BaseService<TransactionResponsibles, Long, TransactionResponsiblesDTO> {

    public TransactionResponsiblesService(TransactionResponsiblesRepository transactionResponsibleRepository) {
        super(transactionResponsibleRepository);
    }

    @Override
    protected boolean isNameBased() {
        return true;
    }

    @Override
    protected String getEntityName() {
        return "TransactionResponsible";
    }

    @Override
    protected TransactionResponsibles mapToEntity(TransactionResponsiblesDTO dto) {
        TransactionResponsibles entity = new TransactionResponsibles();
        entity.setName(dto.getName());
        return entity;
    }

    @Override
    protected void updateEntityFromDTO(TransactionResponsibles entity, TransactionResponsiblesDTO dto) {
        entity.setName(dto.getName());
    }

    @Override
    protected TransactionResponsiblesDTO mapToResponseDTO(TransactionResponsibles entity) {
        TransactionResponsiblesDTO dto = new TransactionResponsiblesDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
