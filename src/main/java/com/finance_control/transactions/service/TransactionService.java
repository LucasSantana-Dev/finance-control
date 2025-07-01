package com.finance_control.transactions.service;

import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.service.BaseService;
import com.finance_control.shared.util.EntityMapper;
import com.finance_control.shared.util.SpecificationUtils;
import com.finance_control.shared.util.ValidationUtils;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.validation.TransactionValidation;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.repository.responsibles.TransactionResponsiblesRepository;
import com.finance_control.transactions.repository.source.TransactionSourceRepository;
import com.finance_control.transactions.repository.subcategory.TransactionSubcategoryRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing financial transaction operations.
 * Provides methods for creating, updating, deleting, and querying transactions
 * with various filtering options including user, type, category, and date
 * ranges.
 */
@Service
@Transactional
public class TransactionService
        extends BaseService<Transaction, Long, TransactionDTO> {

    private static final String DESCRIPTION_FIELD = "Description";

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final TransactionSubcategoryRepository subcategoryRepository;
    private final TransactionSourceRepository sourceEntityRepository;
    private final TransactionResponsiblesRepository responsibleRepository;

    public TransactionService(TransactionRepository transactionRepository,
            UserRepository userRepository,
            TransactionCategoryRepository categoryRepository,
            TransactionSubcategoryRepository subcategoryRepository,
            TransactionSourceRepository sourceEntityRepository,
            TransactionResponsiblesRepository responsibleRepository) {
        super(transactionRepository);
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.sourceEntityRepository = sourceEntityRepository;
        this.responsibleRepository = responsibleRepository;
    }

    /**
     * Retrieves a paginated list of transactions with dynamic filtering.
     * 
     * @param search        optional search term for description
     * @param sortBy        optional field name to sort by
     * @param sortDirection optional sort direction ("asc" or "desc"), defaults to "desc"
     * @param pageable      pagination parameters
     * @param filters       optional filter parameters from DTO
     * @return a page of transaction DTOs matching the criteria
     */
    public Page<TransactionDTO> findAll(String search, String sortBy, String sortDirection, 
            Pageable pageable, TransactionDTO filters) {
        
        // Convert DTO filters to Map for BaseService
        Map<String, Object> filterMap = null;
        if (filters != null) {
            filterMap = Map.of(
                "userId", filters.getUserId(),
                "type", filters.getType(),
                "categoryId", filters.getCategoryId(),
                "subcategoryId", filters.getSubcategoryId(),
                "sourceEntityId", filters.getSourceEntityId(),
                "description", filters.getDescription()
            );
        }
        
        return findAll(search, filterMap, sortBy, sortDirection != null ? sortDirection : "desc", pageable);
    }



    // BaseService abstract method implementations
    @Override
    protected Transaction mapToEntity(TransactionDTO createDTO) {
        Transaction transaction = new Transaction();
        
        // Map common fields using reflection
        EntityMapper.mapCommonFields(createDTO, transaction);
        
        // Set default date if not provided
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDateTime.now());
        }
        
        // Set required relationships
        transaction.setUser(getUserById(createDTO.getUserId()));
        transaction.setCategory(getCategoryById(createDTO.getCategoryId()));
        
        // Set optional relationships
        if (createDTO.getSubcategoryId() != null) {
            transaction.setSubcategory(getSubcategoryById(createDTO.getSubcategoryId()));
        }
        
        if (createDTO.getSourceEntityId() != null) {
            transaction.setSourceEntity(getSourceEntityById(createDTO.getSourceEntityId()));
        }
        
        // Set responsibilities
        if (createDTO.getResponsibilities() != null) {
            for (TransactionResponsiblesDTO respDTO : createDTO.getResponsibilities()) {
                TransactionResponsibles responsible = getResponsibleById(respDTO.getResponsibleId());
                transaction.addResponsible(responsible, respDTO.getPercentage(), respDTO.getNotes());
            }
        }

        return transaction;
    }

    @Override
    protected void updateEntityFromDTO(Transaction entity, TransactionDTO updateDTO) {
        // Map common fields using reflection
        EntityMapper.mapCommonFields(updateDTO, entity);
        
        // Update relationships if provided
        if (updateDTO.getCategoryId() != null) {
            entity.setCategory(getCategoryById(updateDTO.getCategoryId()));
        }
        
        if (updateDTO.getSubcategoryId() != null) {
            entity.setSubcategory(getSubcategoryById(updateDTO.getSubcategoryId()));
        } else {
            entity.setSubcategory(null);
        }
        
        if (updateDTO.getSourceEntityId() != null) {
            entity.setSourceEntity(getSourceEntityById(updateDTO.getSourceEntityId()));
        } else {
            entity.setSourceEntity(null);
        }

        // Clear existing responsibilities and add new ones
        entity.getResponsibilities().clear();
        if (updateDTO.getResponsibilities() != null) {
            for (TransactionResponsiblesDTO respDTO : updateDTO.getResponsibilities()) {
                TransactionResponsibles responsible = getResponsibleById(respDTO.getResponsibleId());
                entity.addResponsible(responsible, respDTO.getPercentage(), respDTO.getNotes());
            }
        }
    }

    @Override
    protected TransactionDTO mapToResponseDTO(Transaction entity) {
        TransactionDTO dto = new TransactionDTO();
        
        // Map common fields using reflection
        EntityMapper.mapCommonFields(entity, dto);
        
        // Map nested fields separately
        dto.setUserId(entity.getUser().getId());
        dto.setCategoryId(entity.getCategory().getId());
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

    @Override
    protected void validateCreateDTO(TransactionDTO createDTO) {
        createDTO.validateCreate();
    }

    @Override
    protected void validateUpdateDTO(TransactionDTO updateDTO) {
        updateDTO.validateUpdate();
    }

    @Override
    protected void validateEntity(Transaction transaction) {
        validateTransaction(transaction);
    }

    @Override
    protected String getEntityName() {
        return "Transaction";
    }
    
    @Override
    protected org.springframework.data.jpa.domain.Specification<Transaction> createSpecificationFromFilters(String search, java.util.Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            // Handle search term across searchable fields
            if (search != null && !search.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + search.toLowerCase() + "%"));
            }
            
            // Handle specific filters using SpecificationUtils patterns
            if (filters != null) {
                filters.forEach((key, value) -> {
                    if (value != null) {
                        switch (key) {
                            case "userId" -> predicates.add(criteriaBuilder.equal(root.get("user").get("id"), value));
                            case "type" -> predicates.add(criteriaBuilder.equal(root.get("type"), value));
                            case "categoryId" -> predicates.add(criteriaBuilder.equal(root.get("category").get("id"), value));
                            case "subcategoryId" -> predicates.add(criteriaBuilder.equal(root.get("subcategory").get("id"), value));
                            case "sourceEntityId" -> predicates.add(criteriaBuilder.equal(root.get("sourceEntity").get("id"), value));
                            case "description" -> predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + value.toString().toLowerCase() + "%"));
                        }
                    }
                });
            }
            
            return predicates.isEmpty() ? null : criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    // Helper methods for entity fetching
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", "id", userId));
    }
    
    private TransactionCategory getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("TransactionCategory", "id", categoryId));
    }
    
    private TransactionSubcategory getSubcategoryById(Long subcategoryId) {
        return subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new EntityNotFoundException("TransactionSubcategory", "id", subcategoryId));
    }
    
    private TransactionSourceEntity getSourceEntityById(Long sourceEntityId) {
        return sourceEntityRepository.findById(sourceEntityId)
                .orElseThrow(() -> new EntityNotFoundException("TransactionSourceEntity", "id", sourceEntityId));
    }
    
    private TransactionResponsibles getResponsibleById(Long responsibleId) {
        return responsibleRepository.findById(responsibleId)
                .orElseThrow(() -> new EntityNotFoundException("TransactionResponsible", "id", responsibleId));
    }

    // Helper methods for validation
    private void validateOptionalIds(Long... ids) {
        for (Long id : ids) {
            if (id != null) {
                ValidationUtils.validateId(id);
            }
        }
    }
    
    private void validateInstallments(Integer installments) {
        if (installments != null && installments <= 0) {
            throw new IllegalArgumentException("Installments must be greater than 0");
        }
    }

    private TransactionResponsiblesDTO mapResponsiblesToDTO(
            com.finance_control.transactions.model.responsibles.TransactionResponsibles.TransactionResponsibility responsibility) {
        TransactionResponsiblesDTO dto = new TransactionResponsiblesDTO();
        
        // Map common fields using reflection
        EntityMapper.mapCommonFields(responsibility, dto);
        
        // Map nested fields separately
        dto.setResponsibleId(responsibility.getResponsible().getId());
        dto.setResponsibleName(responsibility.getResponsible().getName());
        
        return dto;
    }

    private void validateTransaction(Transaction transaction) {
        ValidationUtils.validateAmount(transaction.getAmount());
        ValidationUtils.validateString(transaction.getDescription(), DESCRIPTION_FIELD);

        if (!transaction.isPercentageValid()) {
            throw new IllegalArgumentException("Total percentage of responsibilities must equal 100%");
        }

        for (com.finance_control.transactions.model.responsibles.TransactionResponsibles.TransactionResponsibility responsibility : transaction
                .getResponsibilities()) {
            ValidationUtils.validatePercentage(responsibility.getPercentage());
        }
    }
}