package com.finance_control.transactions.service.subcategory;

import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.service.BaseService;
import com.finance_control.shared.util.ValidationUtils;
import com.finance_control.transactions.dto.subcategory.TransactionSubcategoryDTO;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.repository.subcategory.TransactionSubcategoryRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class TransactionSubcategoryService extends
        BaseService<TransactionSubcategory, Long, TransactionSubcategoryDTO> {

    private static final String CATEGORY_FIELD = "category";
    private static final String IS_ACTIVE_FIELD = "isActive";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_CATEGORY_ID = "categoryId";

    private final TransactionSubcategoryRepository transactionSubcategoryRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;

    public TransactionSubcategoryService(TransactionSubcategoryRepository transactionSubcategoryRepository,
            TransactionCategoryRepository transactionCategoryRepository) {
        super(transactionSubcategoryRepository);
        this.transactionSubcategoryRepository = transactionSubcategoryRepository;
        this.transactionCategoryRepository = transactionCategoryRepository;
    }

    /**
     * Find subcategories by category ID (active only).
     *
     * @param categoryId the category ID
     * @return a list of subcategory DTOs
     */
    public List<TransactionSubcategoryDTO> findByCategoryId(Long categoryId) {
        ValidationUtils.validateId(categoryId);

        Map<String, Object> filters = Map.of(
                FIELD_CATEGORY_ID, categoryId,
                IS_ACTIVE_FIELD, true);

        return findAll(null, filters, "name", "asc", Pageable.unpaged())
                .getContent();
    }

    /**
     * Find all active subcategories.
     *
     * @return a list of active subcategory DTOs
     */
    public List<TransactionSubcategoryDTO> findAllActive() {
        Map<String, Object> filters = Map.of(IS_ACTIVE_FIELD, true);

        return findAll(null, filters, "name", "asc", Pageable.unpaged())
                .getContent();
    }

    /**
     * Find subcategories by category ID ordered by usage.
     *
     * @param categoryId the category ID
     * @return a list of subcategory DTOs ordered by usage
     */
    public List<TransactionSubcategoryDTO> findByCategoryIdOrderByUsage(Long categoryId) {
        ValidationUtils.validateId(categoryId);
        return transactionSubcategoryRepository.findByCategoryIdOrderByUsageAndName(categoryId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Count subcategories by category ID (active only).
     *
     * @param categoryId the category ID
     * @return the count of active subcategories
     */
    public long countByCategoryId(Long categoryId) {
        ValidationUtils.validateId(categoryId);

        Map<String, Object> filters = Map.of(
                FIELD_CATEGORY_ID, categoryId,
                IS_ACTIVE_FIELD, true);

        Specification<TransactionSubcategory> spec = createSpecificationFromFilters(null, filters);
        return transactionSubcategoryRepository.count(spec);
    }

    /**
     * Find subcategory by category ID and name.
     *
     * @param categoryId the category ID
     * @param name       the subcategory name
     * @return an Optional containing the subcategory DTO if found
     */
    public Optional<TransactionSubcategoryDTO> findByCategoryIdAndName(Long categoryId, String name) {
        ValidationUtils.validateId(categoryId);
        ValidationUtils.validateString(name, "Name");

        Map<String, Object> filters = Map.of(
                FIELD_CATEGORY_ID, categoryId,
                FIELD_NAME, name);

        return findAll(null, filters, null, null, Pageable.unpaged())
                .getContent()
                .stream()
                .findFirst();
    }

    /**
     * Check if subcategory exists by category ID and name.
     *
     * @param categoryId the category ID
     * @param name       the subcategory name
     * @return true if exists, false otherwise
     */
    public boolean existsByCategoryIdAndName(Long categoryId, String name) {
        ValidationUtils.validateId(categoryId);
        ValidationUtils.validateString(name, "Name");

        Map<String, Object> filters = Map.of(
                FIELD_CATEGORY_ID, categoryId,
                FIELD_NAME, name);

        Specification<TransactionSubcategory> spec = createSpecificationFromFilters(null, filters);
        return transactionSubcategoryRepository.exists(spec);
    }

    @Override
    protected boolean isNameBased() {
        return false; // Subcategories don't support global name-based operations
    }

    public TransactionSubcategory getTransactionSubcategoryEntity(Long id) {
        return transactionSubcategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction subcategory not found"));
    }

    // BaseService abstract method implementations
    @Override
    protected TransactionSubcategory mapToEntity(TransactionSubcategoryDTO createDTO) {
        validateCategoryExists(createDTO.getCategoryId());

        TransactionCategory category = transactionCategoryRepository.findById(createDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction category not found"));

        TransactionSubcategory entity = new TransactionSubcategory();
        entity.setName(createDTO.getName());
        entity.setDescription(createDTO.getDescription());
        entity.setCategory(category);
        entity.setIsActive(true);
        return entity;
    }

    @Override
    protected void updateEntityFromDTO(TransactionSubcategory entity, TransactionSubcategoryDTO updateDTO) {
        entity.setName(updateDTO.getName());
        entity.setDescription(updateDTO.getDescription());
    }

    @Override
    protected TransactionSubcategoryDTO mapToResponseDTO(TransactionSubcategory entity) {
        TransactionSubcategoryDTO dto = new TransactionSubcategoryDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCategoryId(entity.getCategory().getId());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    @Override
    protected void validateCreateDTO(TransactionSubcategoryDTO createDTO) {
        super.validateCreateDTO(createDTO);
        ValidationUtils.validateId(createDTO.getCategoryId());
    }

    @Override
    protected void validateUpdateDTO(TransactionSubcategoryDTO updateDTO) {
        super.validateUpdateDTO(updateDTO);
    }

    @Override
    protected String getEntityName() {
        return "TransactionSubcategory";
    }

    @Override
    protected Specification<TransactionSubcategory> createSpecificationFromFilters(
            String search, Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();

            addSearchPredicates(predicates, search, root, criteriaBuilder);
            addFilterPredicates(predicates, filters, root, criteriaBuilder);

            return predicates.isEmpty() ? null
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addSearchPredicates(List<Predicate> predicates, String search,
            Root<TransactionSubcategory> root, CriteriaBuilder criteriaBuilder) {
        if (search != null && !search.trim().isEmpty()) {
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(FIELD_NAME)), "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(FIELD_DESCRIPTION)),
                            "%" + search.toLowerCase() + "%")));
        }
    }

    private void addFilterPredicates(List<Predicate> predicates,
            Map<String, Object> filters, Root<TransactionSubcategory> root,
            CriteriaBuilder criteriaBuilder) {
        if (filters != null) {
            filters.forEach((key, value) -> {
                if (value != null) {
                    addFilterPredicate(predicates, key, value, root, criteriaBuilder);
                }
            });
        }
    }

    private void addFilterPredicate(java.util.List<jakarta.persistence.criteria.Predicate> predicates, String key,
            Object value, jakarta.persistence.criteria.Root<TransactionSubcategory> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder) {
        switch (key) {
            case FIELD_CATEGORY_ID ->
                predicates.add(criteriaBuilder.equal(root.get(CATEGORY_FIELD).get("id"), value));
            case IS_ACTIVE_FIELD ->
                predicates.add(criteriaBuilder.equal(root.get(IS_ACTIVE_FIELD), value));
            case FIELD_NAME -> predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(FIELD_NAME)),
                    "%" + value.toString().toLowerCase() + "%"));
            case FIELD_DESCRIPTION ->
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(FIELD_DESCRIPTION)),
                        "%" + value.toString().toLowerCase() + "%"));
            default -> {
                // Ignore unknown filter keys
            }
        }
    }

    // Helper methods
    private void validateCategoryExists(Long categoryId) {
        if (!transactionCategoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Transaction category", "id", categoryId);
        }
    }

}
