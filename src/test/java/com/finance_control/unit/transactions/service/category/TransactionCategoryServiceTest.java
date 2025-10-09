package com.finance_control.unit.transactions.service.category;

import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.transactions.dto.category.TransactionCategoryDTO;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.service.category.TransactionCategoryService;
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
class TransactionCategoryServiceTest {

    @Mock
    private TransactionCategoryRepository transactionCategoryRepository;

    @InjectMocks
    private TransactionCategoryService transactionCategoryService;

    private TransactionCategory testCategory;
    private TransactionCategoryDTO testCategoryDTO;

    @BeforeEach
    void setUp() {
        testCategory = new TransactionCategory();
        testCategory.setId(1L);
        testCategory.setName("Test Category");
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());

        testCategoryDTO = new TransactionCategoryDTO();
        testCategoryDTO.setId(1L);
        testCategoryDTO.setName("Test Category");
        testCategoryDTO.setCreatedAt(LocalDateTime.now());
        testCategoryDTO.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void create_WithValidData_ShouldReturnCreatedCategory() {
        // Given
        TransactionCategoryDTO createDTO = new TransactionCategoryDTO();
        createDTO.setName("New Category");

        when(transactionCategoryRepository.existsByNameIgnoreCase("New Category")).thenReturn(false);
        when(transactionCategoryRepository.save(any(TransactionCategory.class))).thenReturn(testCategory);

        // When
        TransactionCategoryDTO result = transactionCategoryService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Category");

        verify(transactionCategoryRepository).existsByNameIgnoreCase("New Category");
        verify(transactionCategoryRepository).save(any(TransactionCategory.class));
    }

    @Test
    void create_WithDuplicateName_ShouldThrowException() {
        // Given
        TransactionCategoryDTO createDTO = new TransactionCategoryDTO();
        createDTO.setName("Existing Category");

        when(transactionCategoryRepository.existsByNameIgnoreCase("Existing Category")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> transactionCategoryService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(transactionCategoryRepository).existsByNameIgnoreCase("Existing Category");
        verify(transactionCategoryRepository, never()).save(any(TransactionCategory.class));
    }

    @Test
    void findById_WithExistingId_ShouldReturnCategory() {
        // Given
        when(transactionCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When
        Optional<TransactionCategoryDTO> result = transactionCategoryService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Test Category");

        verify(transactionCategoryRepository).findById(1L);
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowException() {
        // Given
        when(transactionCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<TransactionCategoryDTO> result = transactionCategoryService.findById(999L);

        // Then
        assertThat(result).isEmpty();

        verify(transactionCategoryRepository).findById(999L);
    }

    @Test
    void findAll_WithNoFilters_ShouldReturnAllCategories() {
        // Given
        List<TransactionCategory> categories = List.of(testCategory);
        Page<TransactionCategory> page = new PageImpl<>(categories, PageRequest.of(0, 10), 1);

        when(transactionCategoryRepository.findAll(eq((String) null), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<TransactionCategoryDTO> result = transactionCategoryService.findAll(null, null, null, null, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Category");

        verify(transactionCategoryRepository).findAll(eq((String) null), any(Pageable.class));
    }

    @Test
    void findAll_WithSearch_ShouldReturnFilteredCategories() {
        // Given
        List<TransactionCategory> categories = List.of(testCategory);
        Page<TransactionCategory> page = new PageImpl<>(categories, PageRequest.of(0, 10), 1);

        when(transactionCategoryRepository.findAll(anyString(), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<TransactionCategoryDTO> result = transactionCategoryService.findAll("test", null, null, null, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(transactionCategoryRepository).findAll(anyString(), any(Pageable.class));
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedCategory() {
        // Given
        TransactionCategoryDTO updateDTO = new TransactionCategoryDTO();
        updateDTO.setName("Updated Category");

        when(transactionCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionCategoryRepository.existsByNameIgnoreCase("Updated Category")).thenReturn(false);
        when(transactionCategoryRepository.save(any(TransactionCategory.class))).thenReturn(testCategory);

        // When
        TransactionCategoryDTO result = transactionCategoryService.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(transactionCategoryRepository).findById(1L);
        verify(transactionCategoryRepository).existsByNameIgnoreCase("Updated Category");
        verify(transactionCategoryRepository).save(any(TransactionCategory.class));
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        // Given
        TransactionCategoryDTO updateDTO = new TransactionCategoryDTO();
        updateDTO.setName("Updated Category");

        when(transactionCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionCategoryService.update(999L, updateDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(transactionCategoryRepository).findById(999L);
        verify(transactionCategoryRepository, never()).save(any(TransactionCategory.class));
    }

    @Test
    void update_WithDuplicateName_ShouldThrowException() {
        // Given
        TransactionCategoryDTO updateDTO = new TransactionCategoryDTO();
        updateDTO.setName("Existing Category");

        when(transactionCategoryRepository.existsByNameIgnoreCase("Existing Category")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> transactionCategoryService.update(1L, updateDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(transactionCategoryRepository).existsByNameIgnoreCase("Existing Category");
        verify(transactionCategoryRepository, never()).findById(anyLong());
        verify(transactionCategoryRepository, never()).save(any(TransactionCategory.class));
    }

    @Test
    void delete_WithExistingId_ShouldDeleteCategory() {
        // Given
        when(transactionCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        doNothing().when(transactionCategoryRepository).deleteById(1L);

        // When
        transactionCategoryService.delete(1L);

        // Then
        verify(transactionCategoryRepository).findById(1L);
        verify(transactionCategoryRepository).deleteById(1L);
    }

    @Test
    void delete_WithNonExistingId_ShouldThrowException() {
        // Given
        when(transactionCategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionCategoryService.delete(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(transactionCategoryRepository).findById(999L);
        verify(transactionCategoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void create_WithNullName_ShouldThrowException() {
        // Given
        TransactionCategoryDTO createDTO = new TransactionCategoryDTO();
        createDTO.setName(null);

        // When & Then
        assertThatThrownBy(() -> transactionCategoryService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class);

        verify(transactionCategoryRepository, never()).save(any(TransactionCategory.class));
    }

    @Test
    void create_WithEmptyName_ShouldThrowException() {
        // Given
        TransactionCategoryDTO createDTO = new TransactionCategoryDTO();
        createDTO.setName("");

        // When & Then
        assertThatThrownBy(() -> transactionCategoryService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class);

        verify(transactionCategoryRepository, never()).save(any(TransactionCategory.class));
    }
}
