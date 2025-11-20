package com.finance_control.transactions.service;

import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.repository.responsibles.TransactionResponsiblesRepository;
import com.finance_control.transactions.repository.source.TransactionSourceRepository;
import com.finance_control.transactions.repository.subcategory.TransactionSubcategoryRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Helper class for looking up transaction-related entities.
 * Reduces coupling and code duplication in TransactionService.
 */
@Component
@RequiredArgsConstructor
public class TransactionEntityLookupHelper {

    private final UserRepository userRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final TransactionSubcategoryRepository subcategoryRepository;
    private final TransactionSourceRepository sourceEntityRepository;
    private final TransactionResponsiblesRepository responsibleRepository;

    public List<TransactionSubcategory> getSubcategoriesByCategoryId(Long categoryId) {
        return subcategoryRepository.findByCategoryIdAndIsActiveTrueOrderByNameAsc(categoryId);
    }

    public List<TransactionSourceEntity> getAllSourceEntities() {
        return sourceEntityRepository.findAll();
    }

    public User getUserById(Long userId) {
        return findEntityById(userRepository, userId, "User");
    }

    public TransactionCategory getCategoryById(Long categoryId) {
        return findEntityById(categoryRepository, categoryId, "TransactionCategory");
    }

    public TransactionSubcategory getSubcategoryById(Long subcategoryId) {
        return findEntityById(subcategoryRepository, subcategoryId, "TransactionSubcategory");
    }

    public TransactionSourceEntity getSourceEntityById(Long sourceEntityId) {
        return findEntityById(sourceEntityRepository, sourceEntityId, "TransactionSourceEntity");
    }

    public TransactionResponsibles getResponsibleById(Long responsibleId) {
        return findEntityById(responsibleRepository, responsibleId, "TransactionResponsible");
    }

    private <T> T findEntityById(JpaRepository<T, Long> repository, Long id, String entityName) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(entityName, "id", id));
    }
}
