package com.finance_control.transactions.service.category;

import com.finance_control.shared.service.BaseService;
import com.finance_control.transactions.dto.category.TransactionCategoryDTO;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionCategoryService extends BaseService<TransactionCategory, Long, TransactionCategoryDTO> {

    public TransactionCategoryService(TransactionCategoryRepository transactionCategoryRepository) {
        super(transactionCategoryRepository);
    }

    @Override
    protected boolean isNameBased() {
        return true;
    }

    @Override
    protected String getEntityName() {
        return "TransactionCategory";
    }

    @Override
    protected TransactionCategory mapToEntity(TransactionCategoryDTO dto) {
        TransactionCategory entity = new TransactionCategory();
        entity.setName(dto.getName());
        return entity;
    }

    @Override
    protected void updateEntityFromDTO(TransactionCategory entity, TransactionCategoryDTO dto) {
        entity.setName(dto.getName());
    }

    @Override
    protected TransactionCategoryDTO mapToResponseDTO(TransactionCategory entity) {
        TransactionCategoryDTO dto = new TransactionCategoryDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    @Override
    protected void validateCreateDTO(TransactionCategoryDTO createDTO) {
        createDTO.validateCreate();
        super.validateCreateDTO(createDTO); // This calls the duplicate validation
    }

    @Override
    protected void validateUpdateDTO(TransactionCategoryDTO updateDTO) {
        updateDTO.validateUpdate();
        super.validateUpdateDTO(updateDTO); // This calls the duplicate validation
    }
}
