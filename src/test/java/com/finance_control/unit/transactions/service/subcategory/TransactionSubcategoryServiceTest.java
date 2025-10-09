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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionSubcategoryServiceTest {

    @Mock
    private TransactionSubcategoryRepository transactionSubcategoryRepository;

    @Mock
    private TransactionCategoryRepository transactionCategoryRepository;

    @InjectMocks
    private TransactionSubcategoryService transactionSubcategoryService;

    private TransactionCategory testCategory;
    private TransactionSubcategory testSubcategory;
    private TransactionSubcategoryDTO testSubcategoryDTO;

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

        testSubcategoryDTO = new TransactionSubcategoryDTO();
        testSubcategoryDTO.setId(1L);
        testSubcategoryDTO.setName("Test Subcategory");
        testSubcategoryDTO.setDescription("Test Description");
        testSubcategoryDTO.setCategoryId(1L);
        testSubcategoryDTO.setIsActive(true);
        testSubcategoryDTO.setCreatedAt(LocalDateTime.now());
        testSubcategoryDTO.setUpdatedAt(LocalDateTime.now());
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
        when(transactionSubcategoryRepository.existsByCategoryIdAndNameIgnoreCase(1L, "New Subcategory")).thenReturn(false);
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
        verify(transactionSubcategoryRepository).existsByCategoryIdAndNameIgnoreCase(1L, "New Subcategory");
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

    @Test
    void create_WithDuplicateNameInCategory_ShouldThrowException() {
        // Given
        TransactionSubcategoryDTO createDTO = new TransactionSubcategoryDTO();
        createDTO.setName("Existing Subcategory");
        createDTO.setCategoryId(1L);

        when(transactionCategoryRepository.existsById(1L)).thenReturn(true);
        when(transactionSubcategoryRepository.existsByCategoryIdAndNameIgnoreCase(1L, "Existing Subcategory")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(transactionCategoryRepository).existsById(1L);
        verify(transactionSubcategoryRepository).existsByCategoryIdAndNameIgnoreCase(1L, "Existing Subcategory");
        verify(transactionSubcategoryRepository, never()).save(any(TransactionSubcategory.class));
    }

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
    void findById_WithNonExistingId_ShouldThrowException() {
        // Given
        when(transactionSubcategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionSubcategoryService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(transactionSubcategoryRepository).findById(999L);
    }

    @Test
    void findByCategoryId_WithValidCategoryId_ShouldReturnSubcategories() {
        // Given
        List<TransactionSubcategory> subcategories = List.of(testSubcategory);
        Page<TransactionSubcategory> page = new PageImpl<>(subcategories, PageRequest.of(0, 10), 1);

        when(transactionSubcategoryRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // When
        List<TransactionSubcategoryDTO> result = transactionSubcategoryService.findByCategoryId(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Subcategory");
        assertThat(result.get(0).getCategoryId()).isEqualTo(1L);

        verify(transactionSubcategoryRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
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
        when(transactionSubcategoryRepository.existsByCategoryIdAndNameIgnoreCase(1L, "Updated Subcategory")).thenReturn(false);
        when(transactionSubcategoryRepository.save(any(TransactionSubcategory.class))).thenReturn(testSubcategory);

        // When
        TransactionSubcategoryDTO result = transactionSubcategoryService.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(transactionSubcategoryRepository).findById(1L);
        verify(transactionSubcategoryRepository).existsByCategoryIdAndNameIgnoreCase(1L, "Updated Subcategory");
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

        when(transactionSubcategoryRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // When
        List<TransactionSubcategoryDTO> result = transactionSubcategoryService.findAllActive();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();

        verify(transactionSubcategoryRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }
}
