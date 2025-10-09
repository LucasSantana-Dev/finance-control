package com.finance_control.integration.transactions.service.category;

import com.finance_control.integration.BaseIntegrationTest;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.transactions.dto.category.TransactionCategoryDTO;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.service.category.TransactionCategoryService;
import com.finance_control.unit.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
class TransactionCategoryServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionCategoryService transactionCategoryService;

    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;

    private TransactionCategory testCategory;

    @BeforeEach
    void setUp() {
        // Create test category
        testCategory = new TransactionCategory();
        testCategory.setName("Test Category");
        testCategory = transactionCategoryRepository.save(testCategory);

        // Set up user context for the service calls
        TestUtils.setupUserContext(1L);
    }

    @AfterEach
    void tearDown() {
        // Clear user context after each test
        TestUtils.clearUserContext();
    }

    @Test
    void create_WithValidData_ShouldCreateAndReturnCategory() {
        // Given
        TransactionCategoryDTO createDTO = new TransactionCategoryDTO();
        createDTO.setName("New Category");

        // When
        TransactionCategoryDTO result = transactionCategoryService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("New Category");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        // Verify it was saved to database
        TransactionCategory savedCategory = transactionCategoryRepository.findById(result.getId()).orElse(null);
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("New Category");
    }

    @Test
    void create_WithDuplicateName_ShouldThrowException() {
        // Given
        TransactionCategoryDTO createDTO = new TransactionCategoryDTO();
        createDTO.setName("Test Category"); // Same name as existing category

        // When & Then
        assertThatThrownBy(() -> transactionCategoryService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void findById_WithExistingId_ShouldReturnCategory() {
        // When
        Optional<TransactionCategoryDTO> result = transactionCategoryService.findById(testCategory.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testCategory.getId());
        assertThat(result.get().getName()).isEqualTo("Test Category");
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowException() {
        // When
        Optional<TransactionCategoryDTO> result = transactionCategoryService.findById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_WithNoFilters_ShouldReturnAllCategories() {
        // Given - we already have one category from setUp
        TransactionCategory anotherCategory = new TransactionCategory();
        anotherCategory.setName("Another Category");
        transactionCategoryRepository.save(anotherCategory);

        // When
        Page<TransactionCategoryDTO> result = transactionCategoryService.findAll(
                null, null, null, null, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(TransactionCategoryDTO::getName)
                .containsExactlyInAnyOrder("Test Category", "Another Category");
    }

    @Test
    void findAll_WithSearch_ShouldReturnFilteredCategories() {
        // Given
        TransactionCategory anotherCategory = new TransactionCategory();
        anotherCategory.setName("Another Category");
        transactionCategoryRepository.save(anotherCategory);

        // When
        Page<TransactionCategoryDTO> result = transactionCategoryService.findAll(
                "test", null, null, null, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Category");
    }

    @Test
    void update_WithValidData_ShouldUpdateAndReturnCategory() {
        // Given
        TransactionCategoryDTO updateDTO = new TransactionCategoryDTO();
        updateDTO.setName("Updated Category");

        // When
        TransactionCategoryDTO result = transactionCategoryService.update(testCategory.getId(), updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testCategory.getId());
        assertThat(result.getName()).isEqualTo("Updated Category");
        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(result.getCreatedAt());

        // Verify it was updated in database
        TransactionCategory updatedCategory = transactionCategoryRepository.findById(testCategory.getId()).orElse(null);
        assertThat(updatedCategory).isNotNull();
        assertThat(updatedCategory.getName()).isEqualTo("Updated Category");
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        // Given
        TransactionCategoryDTO updateDTO = new TransactionCategoryDTO();
        updateDTO.setName("Updated Category");

        // When & Then
        assertThatThrownBy(() -> transactionCategoryService.update(999L, updateDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void delete_WithExistingId_ShouldDeleteCategory() {
        // Given
        Long categoryId = testCategory.getId();

        // When
        transactionCategoryService.delete(categoryId);

        // Then
        // Verify it was deleted from database
        boolean exists = transactionCategoryRepository.existsById(categoryId);
        assertThat(exists).isFalse();
    }

    @Test
    void delete_WithNonExistingId_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> transactionCategoryService.delete(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void create_WithNullName_ShouldThrowException() {
        // Given
        TransactionCategoryDTO createDTO = new TransactionCategoryDTO();
        createDTO.setName(null);

        // When & Then
        assertThatThrownBy(() -> transactionCategoryService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_WithEmptyName_ShouldThrowException() {
        // Given
        TransactionCategoryDTO createDTO = new TransactionCategoryDTO();
        createDTO.setName("");

        // When & Then
        assertThatThrownBy(() -> transactionCategoryService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findAll_WithPagination_ShouldReturnCorrectPage() {
        // Given - create multiple categories
        for (int i = 1; i <= 5; i++) {
            TransactionCategory category = new TransactionCategory();
            category.setName("Category " + i);
            transactionCategoryRepository.save(category);
        }

        // When
        Page<TransactionCategoryDTO> result = transactionCategoryService.findAll(
                null, null, null, null, PageRequest.of(0, 3));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(6); // 5 new + 1 from setUp
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void findAll_WithSorting_ShouldReturnSortedResults() {
        // Given
        TransactionCategory categoryA = new TransactionCategory();
        categoryA.setName("Category A");
        transactionCategoryRepository.save(categoryA);

        TransactionCategory categoryB = new TransactionCategory();
        categoryB.setName("Category B");
        transactionCategoryRepository.save(categoryB);

        // When
        Page<TransactionCategoryDTO> result = transactionCategoryService.findAll(
                null, null, "name", "asc", PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(TransactionCategoryDTO::getName)
                .containsExactly("Category A", "Category B", "Test Category");
    }
}
