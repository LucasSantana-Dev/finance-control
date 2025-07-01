package com.finance_control.transactions.service.category;

import com.finance_control.shared.service.NameBasedService;
import com.finance_control.transactions.dto.category.TransactionCategoryDTO;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.validation.TransactionCategoryValidation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionCategoryService extends NameBasedService<TransactionCategory, Long, TransactionCategoryDTO> {

    private final TransactionCategoryRepository transactionCategoryRepository;

    public TransactionCategoryService(TransactionCategoryRepository transactionCategoryRepository) {
        super(transactionCategoryRepository);
        this.transactionCategoryRepository = transactionCategoryRepository;
    }

    @Override
    protected NameBasedRepository<TransactionCategory, Long> getRepository() {
        return transactionCategoryRepository;
    }

    @Override
    protected String getEntityName() {
        return "TransactionCategory";
    }

    @Override
    protected TransactionCategory createEntityInstance() {
        return new TransactionCategory();
    }

    @Override
    protected TransactionCategoryDTO createResponseDTOInstance() {
        return new TransactionCategoryDTO();
    }
    
    @Override
    protected void validateCreateDTO(TransactionCategoryDTO createDTO) {
        createDTO.validateCreate();
    }
    
    @Override
    protected void validateUpdateDTO(TransactionCategoryDTO updateDTO) {
        updateDTO.validateUpdate();
    }
} 