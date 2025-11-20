package com.finance_control.transactions.service;

import com.finance_control.shared.util.EntityMapper;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles.TransactionResponsibility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Helper class for mapping between Transaction entities and DTOs.
 * Reduces complexity and coupling in TransactionService.
 */
@Component
@RequiredArgsConstructor
public class TransactionMapper {

    public TransactionDTO mapToResponseDTO(Transaction entity) {
        TransactionDTO dto = new TransactionDTO();

        EntityMapper.mapCommonFields(entity, dto);

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
        }
        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getId());
        }
        if (entity.getSubcategory() != null) {
            dto.setSubcategoryId(entity.getSubcategory().getId());
        }
        if (entity.getSourceEntity() != null) {
            dto.setSourceEntityId(entity.getSourceEntity().getId());
        }
        dto.setResponsibilities(entity.getResponsibilities().stream()
                .map(this::mapResponsiblesToDTO)
                .toList());

        return dto;
    }

    public TransactionResponsiblesDTO mapResponsiblesToDTO(TransactionResponsibility responsibility) {
        TransactionResponsiblesDTO dto = new TransactionResponsiblesDTO();

        EntityMapper.mapCommonFields(responsibility, dto);

        dto.setResponsibleId(responsibility.getResponsible().getId());
        dto.setResponsibleName(responsibility.getResponsible().getName());

        return dto;
    }
}



