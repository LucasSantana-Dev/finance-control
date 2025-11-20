package com.finance_control.transactions.service;

import com.finance_control.shared.util.EntityMapper;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.transactions.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for updating Transaction entities from DTOs.
 * Reduces complexity and coupling in TransactionService.
 */
@Component
@RequiredArgsConstructor
public class TransactionUpdateHelper {

    private final TransactionEntityLookupHelper entityLookupHelper;

    public void updateEntityFromDTO(Transaction entity, TransactionDTO updateDTO) {
        updateCommonFields(entity, updateDTO);
        updateRelationships(entity, updateDTO);
        updateResponsibilities(entity, updateDTO);
    }

    private void updateCommonFields(Transaction entity, TransactionDTO updateDTO) {
        List<TransactionResponsiblesDTO> responsibilitiesToUpdate = updateDTO.getResponsibilities();

        updateDTO.setResponsibilities(null);

        EntityMapper.mapCommonFields(updateDTO, entity);

        updateDTO.setResponsibilities(responsibilitiesToUpdate);
    }

    private void updateRelationships(Transaction entity, TransactionDTO updateDTO) {
        updateCategory(entity, updateDTO);
        updateSubcategory(entity, updateDTO);
        updateSourceEntity(entity, updateDTO);
    }

    private void updateCategory(Transaction entity, TransactionDTO updateDTO) {
        if (updateDTO.getCategoryId() != null) {
            entity.setCategory(entityLookupHelper.getCategoryById(updateDTO.getCategoryId()));
        }
    }

    private void updateSubcategory(Transaction entity, TransactionDTO updateDTO) {
        if (updateDTO.getSubcategoryId() != null) {
            entity.setSubcategory(entityLookupHelper.getSubcategoryById(updateDTO.getSubcategoryId()));
        } else {
            entity.setSubcategory(null);
        }
    }

    private void updateSourceEntity(Transaction entity, TransactionDTO updateDTO) {
        if (updateDTO.getSourceEntityId() != null) {
            entity.setSourceEntity(entityLookupHelper.getSourceEntityById(updateDTO.getSourceEntityId()));
        } else {
            entity.setSourceEntity(null);
        }
    }

    private void updateResponsibilities(Transaction entity, TransactionDTO updateDTO) {
        clearExistingResponsibilities(entity);
        addNewResponsibilities(entity, updateDTO);
    }

    private void clearExistingResponsibilities(Transaction entity) {
        if (entity.getResponsibilities() == null) {
            entity.setResponsibilities(new ArrayList<>());
        } else {
            entity.getResponsibilities().clear();
        }
    }

    private void addNewResponsibilities(Transaction entity, TransactionDTO updateDTO) {
        if (updateDTO.getResponsibilities() != null) {
            for (TransactionResponsiblesDTO respDTO : updateDTO.getResponsibilities()) {
                var responsible = entityLookupHelper.getResponsibleById(respDTO.getResponsibleId());
                entity.addResponsible(responsible, respDTO.getPercentage(), respDTO.getNotes());
            }
        }
    }
}



