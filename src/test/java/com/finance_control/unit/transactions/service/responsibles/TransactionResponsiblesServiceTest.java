package com.finance_control.unit.transactions.service.responsibles;

import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.context.UserContext;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.repository.responsibles.TransactionResponsiblesRepository;
import com.finance_control.transactions.service.responsibles.TransactionResponsiblesService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("TransactionResponsiblesService Unit Tests")
class TransactionResponsiblesServiceTest {

    @Mock
    private TransactionResponsiblesRepository transactionResponsiblesRepository;

    @InjectMocks
    private TransactionResponsiblesService transactionResponsiblesService;

    private TransactionResponsibles testResponsible;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUserId(1L);

        testResponsible = new TransactionResponsibles();
        testResponsible.setId(1L);
        testResponsible.setName("Test Responsible");
        testResponsible.setCreatedAt(LocalDateTime.now());
        testResponsible.setUpdatedAt(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("create_WithValidDTO_ShouldReturnDTO")
    void create_WithValidDTO_ShouldReturnDTO() {
        TransactionResponsiblesDTO createDTO = new TransactionResponsiblesDTO();
        createDTO.setName("New Responsible");

        when(transactionResponsiblesRepository.existsByNameIgnoreCase("New Responsible")).thenReturn(false);
        when(transactionResponsiblesRepository.save(any(TransactionResponsibles.class))).thenReturn(testResponsible);

        TransactionResponsiblesDTO result = transactionResponsiblesService.create(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Responsible");

        verify(transactionResponsiblesRepository).existsByNameIgnoreCase("New Responsible");
        verify(transactionResponsiblesRepository).save(any(TransactionResponsibles.class));
    }

    @Test
    @DisplayName("create_WithDuplicateName_ShouldThrowException")
    void create_WithDuplicateName_ShouldThrowException() {
        TransactionResponsiblesDTO createDTO = new TransactionResponsiblesDTO();
        createDTO.setName("Existing Responsible");

        when(transactionResponsiblesRepository.existsByNameIgnoreCase("Existing Responsible")).thenReturn(true);

        assertThatThrownBy(() -> transactionResponsiblesService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(transactionResponsiblesRepository).existsByNameIgnoreCase("Existing Responsible");
        verify(transactionResponsiblesRepository, never()).save(any(TransactionResponsibles.class));
    }

    @Test
    @DisplayName("update_WithValidDTO_ShouldReturnUpdatedDTO")
    void update_WithValidDTO_ShouldReturnUpdatedDTO() {
        TransactionResponsiblesDTO updateDTO = new TransactionResponsiblesDTO();
        updateDTO.setName("Updated Responsible");

        when(transactionResponsiblesRepository.findById(1L)).thenReturn(Optional.of(testResponsible));
        when(transactionResponsiblesRepository.existsByNameIgnoreCase("Updated Responsible")).thenReturn(false);
        when(transactionResponsiblesRepository.save(any(TransactionResponsibles.class))).thenReturn(testResponsible);

        TransactionResponsiblesDTO result = transactionResponsiblesService.update(1L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(transactionResponsiblesRepository).findById(1L);
        verify(transactionResponsiblesRepository).existsByNameIgnoreCase("Updated Responsible");
        verify(transactionResponsiblesRepository).save(any(TransactionResponsibles.class));
    }

    @Test
    @DisplayName("findById_WithValidId_ShouldReturnDTO")
    void findById_WithValidId_ShouldReturnDTO() {
        when(transactionResponsiblesRepository.findById(1L)).thenReturn(Optional.of(testResponsible));

        Optional<TransactionResponsiblesDTO> result = transactionResponsiblesService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Test Responsible");

        verify(transactionResponsiblesRepository).findById(1L);
    }

    @Test
    @DisplayName("findById_WithInvalidId_ShouldThrowException")
    void findById_WithInvalidId_ShouldThrowException() {
        when(transactionResponsiblesRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<TransactionResponsiblesDTO> result = transactionResponsiblesService.findById(999L);

        assertThat(result).isEmpty();

        verify(transactionResponsiblesRepository).findById(999L);
    }

    @Test
    @DisplayName("delete_WithValidId_ShouldDeleteEntity")
    void delete_WithValidId_ShouldDeleteEntity() {
        when(transactionResponsiblesRepository.findById(1L)).thenReturn(Optional.of(testResponsible));
        doNothing().when(transactionResponsiblesRepository).deleteById(1L);

        transactionResponsiblesService.delete(1L);

        verify(transactionResponsiblesRepository).findById(1L);
        verify(transactionResponsiblesRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete_WithInvalidId_ShouldThrowException")
    void delete_WithInvalidId_ShouldThrowException() {
        when(transactionResponsiblesRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionResponsiblesService.delete(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(transactionResponsiblesRepository).findById(999L);
        verify(transactionResponsiblesRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("findAll_WithNoFilters_ShouldReturnAllEntities")
    void findAll_WithNoFilters_ShouldReturnAllEntities() {
        List<TransactionResponsibles> responsibles = List.of(testResponsible);
        Page<TransactionResponsibles> page = new PageImpl<>(responsibles, PageRequest.of(0, 10), 1);

        when(transactionResponsiblesRepository.findAll(eq((String) null), any(Pageable.class)))
                .thenReturn(page);

        Page<TransactionResponsiblesDTO> result = transactionResponsiblesService.findAll(null, null, null, null, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Responsible");

        verify(transactionResponsiblesRepository).findAll(eq((String) null), any(Pageable.class));
    }

    @Test
    @DisplayName("findAll_WithSearch_ShouldReturnFilteredEntities")
    void findAll_WithSearch_ShouldReturnFilteredEntities() {
        List<TransactionResponsibles> responsibles = List.of(testResponsible);
        Page<TransactionResponsibles> page = new PageImpl<>(responsibles, PageRequest.of(0, 10), 1);

        when(transactionResponsiblesRepository.findAll(anyString(), any(Pageable.class)))
                .thenReturn(page);

        Page<TransactionResponsiblesDTO> result = transactionResponsiblesService.findAll("test", null, null, null, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(transactionResponsiblesRepository).findAll(anyString(), any(Pageable.class));
    }
}
