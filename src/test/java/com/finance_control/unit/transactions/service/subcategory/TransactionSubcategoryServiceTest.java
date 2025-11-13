package com.finance_control.unit.transactions.service.subcategory;

import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.transactions.dto.subcategory.TransactionSubcategoryDTO;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.repository.subcategory.TransactionSubcategoryRepository;
import com.finance_control.transactions.service.subcategory.TransactionSubcategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class TransactionSubcategoryServiceTest {

    @Mock
    private TransactionSubcategoryRepository transactionSubcategoryRepository;

    @Mock
    private TransactionCategoryRepository transactionCategoryRepository;

    @InjectMocks
    private TransactionSubcategoryService transactionSubcategoryService;

    private TransactionCategory testCategory;
    private TransactionSubcategory testSubcategory;

    @BeforeEach
    void setUp() {
        testCategory = new TransactionCategory();
        testCategory.setId(1L);
        testCategory.setName("Test Category");

        testSubcategory = new TransactionSubcategory();
        testSubcategory.setId(1L);
        testSubcategory.setName("Test Subcategory");
        testSubcategory.setDescription("Test Description");
        testSubcategory.setCategory(testCategory);
        testSubcategory.setIsActive(true);
        testSubcategory.setCreatedAt(LocalDateTime.now());
        testSubcategory.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void create_WithValidData_ShouldReturnCreatedSubcategory() {
        // Given
        TransactionSubcategoryDTO createDTO = new TransactionSubcategoryDTO();
        createDTO.setName("New Subcategory");
        createDTO.setDescription("New Description");
        createDTO.setCategoryId(1L);

        when(transactionCategoryRepository.existsById(1L)).thenReturn(true);
        when(transactionCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionSubcategoryRepository.save(any(TransactionSubcategory.class))).thenReturn(testSubcategory);

        // When
        TransactionSubcategoryDTO result = transactionSubcategoryService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Subcategory");
        assertThat(result.getCategoryId()).isEqualTo(1L);

        verify(transactionCategoryRepository).existsById(1L);
        verify(transactionCategoryRepository).findById(1L);
        verify(transactionSubcategoryRepository).save(any(TransactionSubcategory.class));
    }

    @Test
    void create_WithNonExistingCategory_ShouldThrowException() {
        // Given
        TransactionSubcategoryDTO createDTO = new TransactionSubcategoryDTO();
        createDTO.setName("New Subcategory");
        createDTO.setCategoryId(999L);

        when(transactionCategoryRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.create(createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Transaction category not found");

        verify(transactionCategoryRepository).existsById(999L);
        verify(transactionSubcategoryRepository, never()).save(any(TransactionSubcategory.class));
    }

    // Note: Subcategories don't have duplicate name validation since they can have the same name in different categories

    @Test
    void findById_WithExistingId_ShouldReturnSubcategory() {
        // Given
        when(transactionSubcategoryRepository.findById(1L)).thenReturn(Optional.of(testSubcategory));

        // When
        Optional<TransactionSubcategoryDTO> result = transactionSubcategoryService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Test Subcategory");
        assertThat(result.get().getCategoryId()).isEqualTo(1L);

        verify(transactionSubcategoryRepository).findById(1L);
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // Given
        when(transactionSubcategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<TransactionSubcategoryDTO> result = transactionSubcategoryService.findById(999L);

        // Then
        assertThat(result).isEmpty();

        verify(transactionSubcategoryRepository).findById(999L);
    }

    @Test
    void findByCategoryId_WithValidCategoryId_ShouldReturnSubcategories() {
        // Given
        List<TransactionSubcategory> subcategories = List.of(testSubcategory);
        Page<TransactionSubcategory> page = new PageImpl<>(subcategories, PageRequest.of(0, 10), 1);

        doReturn(page).when(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));

        // When
        List<TransactionSubcategoryDTO> result = transactionSubcategoryService.findByCategoryId(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Subcategory");
        assertThat(result.get(0).getCategoryId()).isEqualTo(1L);

        verify(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }

    @Test
    void findByCategoryIdOrderByUsage_WithValidCategoryId_ShouldReturnOrderedSubcategories() {
        // Given
        List<TransactionSubcategory> subcategories = List.of(testSubcategory);

        when(transactionSubcategoryRepository.findByCategoryIdOrderByUsageAndName(1L)).thenReturn(subcategories);

        // When
        List<TransactionSubcategoryDTO> result = transactionSubcategoryService.findByCategoryIdOrderByUsage(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Subcategory");

        verify(transactionSubcategoryRepository).findByCategoryIdOrderByUsageAndName(1L);
    }

    @Test
    void countByCategoryId_WithValidCategoryId_ShouldReturnCount() {
        // Given
        when(transactionSubcategoryRepository.count(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(5L);

        // When
        long result = transactionSubcategoryService.countByCategoryId(1L);

        // Then
        assertThat(result).isEqualTo(5L);

        verify(transactionSubcategoryRepository).count(any(org.springframework.data.jpa.domain.Specification.class));
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedSubcategory() {
        // Given
        TransactionSubcategoryDTO updateDTO = new TransactionSubcategoryDTO();
        updateDTO.setName("Updated Subcategory");
        updateDTO.setDescription("Updated Description");

        when(transactionSubcategoryRepository.findById(1L)).thenReturn(Optional.of(testSubcategory));
        when(transactionSubcategoryRepository.save(any(TransactionSubcategory.class))).thenReturn(testSubcategory);

        // When
        TransactionSubcategoryDTO result = transactionSubcategoryService.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(transactionSubcategoryRepository).findById(1L);
        verify(transactionSubcategoryRepository).save(any(TransactionSubcategory.class));
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        // Given
        TransactionSubcategoryDTO updateDTO = new TransactionSubcategoryDTO();
        updateDTO.setName("Updated Subcategory");

        when(transactionSubcategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.update(999L, updateDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(transactionSubcategoryRepository).findById(999L);
        verify(transactionSubcategoryRepository, never()).save(any(TransactionSubcategory.class));
    }

    @Test
    void delete_WithExistingId_ShouldDeleteSubcategory() {
        // Given
        when(transactionSubcategoryRepository.findById(1L)).thenReturn(Optional.of(testSubcategory));
        doNothing().when(transactionSubcategoryRepository).deleteById(1L);

        // When
        transactionSubcategoryService.delete(1L);

        // Then
        verify(transactionSubcategoryRepository).findById(1L);
        verify(transactionSubcategoryRepository).deleteById(1L);
    }

    @Test
    void delete_WithNonExistingId_ShouldThrowException() {
        // Given
        when(transactionSubcategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.delete(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(transactionSubcategoryRepository).findById(999L);
        verify(transactionSubcategoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void create_WithNullName_ShouldThrowException() {
        // Given
        TransactionSubcategoryDTO createDTO = new TransactionSubcategoryDTO();
        createDTO.setName(null);
        createDTO.setCategoryId(1L);

        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class);

        verify(transactionSubcategoryRepository, never()).save(any(TransactionSubcategory.class));
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

        verify(transactionSubcategoryRepository, never()).save(any(TransactionSubcategory.class));
    }

    @Test
    void findAllActive_ShouldReturnActiveSubcategories() {
        // Given
        List<TransactionSubcategory> subcategories = List.of(testSubcategory);
        Page<TransactionSubcategory> page = new PageImpl<>(subcategories, PageRequest.of(0, 10), 1);

        doReturn(page).when(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));

        // When
        List<TransactionSubcategoryDTO> result = transactionSubcategoryService.findAllActive();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();

        verify(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }

    @Test
    void findByCategoryId_WithPagination_ShouldReturnPagedResults() {
        // Given
        List<TransactionSubcategory> subcategories = List.of(testSubcategory);
        Pageable pageable = PageRequest.of(0, 1);
        Page<TransactionSubcategory> fullPage = new PageImpl<>(subcategories, PageRequest.of(0, 10), 1);

        when(transactionSubcategoryRepository.findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(fullPage);

        // When
        Page<TransactionSubcategoryDTO> result = transactionSubcategoryService.findByCategoryId(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }

    @Test
    void findByCategoryIdOrderByUsage_WithPagination_ShouldReturnPagedResults() {
        // Given
        List<TransactionSubcategory> allSubcategories = List.of(testSubcategory);
        Pageable pageable = PageRequest.of(0, 1);

        when(transactionSubcategoryRepository.findByCategoryIdOrderByUsageAndName(1L))
                .thenReturn(allSubcategories);

        // When
        Page<TransactionSubcategoryDTO> result = transactionSubcategoryService.findByCategoryIdOrderByUsage(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        verify(transactionSubcategoryRepository).findByCategoryIdOrderByUsageAndName(1L);
    }

    @Test
    void findByCategoryIdOrderByUsage_WithPagination_ShouldHandleEmptyList() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        when(transactionSubcategoryRepository.findByCategoryIdOrderByUsageAndName(1L))
                .thenReturn(List.of());

        // When
        Page<TransactionSubcategoryDTO> result = transactionSubcategoryService.findByCategoryIdOrderByUsage(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0L);
    }

    @Test
    void findByCategoryIdAndName_WithValidData_ShouldReturnSubcategory() {
        // Given
        Page<TransactionSubcategory> page = new PageImpl<>(List.of(testSubcategory), PageRequest.of(0, 10), 1);

        doReturn(page).when(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));

        // When
        Optional<TransactionSubcategoryDTO> result = transactionSubcategoryService.findByCategoryIdAndName(1L, "Test Subcategory");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Subcategory");
        verify(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }

    @Test
    void findByCategoryIdAndName_WithNonExistingName_ShouldReturnEmpty() {
        // Given
        Page<TransactionSubcategory> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        doReturn(page).when(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));

        // When
        Optional<TransactionSubcategoryDTO> result = transactionSubcategoryService.findByCategoryIdAndName(1L, "Non Existent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByCategoryIdAndName_WithNullName_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.findByCategoryIdAndName(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void existsByCategoryIdAndName_WithExistingSubcategory_ShouldReturnTrue() {
        // Given
        when(transactionSubcategoryRepository.exists(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(true);

        // When
        boolean result = transactionSubcategoryService.existsByCategoryIdAndName(1L, "Test Subcategory");

        // Then
        assertThat(result).isTrue();
        verify(transactionSubcategoryRepository).exists(any(org.springframework.data.jpa.domain.Specification.class));
    }

    @Test
    void existsByCategoryIdAndName_WithNonExistingSubcategory_ShouldReturnFalse() {
        // Given
        when(transactionSubcategoryRepository.exists(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(false);

        // When
        boolean result = transactionSubcategoryService.existsByCategoryIdAndName(1L, "Non Existent");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void existsByCategoryIdAndName_WithNullName_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.existsByCategoryIdAndName(1L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getTotalCount_ShouldReturnCount() {
        // Given
        when(transactionSubcategoryRepository.count()).thenReturn(10L);

        // When
        Long result = transactionSubcategoryService.getTotalCount();

        // Then
        assertThat(result).isEqualTo(10L);
        verify(transactionSubcategoryRepository).count();
    }

    @Test
    void findAll_WithSearchTerm_ShouldFilterByNameAndDescription() {
        // Given
        Page<TransactionSubcategory> page = new PageImpl<>(List.of(testSubcategory), PageRequest.of(0, 10), 1);
        Pageable pageable = PageRequest.of(0, 10);

        when(transactionSubcategoryRepository.findAll(anyString(), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<TransactionSubcategoryDTO> result = transactionSubcategoryService.findAll("Test", null, null, null, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(transactionSubcategoryRepository).findAll(anyString(), any(Pageable.class));
    }

    @Test
    void findAll_WithFilterIsActive_ShouldFilterByActiveStatus() {
        // Given
        Page<TransactionSubcategory> page = new PageImpl<>(List.of(testSubcategory), PageRequest.of(0, 10), 1);
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, Object> filters = Map.of("isActive", true);

        doReturn(page).when(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));

        // When
        Page<TransactionSubcategoryDTO> result = transactionSubcategoryService.findAll(null, filters, null, null, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }

    @Test
    void findAll_WithFilterName_ShouldFilterByName() {
        // Given
        Page<TransactionSubcategory> page = new PageImpl<>(List.of(testSubcategory), PageRequest.of(0, 10), 1);
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, Object> filters = Map.of("name", "Test");

        doReturn(page).when(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));

        // When
        Page<TransactionSubcategoryDTO> result = transactionSubcategoryService.findAll(null, filters, null, null, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }

    @Test
    void findAll_WithFilterDescription_ShouldFilterByDescription() {
        // Given
        Page<TransactionSubcategory> page = new PageImpl<>(List.of(testSubcategory), PageRequest.of(0, 10), 1);
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, Object> filters = Map.of("description", "Test");

        doReturn(page).when(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));

        // When
        Page<TransactionSubcategoryDTO> result = transactionSubcategoryService.findAll(null, filters, null, null, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }

    @Test
    void findAll_WithUnknownFilterKey_ShouldIgnoreFilter() {
        // Given
        Page<TransactionSubcategory> page = new PageImpl<>(List.of(testSubcategory), PageRequest.of(0, 10), 1);
        Pageable pageable = PageRequest.of(0, 10);
        Map<String, Object> filters = Map.of("unknownKey", "value");

        doReturn(page).when(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));

        // When
        Page<TransactionSubcategoryDTO> result = transactionSubcategoryService.findAll(null, filters, null, null, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(transactionSubcategoryRepository).findAll((org.springframework.data.jpa.domain.Specification<TransactionSubcategory>) any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }

    @Test
    void findAll_WithEmptySearch_ShouldNotFilterBySearch() {
        // Given
        Page<TransactionSubcategory> page = new PageImpl<>(List.of(testSubcategory), PageRequest.of(0, 10), 1);
        Pageable pageable = PageRequest.of(0, 10);

        when(transactionSubcategoryRepository.findAll(anyString(), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<TransactionSubcategoryDTO> result = transactionSubcategoryService.findAll("   ", null, null, null, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        verify(transactionSubcategoryRepository).findAll(anyString(), any(Pageable.class));
    }


    @Test
    void getTransactionSubcategoryEntity_WithValidId_ShouldReturnEntity() {
        // Given
        when(transactionSubcategoryRepository.findById(1L)).thenReturn(Optional.of(testSubcategory));

        // When
        TransactionSubcategory result = transactionSubcategoryService.getTransactionSubcategoryEntity(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(transactionSubcategoryRepository).findById(1L);
    }

    @Test
    void getTransactionSubcategoryEntity_WithInvalidId_ShouldThrowException() {
        // Given
        when(transactionSubcategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.getTransactionSubcategoryEntity(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Transaction subcategory not found");
    }

    @Test
    void findByCategoryId_WithInvalidId_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.findByCategoryId(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void countByCategoryId_WithInvalidId_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.countByCategoryId(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void countByCategoryId_WithZero_ShouldReturnCount() {
        // Given
        when(transactionSubcategoryRepository.count(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(0L);

        // When
        long result = transactionSubcategoryService.countByCategoryId(1L);

        // Then
        assertThat(result).isEqualTo(0L);
    }
}
