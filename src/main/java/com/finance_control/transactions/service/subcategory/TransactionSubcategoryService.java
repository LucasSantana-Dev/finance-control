package com.finance_control.transactions.service.subcategory;

import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.service.BaseService;
import com.finance_control.shared.util.EntityMapper;
import com.finance_control.shared.util.SpecificationUtils;
import com.finance_control.shared.util.ValidationUtils;
import com.finance_control.transactions.dto.subcategory.TransactionSubcategoryDTO;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.repository.subcategory.TransactionSubcategoryRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class TransactionSubcategoryService extends
        BaseService<TransactionSubcategory, Long, TransactionSubcategoryDTO> {

    private static final String CATEGORY_FIELD = "category";
    private static final String IS_ACTIVE_FIELD = "isActive";

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
                "categoryId", categoryId,
                "isActive", true);

        return findAll(null, filters, "name", "asc", Pageable.unpaged())
                .getContent();
    }

    /**
     * Find all active subcategories.
     * 
     * @return a list of active subcategory DTOs
     */
    public List<TransactionSubcategoryDTO> findAllActive() {
        Map<String, Object> filters = Map.of("isActive", true);

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
                "categoryId", categoryId,
                "isActive", true);

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
                "categoryId", categoryId,
                "name", name);

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
                "categoryId", categoryId,
                "name", name);

        Specification<TransactionSubcategory> spec = createSpecificationFromFilters(null, filters);
        return transactionSubcategoryRepository.exists(spec);
    }

    public TransactionSubcategory getTransactionSubcategoryEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction subcategory not found"));
    }

    // BaseService abstract method implementations
    @Override
    protected TransactionSubcategory mapToEntity(TransactionSubcategoryDTO createDTO) {
        validateCategoryExists(createDTO.getCategoryId());

        if (transactionSubcategoryRepository.existsByCategoryIdAndNameIgnoreCase(createDTO.getCategoryId(),
                createDTO.getName())) {
            throw new IllegalArgumentException(
                    "Transaction subcategory with this name already exists in this category");
        }

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
        if (!entity.getName().equalsIgnoreCase(updateDTO.getName()) &&
                transactionSubcategoryRepository.existsByCategoryIdAndNameIgnoreCase(entity.getCategory().getId(),
                        updateDTO.getName())) {
            throw new IllegalArgumentException(
                    "Transaction subcategory with this name already exists in this category");
        }

        entity.setName(updateDTO.getName());
        entity.setDescription(updateDTO.getDescription());
    }

    @Override
    protected TransactionSubcategoryDTO mapToResponseDTO(TransactionSubcategory entity) {
        return convertToDTO(entity);
    }

    @Override
    protected void validateCreateDTO(TransactionSubcategoryDTO createDTO) {
        ValidationUtils.validateString(createDTO.getName(), "Name");
        ValidationUtils.validateId(createDTO.getCategoryId());
    }

    @Override
    protected void validateUpdateDTO(TransactionSubcategoryDTO updateDTO) {
        ValidationUtils.validateString(updateDTO.getName(), "Name");
    }

    @Override
    protected String getEntityName() {
        return "TransactionSubcategory";
    }

    @Override
    protected org.springframework.data.jpa.domain.Specification<TransactionSubcategory> createSpecificationFromFilters(
            String search, java.util.Map<String, Object> filters) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            // Handle search term across searchable fields
            if (search != null && !search.trim().isEmpty()) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + search.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                                "%" + search.toLowerCase() + "%")));
            }

            // Handle specific filters using SpecificationUtils
            if (filters != null) {
                filters.forEach((key, value) -> {
                    if (value != null) {
                        switch (key) {
                            case "categoryId" ->
                                predicates.add(criteriaBuilder.equal(root.get(CATEGORY_FIELD).get("id"), value));
                            case "isActive" ->
                                predicates.add((Boolean) value ? criteriaBuilder.isTrue(root.get(IS_ACTIVE_FIELD))
                                        : criteriaBuilder.isFalse(root.get(IS_ACTIVE_FIELD)));
                            case "name" -> predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                                    "%" + value.toString().toLowerCase() + "%"));
                            case "description" ->
                                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                                        "%" + value.toString().toLowerCase() + "%"));
                        }
                    }
                });
            }

            return predicates.isEmpty() ? null
                    : criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    // Helper methods
    private void validateCategoryExists(Long categoryId) {
        if (!transactionCategoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Transaction category", "id", categoryId);
        }
    }

    private TransactionSubcategoryDTO convertToDTO(TransactionSubcategory entity) {
        TransactionSubcategoryDTO dto = new TransactionSubcategoryDTO();

        // Map common fields using reflection
        EntityMapper.mapCommonFields(entity, dto);

        // Map nested fields separately
        dto.setCategoryId(entity.getCategory().getId());
        dto.setCategoryName(entity.getCategory().getName());

        return dto;
    }
}