package com.finance_control.unit.transactions.repository.category;

import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @DataJpaTest repository layer test for TransactionCategoryRepository.
 * Tests JPA repository operations with in-memory H2 database.
 */
@DataJpaTest
@ActiveProfiles("test")
class TransactionCategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;

    private TransactionCategory testCategory1;
    private TransactionCategory testCategory2;

    @BeforeEach
    void setUp() {
        // Create test categories
        testCategory1 = new TransactionCategory();
        testCategory1.setName("Food & Dining");
        testCategory1.setCreatedAt(LocalDateTime.now());
        testCategory1.setUpdatedAt(LocalDateTime.now());
        testCategory1 = entityManager.persistAndFlush(testCategory1);

        testCategory2 = new TransactionCategory();
        testCategory2.setName("Transportation");
        testCategory2.setCreatedAt(LocalDateTime.now());
        testCategory2.setUpdatedAt(LocalDateTime.now());
        testCategory2 = entityManager.persistAndFlush(testCategory2);
    }

    @Test
    void shouldFindCategoryById() {
        // When
        Optional<TransactionCategory> result = transactionCategoryRepository.findById(testCategory1.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Food & Dining");
    }

    @Test
    void shouldFindAllCategoriesOrderedByName() {
        // When
        List<TransactionCategory> categories = transactionCategoryRepository.findAllByOrderByNameAsc();

        // Then
        assertThat(categories).hasSize(2);
        assertThat(categories.get(0).getName()).isEqualTo("Food & Dining");
        assertThat(categories.get(1).getName()).isEqualTo("Transportation");
    }

    @Test
    void shouldFindCategoryByNameIgnoreCase() {
        // When
        Optional<TransactionCategory> result = transactionCategoryRepository.findByNameIgnoreCase("food & dining");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Food & Dining");
    }

    @Test
    void shouldReturnEmptyWhenCategoryNotFoundByName() {
        // When
        Optional<TransactionCategory> result = transactionCategoryRepository.findByNameIgnoreCase("Non-existent Category");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldCheckIfCategoryExistsByNameIgnoreCase() {
        // When
        boolean exists = transactionCategoryRepository.existsByNameIgnoreCase("food & dining");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenCategoryDoesNotExistByName() {
        // When
        boolean exists = transactionCategoryRepository.existsByNameIgnoreCase("Non-existent Category");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldSaveNewCategory() {
        // Given
        TransactionCategory newCategory = new TransactionCategory();
        newCategory.setName("Entertainment");
        newCategory.setCreatedAt(LocalDateTime.now());
        newCategory.setUpdatedAt(LocalDateTime.now());

        // When
        TransactionCategory savedCategory = transactionCategoryRepository.save(newCategory);
        entityManager.flush();

        // Then
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("Entertainment");

        // Verify it was actually saved
        Optional<TransactionCategory> found = transactionCategoryRepository.findById(savedCategory.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Entertainment");
    }

    @Test
    void shouldUpdateExistingCategory() {
        // Given
        testCategory1.setName("Updated Food & Dining");

        // When
        TransactionCategory updatedCategory = transactionCategoryRepository.save(testCategory1);
        entityManager.flush();

        // Then
        assertThat(updatedCategory.getName()).isEqualTo("Updated Food & Dining");

        // Verify the update
        Optional<TransactionCategory> found = transactionCategoryRepository.findById(testCategory1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Food & Dining");
    }

    @Test
    void shouldDeleteCategory() {
        // Given
        Long categoryId = testCategory1.getId();

        // When
        transactionCategoryRepository.deleteById(categoryId);
        entityManager.flush();

        // Then
        Optional<TransactionCategory> found = transactionCategoryRepository.findById(categoryId);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCountCategories() {
        // When
        long count = transactionCategoryRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldFindAllCategories() {
        // When
        List<TransactionCategory> categories = transactionCategoryRepository.findAll();

        // Then
        assertThat(categories).hasSize(2);
        assertThat(categories).extracting(TransactionCategory::getName)
                .containsExactlyInAnyOrder("Food & Dining", "Transportation");
    }
}
