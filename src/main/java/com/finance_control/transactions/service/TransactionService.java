package com.finance_control.transactions.service;

import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.shared.service.BaseService;
import com.finance_control.shared.util.ValidationUtils;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.dto.TransactionReconciliationRequest;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles.TransactionResponsibility;
import com.finance_control.transactions.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for managing financial transaction operations.
 * Provides methods for creating, updating, deleting, and querying transactions
 * with various filtering options including user, type, category, and date
 * ranges.
 */
@Service
@Transactional
@Slf4j
public class TransactionService
        extends BaseService<Transaction, Long, TransactionDTO> {

    private static final String FIELD_DESCRIPTION = "description";

    private final TransactionRepository transactionRepository;
    private final TransactionEntityLookupHelper entityLookupHelper;
    private final TransactionMapper transactionMapper;
    private final TransactionSpecificationBuilder specificationBuilder;
    private final TransactionUpdateHelper updateHelper;
    private final TransactionQueryHelper queryHelper;
    private final MetricsService metricsService;
    private final TransactionNotificationHelper notificationHelper;

    public TransactionService(TransactionRepository transactionRepository,
            TransactionEntityLookupHelper entityLookupHelper,
            TransactionMapper transactionMapper,
            TransactionSpecificationBuilder specificationBuilder,
            TransactionUpdateHelper updateHelper,
            TransactionQueryHelper queryHelper,
            MetricsService metricsService,
            TransactionNotificationHelper notificationHelper) {
        super(transactionRepository);
        this.transactionRepository = transactionRepository;
        this.entityLookupHelper = entityLookupHelper;
        this.transactionMapper = transactionMapper;
        this.specificationBuilder = specificationBuilder;
        this.updateHelper = updateHelper;
        this.queryHelper = queryHelper;
        this.metricsService = metricsService;
        this.notificationHelper = notificationHelper;
    }

    /**
     * Retrieves a paginated list of transactions with dynamic filtering.
     *
     * @param search        optional search term for description
     * @param sortBy        optional field name to sort by
     * @param sortDirection optional sort direction ("asc" or "desc"), defaults to
     *                      "desc"
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
                    FIELD_DESCRIPTION, filters.getDescription());
        }

        return findAll(search, filterMap, sortBy, sortDirection != null ? sortDirection : "desc", pageable);
    }

    // BaseService abstract method implementations
    @Override
    protected Transaction mapToEntity(TransactionDTO createDTO) {
        Transaction transaction = new Transaction();

        // Map simple fields manually (avoid EntityMapper to prevent DTO list being copied to entity)
        transaction.setDescription(createDTO.getDescription());
        transaction.setAmount(createDTO.getAmount());
        transaction.setType(createDTO.getType());
        transaction.setSubtype(createDTO.getSubtype());
        transaction.setSource(createDTO.getSource());
        transaction.setInstallments(createDTO.getInstallments());
        transaction.setDate(createDTO.getDate());

        // Set default date if not provided
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDateTime.now());
        }

        // Set required relationships
        transaction.setUser(entityLookupHelper.getUserById(createDTO.getUserId()));
        transaction.setCategory(entityLookupHelper.getCategoryById(createDTO.getCategoryId()));

        // Set optional relationships
        if (createDTO.getSubcategoryId() != null) {
            transaction.setSubcategory(entityLookupHelper.getSubcategoryById(createDTO.getSubcategoryId()));
        }

        if (createDTO.getSourceEntityId() != null) {
            transaction.setSourceEntity(entityLookupHelper.getSourceEntityById(createDTO.getSourceEntityId()));
        }

        // Set responsibilities (properly convert DTOs to entities)
        if (createDTO.getResponsibilities() != null) {
            List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>(createDTO.getResponsibilities());
            for (TransactionResponsiblesDTO respDTO : responsibilities) {
                TransactionResponsibles responsible = entityLookupHelper.getResponsibleById(respDTO.getResponsibleId());
                transaction.addResponsible(responsible, respDTO.getPercentage(), respDTO.getNotes());
            }
        }

        return transaction;
    }

    @Override
    protected void updateEntityFromDTO(Transaction entity, TransactionDTO updateDTO) {
        updateHelper.updateEntityFromDTO(entity, updateDTO);
    }

    @Override
    protected TransactionDTO mapToResponseDTO(Transaction entity) {
        return transactionMapper.mapToResponseDTO(entity);
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
    protected boolean isUserAware() {
        return true;
    }

    @Override
    protected boolean belongsToUser(Transaction entity, Long userId) {
        return entity.getUser().getId().equals(userId);
    }

    @Override
    protected void setUserId(Transaction entity, Long userId) {
        entity.setUser(entityLookupHelper.getUserById(userId));
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
    protected org.springframework.data.jpa.domain.Specification<Transaction> createSpecificationFromFilters(
            String search, Map<String, Object> filters) {
        return specificationBuilder.buildSpecification(search, filters);
    }


    /**
     * Get categories by user ID.
     *
     * @param userId the user ID
     * @return list of categories used by the user
     */
    public List<com.finance_control.transactions.model.category.TransactionCategory> getCategoriesByUserId(Long userId) {
        return queryHelper.getCategoriesByUserId(userId);
    }

    /**
     * Get subcategories by category ID.
     *
     * @param categoryId the category ID
     * @return list of subcategories for the category
     */
    public List<com.finance_control.transactions.model.subcategory.TransactionSubcategory> getSubcategoriesByCategoryId(Long categoryId) {
        return queryHelper.getSubcategoriesByCategoryId(categoryId);
    }

    /**
     * Get all transaction types.
     *
     * @return list of distinct transaction types
     */
    public List<String> getTransactionTypes() {
        return queryHelper.getTransactionTypes();
    }

    /**
     * Get all source entities.
     *
     * @return list of all source entities
     */
    public List<com.finance_control.transactions.model.source.TransactionSourceEntity> getSourceEntities() {
        return queryHelper.getSourceEntities();
    }

    /**
     * Get total amount by user ID.
     *
     * @param userId the user ID
     * @return total amount for the user
     */
    public BigDecimal getTotalAmountByUserId(Long userId) {
        return queryHelper.getTotalAmountByUserId(userId);
    }

    /**
     * Get amount by type for a user.
     *
     * @param userId the user ID
     * @return map of type to total amount
     */
    public Map<String, BigDecimal> getAmountByType(Long userId) {
        return queryHelper.getAmountByType(userId);
    }

    /**
     * Get amount by category for a user.
     *
     * @param userId the user ID
     * @return map of category to total amount
     */
    public Map<String, BigDecimal> getAmountByCategory(Long userId) {
        return queryHelper.getAmountByCategory(userId);
    }

    /**
     * Get monthly summary for a user within date range.
     *
     * @param userId the user ID
     * @param startDate the start date
     * @param endDate the end date
     * @return monthly summary data
     */
    public Map<String, Object> getMonthlySummary(Long userId, LocalDate startDate, LocalDate endDate) {
        return queryHelper.getMonthlySummary(userId, startDate, endDate);
    }


    private void validateTransaction(Transaction transaction) {
        ValidationUtils.validateAmount(transaction.getAmount());
        ValidationUtils.validateString(transaction.getDescription(), FIELD_DESCRIPTION);

        if (!transaction.isPercentageValid()) {
            throw new IllegalArgumentException("Total percentage of responsibilities must equal 100%");
        }

        for (TransactionResponsibility responsibility : transaction
                .getResponsibilities()) {
            ValidationUtils.validatePercentage(responsibility.getPercentage());
        }
    }

    /**
     * Reconcile a transaction with complete reconciliation data.
     *
     * @param id      the ID of the transaction to reconcile
     * @param request the reconciliation request data
     * @return the reconciled transaction DTO
     * @throws EntityNotFoundException if the transaction is not found
     */
    public TransactionDTO reconcileTransaction(Long id, TransactionReconciliationRequest request) {
        validateId(id);
        Transaction transaction = getEntityById(id);

        // Update reconciliation fields
        transaction.setReconciledAmount(request.getReconciledAmount());
        transaction.setReconciliationDate(request.getReconciliationDate());
        transaction.setReconciled(request.getReconciled());
        transaction.setReconciliationNotes(request.getReconciliationNotes());
        transaction.setBankReference(request.getBankReference());
        transaction.setExternalReference(request.getExternalReference());

        transactionRepository.save(transaction);

        log.info("Transaction reconciled successfully (ID present: {})", id != null);
        return mapToResponseDTO(transaction);
    }

    @Override
    public TransactionDTO create(TransactionDTO createDTO) {
        var sample = metricsService.startTransactionProcessingTimer();
        try {
            TransactionDTO result = super.create(createDTO);
            metricsService.recordTransactionAmount(createDTO.getAmount().doubleValue(), createDTO.getType().name());
            notificationHelper.notifyTransactionChange(result, "creation");
            return result;
        } finally {
            metricsService.recordTransactionProcessingTime(sample);
        }
    }

    @Override
    public TransactionDTO update(Long id, TransactionDTO updateDTO) {
        var sample = metricsService.startTransactionProcessingTimer();
        try {
            TransactionDTO result = super.update(id, updateDTO);
            notificationHelper.notifyTransactionChange(result, "update");
            return result;
        } finally {
            metricsService.recordTransactionProcessingTime(sample);
        }
    }

    @Override
    public void delete(Long id) {
        super.delete(id);
    }

    /**
     * Create transaction installments.
     *
     * @param request the installment request
     * @return list of created transaction DTOs
     */
    public List<TransactionDTO> createInstallments(com.finance_control.transactions.dto.TransactionInstallmentRequest request) {
        String groupId = java.util.UUID.randomUUID().toString();
        BigDecimal installmentAmount = request.getTotalAmount().divide(BigDecimal.valueOf(request.getInstallmentCount()), 2, java.math.RoundingMode.HALF_UP);
        List<TransactionDTO> createdTransactions = new ArrayList<>();

        // Validate user ID if not present in request (should be set by controller)
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        for (int i = 0; i < request.getInstallmentCount(); i++) {
            TransactionDTO dto = new TransactionDTO();
            dto.setDescription(request.getDescription() + " (" + (i + 1) + "/" + request.getInstallmentCount() + ")");
            dto.setAmount(installmentAmount);
            dto.setType(com.finance_control.shared.enums.TransactionType.valueOf(request.getType()));
            dto.setSubtype(com.finance_control.shared.enums.TransactionSubtype.valueOf(request.getSubtype()));
            dto.setSource(com.finance_control.shared.enums.TransactionSource.valueOf(request.getSource()));
            dto.setCategoryId(request.getCategoryId());
            dto.setSubcategoryId(request.getSubcategoryId());
            dto.setUserId(request.getUserId());
            dto.setDate(request.getFirstInstallmentDate().plusMonths(i).atStartOfDay());
            dto.setInstallments(request.getInstallmentCount());

            // Map to entity to set relationships
            Transaction entity = mapToEntity(dto);

            // Set specific installment fields
            entity.setInstallmentGroupId(groupId);
            entity.setInstallmentNumber(i + 1);
            entity.setTotalInstallments(request.getInstallmentCount());
            entity.setInstallmentAmount(installmentAmount);

            // Save
            entity = transactionRepository.save(entity);

            // Notify and add to result
            TransactionDTO resultDTO = mapToResponseDTO(entity);
            notificationHelper.notifyTransactionChange(resultDTO, "creation");
            createdTransactions.add(resultDTO);
        }

        // Record metrics for the total amount
        metricsService.recordTransactionAmount(request.getTotalAmount().doubleValue(), request.getType());

        return createdTransactions;
    }

}
