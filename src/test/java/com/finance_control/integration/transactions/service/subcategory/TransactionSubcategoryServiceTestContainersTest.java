package com.finance_control.integration.transactions.service.subcategory;

import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.transactions.dto.subcategory.TransactionSubcategoryDTO;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.repository.subcategory.TransactionSubcategoryRepository;
import com.finance_control.transactions.service.subcategory.TransactionSubcategoryService;
import com.finance_control.unit.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class TransactionSubcategoryServiceTestContainersTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("finance_control_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TransactionSubcategoryService transactionSubcategoryService;

    @Autowired
    private TransactionSubcategoryRepository transactionSubcategoryRepository;

    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;

    private TransactionCategory testCategory;
    private TransactionSubcategory testSubcategory;

    @BeforeEach
    void setUp() {
        // Create test category
        testCategory = new TransactionCategory();
        testCategory.setName("Test Category");
        testCategory = transactionCategoryRepository.save(testCategory);

        // Create test subcategory
        testSubcategory = new TransactionSubcategory();
        testSubcategory.setName("Test Subcategory");
        testSubcategory.setDescription("Test Description");
        testSubcategory.setCategory(testCategory);
        testSubcategory.setIsActive(true);
        testSubcategory = transactionSubcategoryRepository.save(testSubcategory);

        // Set up user context for the service calls
        TestUtils.setupUserContext(1L);
    }

    @AfterEach
    void tearDown() {
        // Clear user context after each test
        TestUtils.clearUserContext();
    }

    @Test
    void create_WithValidData_ShouldCreateAndReturnSubcategory() {
        // Given
        TransactionSubcategoryDTO createDTO = new TransactionSubcategoryDTO();
        createDTO.setName("New Subcategory");
        createDTO.setDescription("New Description");
        createDTO.setCategoryId(testCategory.getId());

        // When
        TransactionSubcategoryDTO result = transactionSubcategoryService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("New Subcategory");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getCategoryId()).isEqualTo(testCategory.getId());
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        // Verify it was saved to database
        TransactionSubcategory savedSubcategory = transactionSubcategoryRepository.findById(result.getId()).orElse(null);
        assertThat(savedSubcategory).isNotNull();
        assertThat(savedSubcategory.getName()).isEqualTo("New Subcategory");
        assertThat(savedSubcategory.getCategory().getId()).isEqualTo(testCategory.getId());
    }

    @Test
    void create_WithNonExistingCategory_ShouldThrowException() {
        // Given
        TransactionSubcategoryDTO createDTO = new TransactionSubcategoryDTO();
        createDTO.setName("New Subcategory");
        createDTO.setCategoryId(999L);

        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.create(createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Transaction category not found");
    }

    @Test
    void create_WithDuplicateNameInCategory_ShouldThrowException() {
        // Given
        TransactionSubcategoryDTO createDTO = new TransactionSubcategoryDTO();
        createDTO.setName("Test Subcategory"); // Same name as existing subcategory
        createDTO.setCategoryId(testCategory.getId());

        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void findById_WithExistingId_ShouldReturnSubcategory() {
        // When
        Optional<TransactionSubcategoryDTO> result = transactionSubcategoryService.findById(testSubcategory.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testSubcategory.getId());
        assertThat(result.get().getName()).isEqualTo("Test Subcategory");
        assertThat(result.get().getCategoryId()).isEqualTo(testCategory.getId());
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void findByCategoryId_WithValidCategoryId_ShouldReturnSubcategories() {
        // Given - create another subcategory in the same category
        TransactionSubcategory anotherSubcategory = new TransactionSubcategory();
        anotherSubcategory.setName("Another Subcategory");
        anotherSubcategory.setCategory(testCategory);
        anotherSubcategory.setIsActive(true);
        transactionSubcategoryRepository.save(anotherSubcategory);

        // When
        List<TransactionSubcategoryDTO> result = transactionSubcategoryService.findByCategoryId(testCategory.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(TransactionSubcategoryDTO::getName)
                .containsExactlyInAnyOrder("Test Subcategory", "Another Subcategory");
    }

    @Test
    void findByCategoryIdOrderByUsage_WithValidCategoryId_ShouldReturnOrderedSubcategories() {
        // Given - create another subcategory
        TransactionSubcategory anotherSubcategory = new TransactionSubcategory();
        anotherSubcategory.setName("Another Subcategory");
        anotherSubcategory.setCategory(testCategory);
        anotherSubcategory.setIsActive(true);
        transactionSubcategoryRepository.save(anotherSubcategory);

        // When
        List<TransactionSubcategoryDTO> result = transactionSubcategoryService.findByCategoryIdOrderByUsage(testCategory.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(TransactionSubcategoryDTO::getName)
                .containsExactlyInAnyOrder("Test Subcategory", "Another Subcategory");
    }

    @Test
    void countByCategoryId_WithValidCategoryId_ShouldReturnCount() {
        // Given - create another subcategory
        TransactionSubcategory anotherSubcategory = new TransactionSubcategory();
        anotherSubcategory.setName("Another Subcategory");
        anotherSubcategory.setCategory(testCategory);
        anotherSubcategory.setIsActive(true);
        transactionSubcategoryRepository.save(anotherSubcategory);

        // When
        long result = transactionSubcategoryService.countByCategoryId(testCategory.getId());

        // Then
        assertThat(result).isEqualTo(2);
    }

    @Test
    void update_WithValidData_ShouldUpdateAndReturnSubcategory() {
        // Given
        TransactionSubcategoryDTO updateDTO = new TransactionSubcategoryDTO();
        updateDTO.setName("Updated Subcategory");
        updateDTO.setDescription("Updated Description");

        // When
        TransactionSubcategoryDTO result = transactionSubcategoryService.update(testSubcategory.getId(), updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testSubcategory.getId());
        assertThat(result.getName()).isEqualTo("Updated Subcategory");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getUpdatedAt()).isAfter(result.getCreatedAt());

        // Verify it was updated in database
        TransactionSubcategory updatedSubcategory = transactionSubcategoryRepository.findById(testSubcategory.getId()).orElse(null);
        assertThat(updatedSubcategory).isNotNull();
        assertThat(updatedSubcategory.getName()).isEqualTo("Updated Subcategory");
        assertThat(updatedSubcategory.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        // Given
        TransactionSubcategoryDTO updateDTO = new TransactionSubcategoryDTO();
        updateDTO.setName("Updated Subcategory");

        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.update(999L, updateDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void delete_WithExistingId_ShouldDeleteSubcategory() {
        // Given
        Long subcategoryId = testSubcategory.getId();

        // When
        transactionSubcategoryService.delete(subcategoryId);

        // Then
        // Verify it was deleted from database
        boolean exists = transactionSubcategoryRepository.existsById(subcategoryId);
        assertThat(exists).isFalse();
    }

    @Test
    void delete_WithNonExistingId_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.delete(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void findAllActive_ShouldReturnActiveSubcategories() {
        // Given - create an inactive subcategory
        TransactionSubcategory inactiveSubcategory = new TransactionSubcategory();
        inactiveSubcategory.setName("Inactive Subcategory");
        inactiveSubcategory.setCategory(testCategory);
        inactiveSubcategory.setIsActive(false);
        transactionSubcategoryRepository.save(inactiveSubcategory);

        // When
        List<TransactionSubcategoryDTO> result = transactionSubcategoryService.findAllActive();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1); // Only the active one from setUp
        assertThat(result.get(0).getName()).isEqualTo("Test Subcategory");
        assertThat(result.get(0).getIsActive()).isTrue();
    }

    @Test
    void findAll_WithSearch_ShouldReturnFilteredSubcategories() {
        // Given
        TransactionSubcategory anotherSubcategory = new TransactionSubcategory();
        anotherSubcategory.setName("Another Subcategory");
        anotherSubcategory.setDescription("Another description");
        anotherSubcategory.setCategory(testCategory);
        anotherSubcategory.setIsActive(true);
        transactionSubcategoryRepository.save(anotherSubcategory);

        // When
        Page<TransactionSubcategoryDTO> result = transactionSubcategoryService.findAll(
                "test", null, null, null, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Subcategory");
    }

    @Test
    void findAll_WithCategoryFilter_ShouldReturnFilteredSubcategories() {
        // Given - create another category and subcategory
        TransactionCategory anotherCategory = new TransactionCategory();
        anotherCategory.setName("Another Category");
        anotherCategory = transactionCategoryRepository.save(anotherCategory);

        TransactionSubcategory anotherSubcategory = new TransactionSubcategory();
        anotherSubcategory.setName("Another Subcategory");
        anotherSubcategory.setCategory(anotherCategory);
        anotherSubcategory.setIsActive(true);
        transactionSubcategoryRepository.save(anotherSubcategory);

        // When
        Page<TransactionSubcategoryDTO> result = transactionSubcategoryService.findAll(
                null, java.util.Map.of("categoryId", testCategory.getId()), null, null, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Subcategory");
        assertThat(result.getContent().get(0).getCategoryId()).isEqualTo(testCategory.getId());
    }

    @Test
    void create_WithNullName_ShouldThrowException() {
        // Given
        TransactionSubcategoryDTO createDTO = new TransactionSubcategoryDTO();
        createDTO.setName(null);
        createDTO.setCategoryId(testCategory.getId());

        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_WithNullCategoryId_ShouldThrowException() {
        // Given
        TransactionSubcategoryDTO createDTO = new TransactionSubcategoryDTO();
        createDTO.setName("Test Subcategory");
        createDTO.setCategoryId(null);

        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_WithSpecialCharacters_ShouldHandleCorrectly() {
        // Given
        TransactionSubcategoryDTO createDTO = new TransactionSubcategoryDTO();
        createDTO.setName("Subcategory with Special Characters: @#$%^&*()");
        createDTO.setDescription("Description with special chars: @#$%^&*()");
        createDTO.setCategoryId(testCategory.getId());

        // When
        TransactionSubcategoryDTO result = transactionSubcategoryService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Subcategory with Special Characters: @#$%^&*()");
        assertThat(result.getDescription()).isEqualTo("Description with special chars: @#$%^&*()");

        // Verify it was saved to database
        TransactionSubcategory savedSubcategory = transactionSubcategoryRepository.findById(result.getId()).orElse(null);
        assertThat(savedSubcategory).isNotNull();
        assertThat(savedSubcategory.getName()).isEqualTo("Subcategory with Special Characters: @#$%^&*()");
        assertThat(savedSubcategory.getDescription()).isEqualTo("Description with special chars: @#$%^&*()");
    }

    @Test
    void create_WithUnicodeCharacters_ShouldHandleCorrectly() {
        // Given
        TransactionSubcategoryDTO createDTO = new TransactionSubcategoryDTO();
        createDTO.setName("Subcategory with Unicode: 中文 العربية русский");
        createDTO.setDescription("Description with Unicode: 中文 العربية русский");
        createDTO.setCategoryId(testCategory.getId());

        // When
        TransactionSubcategoryDTO result = transactionSubcategoryService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Subcategory with Unicode: 中文 العربية русский");
        assertThat(result.getDescription()).isEqualTo("Description with Unicode: 中文 العربية русский");

        // Verify it was saved to database
        TransactionSubcategory savedSubcategory = transactionSubcategoryRepository.findById(result.getId()).orElse(null);
        assertThat(savedSubcategory).isNotNull();
        assertThat(savedSubcategory.getName()).isEqualTo("Subcategory with Unicode: 中文 العربية русский");
        assertThat(savedSubcategory.getDescription()).isEqualTo("Description with Unicode: 中文 العربية русский");
    }

    @Test
    void create_WithLongDescription_ShouldHandleCorrectly() {
        // Given
        String longDescription = "This is a very long description that tests the handling of long text in the subcategory description field. " +
                "It should be able to handle descriptions up to the maximum length allowed by the database schema. " +
                "This test ensures that the application can properly store and retrieve long descriptions without any issues.";

        TransactionSubcategoryDTO createDTO = new TransactionSubcategoryDTO();
        createDTO.setName("Subcategory with Long Description");
        createDTO.setDescription(longDescription);
        createDTO.setCategoryId(testCategory.getId());

        // When
        TransactionSubcategoryDTO result = transactionSubcategoryService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Subcategory with Long Description");
        assertThat(result.getDescription()).isEqualTo(longDescription);

        // Verify it was saved to database
        TransactionSubcategory savedSubcategory = transactionSubcategoryRepository.findById(result.getId()).orElse(null);
        assertThat(savedSubcategory).isNotNull();
        assertThat(savedSubcategory.getName()).isEqualTo("Subcategory with Long Description");
        assertThat(savedSubcategory.getDescription()).isEqualTo(longDescription);
    }
}
