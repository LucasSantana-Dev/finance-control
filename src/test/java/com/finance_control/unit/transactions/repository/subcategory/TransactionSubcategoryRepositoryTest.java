package com.finance_control.unit.transactions.repository.subcategory;

import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import com.finance_control.transactions.repository.subcategory.TransactionSubcategoryRepository;
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
 * @DataJpaTest repository layer test for TransactionSubcategoryRepository.
 * Tests JPA repository operations with in-memory H2 database.
 */
@DataJpaTest
@ActiveProfiles("test")
class TransactionSubcategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionSubcategoryRepository transactionSubcategoryRepository;

    private TransactionCategory testCategory;
    private TransactionSubcategory testSubcategory1;
    private TransactionSubcategory testSubcategory2;

    @BeforeEach
    void setUp() {
        // Create test category
        testCategory = new TransactionCategory();
        testCategory.setName("Food & Dining");
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());
        testCategory = entityManager.persistAndFlush(testCategory);

        // Create test subcategories
        testSubcategory1 = new TransactionSubcategory();
        testSubcategory1.setName("Restaurants");
        testSubcategory1.setDescription("Dining out at restaurants");
        testSubcategory1.setIsActive(true);
        testSubcategory1.setCategory(testCategory);
        testSubcategory1.setCreatedAt(LocalDateTime.now());
        testSubcategory1.setUpdatedAt(LocalDateTime.now());
        testSubcategory1 = entityManager.persistAndFlush(testSubcategory1);

        testSubcategory2 = new TransactionSubcategory();
        testSubcategory2.setName("Groceries");
        testSubcategory2.setDescription("Grocery shopping");
        testSubcategory2.setIsActive(true);
        testSubcategory2.setCategory(testCategory);
        testSubcategory2.setCreatedAt(LocalDateTime.now());
        testSubcategory2.setUpdatedAt(LocalDateTime.now());
        testSubcategory2 = entityManager.persistAndFlush(testSubcategory2);
    }

    @Test
    void shouldFindSubcategoryById() {
        // When
        Optional<TransactionSubcategory> result = transactionSubcategoryRepository.findById(testSubcategory1.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Restaurants");
        assertThat(result.get().getCategory().getName()).isEqualTo("Food & Dining");
    }

    @Test
    void shouldFindActiveSubcategoriesByCategoryId() {
        // When
        List<TransactionSubcategory> subcategories = transactionSubcategoryRepository.findByCategoryIdAndIsActiveTrueOrderByNameAsc(testCategory.getId());

        // Then
        assertThat(subcategories).hasSize(2);
        assertThat(subcategories).extracting(TransactionSubcategory::getName)
                .containsExactlyInAnyOrder("Restaurants", "Groceries");
    }

    @Test
    void shouldFindAllActiveSubcategories() {
        // When
        List<TransactionSubcategory> subcategories = transactionSubcategoryRepository.findByIsActiveTrueOrderByNameAsc();

        // Then
        assertThat(subcategories).hasSize(2);
        assertThat(subcategories).extracting(TransactionSubcategory::getName)
                .containsExactlyInAnyOrder("Restaurants", "Groceries");
    }

    @Test
    void shouldFindSubcategoryByCategoryIdAndName() {
        // When
        Optional<TransactionSubcategory> result = transactionSubcategoryRepository.findByCategoryIdAndNameIgnoreCase(testCategory.getId(), "restaurants");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Restaurants");
    }

    @Test
    void shouldReturnEmptyWhenSubcategoryNotFoundByCategoryIdAndName() {
        // When
        Optional<TransactionSubcategory> result = transactionSubcategoryRepository
                .findByCategoryIdAndNameIgnoreCase(testCategory.getId(), "Non-existent Subcategory");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldCheckIfSubcategoryExistsByCategoryIdAndName() {
        // When
        boolean exists = transactionSubcategoryRepository.existsByCategoryIdAndNameIgnoreCase(testCategory.getId(), "restaurants");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSubcategoryDoesNotExistByCategoryIdAndName() {
        // When
        boolean exists = transactionSubcategoryRepository.existsByCategoryIdAndNameIgnoreCase(testCategory.getId(), "Non-existent Subcategory");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindSubcategoriesByCategoryIdOrderedByUsage() {
        // When
        List<TransactionSubcategory> subcategories = transactionSubcategoryRepository.findByCategoryIdOrderByUsageAndName(testCategory.getId());

        // Then
        assertThat(subcategories).hasSize(2);
        // Note: Order by usage would depend on actual transaction count, but for this test we just verify the method works
        assertThat(subcategories).extracting(TransactionSubcategory::getName)
                .containsExactlyInAnyOrder("Restaurants", "Groceries");
    }

    @Test
    void shouldCountActiveSubcategoriesByCategoryId() {
        // When
        long count = transactionSubcategoryRepository.countByCategoryIdAndIsActiveTrue(testCategory.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldSaveNewSubcategory() {
        // Given
        TransactionSubcategory newSubcategory = new TransactionSubcategory();
        newSubcategory.setName("Fast Food");
        newSubcategory.setDescription("Fast food restaurants");
        newSubcategory.setIsActive(true);
        newSubcategory.setCategory(testCategory);
        newSubcategory.setCreatedAt(LocalDateTime.now());
        newSubcategory.setUpdatedAt(LocalDateTime.now());

        // When
        TransactionSubcategory savedSubcategory = transactionSubcategoryRepository.save(newSubcategory);
        entityManager.flush();

        // Then
        assertThat(savedSubcategory.getId()).isNotNull();
        assertThat(savedSubcategory.getName()).isEqualTo("Fast Food");
        assertThat(savedSubcategory.getCategory().getId()).isEqualTo(testCategory.getId());
        
        // Verify it was actually saved
        Optional<TransactionSubcategory> found = transactionSubcategoryRepository.findById(savedSubcategory.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Fast Food");
    }

    @Test
    void shouldUpdateExistingSubcategory() {
        // Given
        testSubcategory1.setName("Updated Restaurants");
        testSubcategory1.setDescription("Updated description");

        // When
        TransactionSubcategory updatedSubcategory = transactionSubcategoryRepository.save(testSubcategory1);
        entityManager.flush();

        // Then
        assertThat(updatedSubcategory.getName()).isEqualTo("Updated Restaurants");
        assertThat(updatedSubcategory.getDescription()).isEqualTo("Updated description");
        
        // Verify the update
        Optional<TransactionSubcategory> found = transactionSubcategoryRepository.findById(testSubcategory1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Restaurants");
    }

    @Test
    void shouldDeleteSubcategory() {
        // Given
        Long subcategoryId = testSubcategory1.getId();

        // When
        transactionSubcategoryRepository.deleteById(subcategoryId);
        entityManager.flush();

        // Then
        Optional<TransactionSubcategory> found = transactionSubcategoryRepository.findById(subcategoryId);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCountSubcategories() {
        // When
        long count = transactionSubcategoryRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldFindAllSubcategories() {
        // When
        List<TransactionSubcategory> subcategories = transactionSubcategoryRepository.findAll();

        // Then
        assertThat(subcategories).hasSize(2);
        assertThat(subcategories).extracting(TransactionSubcategory::getName)
                .containsExactlyInAnyOrder("Restaurants", "Groceries");
    }
}
