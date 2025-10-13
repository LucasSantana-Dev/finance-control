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

    /**
     * Find all active categories.
     *
     * @return list of active categories
     */
    public java.util.List<TransactionCategoryDTO> findAllActive() {
        return findAll(null, null, "name", "asc", org.springframework.data.domain.Pageable.unpaged()).getContent();
    }

    /**
     * Get total count of categories.
     *
     * @return total count
     */
    public Long getTotalCount() {
        return ((TransactionCategoryRepository) repository).count();
    }

    /**
     * Get usage statistics for categories.
     *
     * @return usage statistics
     */
    public java.util.Map<String, Object> getUsageStats() {
        return ((TransactionCategoryRepository) repository).getUsageStats();
    }
}
