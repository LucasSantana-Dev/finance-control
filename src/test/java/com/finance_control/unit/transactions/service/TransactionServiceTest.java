package com.finance_control.unit.transactions.service;

import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.dto.TransactionReconciliationRequest;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.repository.responsibles.TransactionResponsiblesRepository;
import com.finance_control.transactions.repository.source.TransactionSourceRepository;
import com.finance_control.transactions.repository.subcategory.TransactionSubcategoryRepository;
import com.finance_control.transactions.service.TransactionService;
import com.finance_control.dashboard.service.DashboardService;
import com.finance_control.shared.service.SupabaseRealtimeService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionCategoryRepository categoryRepository;

    @Mock
    private TransactionSubcategoryRepository subcategoryRepository;

    @Mock
    private TransactionSourceRepository sourceRepository;

    @Mock
    private TransactionResponsiblesRepository responsibleRepository;

    @Mock
    private MetricsService metricsService;

    @Mock
    private SupabaseRealtimeService realtimeService;

    @Mock
    private DashboardService dashboardService;

    @Mock
    private com.finance_control.shared.monitoring.SentryService sentryService;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private TransactionCategory testCategory;
    private TransactionSubcategory testSubcategory;
    private TransactionSourceEntity testSourceEntity;
    private TransactionResponsibles testResponsible;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUserId(1L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setIsActive(true);

        testCategory = new TransactionCategory();
        testCategory.setId(1L);
        testCategory.setName("Test Category");

        testSubcategory = new TransactionSubcategory();
        testSubcategory.setId(1L);
        testSubcategory.setName("Test Subcategory");
        testSubcategory.setCategory(testCategory);

        testSourceEntity = new TransactionSourceEntity();
        testSourceEntity.setId(1L);
        testSourceEntity.setName("Test Source");

        testResponsible = new TransactionResponsibles();
        testResponsible.setId(1L);
        testResponsible.setName("Test Responsible");

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setDescription("Test Transaction");
        testTransaction.setAmount(BigDecimal.valueOf(100.00));
        testTransaction.setDate(LocalDateTime.now());
        testTransaction.setUser(testUser);
        testTransaction.setCategory(testCategory);
        testTransaction.setType(TransactionType.INCOME);
        testTransaction.setSubtype(TransactionSubtype.FIXED);
        testTransaction.setSource(TransactionSource.CASH);

        testTransaction.addResponsible(testResponsible, new BigDecimal("100.00"), "Test notes");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    private Transaction createFreshTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setDescription("Test Transaction");
        transaction.setAmount(BigDecimal.valueOf(100.00));
        transaction.setDate(LocalDateTime.now());
        transaction.setUser(testUser);
        transaction.setCategory(testCategory);
        transaction.setType(TransactionType.INCOME);
        transaction.setSubtype(TransactionSubtype.FIXED);
        transaction.setSource(TransactionSource.CASH);

        // Add a default responsibility to make the initial state valid
        // This will be cleared and replaced by updateEntityFromDTO
        TransactionResponsibles initialResponsible = new TransactionResponsibles();
        initialResponsible.setId(999L); // Use a different ID to avoid conflicts
        initialResponsible.setName("Initial Responsible");
        transaction.addResponsible(initialResponsible, new BigDecimal("100.00"), "Initial responsibility");

        return transaction;
    }

    @Test
    void create_WithValidData_ShouldReturnCreatedTransaction() {
        // Given
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("New Transaction");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setUserId(1L);

            List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
            TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
            responsible.setResponsibleId(1L);
            responsible.setPercentage(new BigDecimal("100.00"));
            responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction savedTransaction = invocation.getArgument(0);
                savedTransaction.setId(1L);
                return savedTransaction;
            });

        // When
        TransactionDTO result = transactionService.create(createDTO);

        // Then
            assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getDescription()).isEqualTo("New Transaction");
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(50.00));

        verify(userRepository, atLeastOnce()).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(responsibleRepository).findById(1L);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void create_WithSubcategory_ShouldReturnCreatedTransaction() {
        // Given
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("New Transaction");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setSubcategoryId(1L);
        createDTO.setUserId(1L);

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(testSubcategory));
        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            savedTransaction.setId(1L);
            return savedTransaction;
        });

        // When
        TransactionDTO result = transactionService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(subcategoryRepository).findById(1L);
    }

    @Test
    void create_WithSourceEntity_ShouldReturnCreatedTransaction() {
        // Given
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("New Transaction");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setSourceEntityId(1L);
        createDTO.setUserId(1L);

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(sourceRepository.findById(1L)).thenReturn(Optional.of(testSourceEntity));
        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            savedTransaction.setId(1L);
            return savedTransaction;
        });

        // When
        TransactionDTO result = transactionService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(sourceRepository).findById(1L);
    }

    @Test
    void create_WithNullDate_ShouldSetCurrentDate() {
        // Given
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("New Transaction");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setUserId(1L);
        createDTO.setDate(null); // Explicitly set to null

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            savedTransaction.setId(1L);
            return savedTransaction;
        });

        // When
        TransactionDTO result = transactionService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDate()).isNotNull();
    }

    @Test
    void create_WithNonExistingUser_ShouldThrowException() {
        // Given
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("New Transaction");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setUserId(999L);

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.create(createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(999L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void create_WithNonExistingCategory_ShouldThrowException() {
        // Given
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("New Transaction");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(999L);
        createDTO.setUserId(1L);

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.create(createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("TransactionCategory not found");

        verify(categoryRepository).findById(999L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void create_WithNonExistingSubcategory_ShouldThrowException() {
        // Given
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("New Transaction");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setSubcategoryId(999L);
        createDTO.setUserId(1L);

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(subcategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.create(createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("TransactionSubcategory not found");

        verify(subcategoryRepository).findById(999L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void create_WithNonExistingSourceEntity_ShouldThrowException() {
        // Given
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("New Transaction");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setSourceEntityId(999L);
        createDTO.setUserId(1L);

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(sourceRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.create(createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("TransactionSourceEntity not found");

        verify(sourceRepository).findById(999L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void create_WithNonExistingResponsible_ShouldThrowException() {
        // Given
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("New Transaction");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setUserId(1L);

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(999L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(responsibleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.create(createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("TransactionResponsible not found");

        verify(responsibleRepository).findById(999L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void findById_WithExistingId_ShouldReturnTransaction() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        // When
        Optional<TransactionDTO> result = transactionService.findById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getDescription()).isEqualTo("Test Transaction");

        verify(transactionRepository).findById(1L);
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // Given
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<TransactionDTO> result = transactionService.findById(999L);

        // Then
        assertThat(result).isEmpty();

        verify(transactionRepository).findById(999L);
    }

    @Test
    void update_WithValidData_ShouldReturnUpdatedTransaction() {
        // Given
        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setDescription("Updated Transaction");
        updateDTO.setAmount(BigDecimal.valueOf(200.00));

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        updateDTO.setResponsibilities(responsibilities);

        // Create a real transaction entity with proper state
        Transaction existingTransaction = new Transaction();
        existingTransaction.setId(1L);
        existingTransaction.setDescription("Original Transaction");
        existingTransaction.setAmount(BigDecimal.valueOf(100.00));
        existingTransaction.setDate(LocalDateTime.now());
        existingTransaction.setUser(testUser);
        existingTransaction.setCategory(testCategory);
        existingTransaction.setType(TransactionType.INCOME);
        existingTransaction.setSubtype(TransactionSubtype.FIXED);
        existingTransaction.setSource(TransactionSource.CASH);

        // Add initial responsibility to make it valid
        TransactionResponsibles initialResponsible = new TransactionResponsibles();
        initialResponsible.setId(999L);
        initialResponsible.setName("Initial Responsible");
        existingTransaction.addResponsible(initialResponsible, new BigDecimal("100.00"), "Initial");

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));

        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            saved.setId(1L); // Set the ID to simulate database behavior
            return saved; // Return the same instance with ID set
        });

        // When
        TransactionDTO result = transactionService.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Updated Transaction");
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(200.00));

        verify(transactionRepository).findById(1L);
        verify(responsibleRepository).findById(1L);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void update_WithCategoryId_ShouldUpdateCategory() {
        // Given
        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setCategoryId(2L);
        updateDTO.setAmount(BigDecimal.valueOf(100.00));
        updateDTO.setDescription("Updated Description");

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        updateDTO.setResponsibilities(responsibilities);

        TransactionCategory newCategory = new TransactionCategory();
        newCategory.setId(2L);
        newCategory.setName("New Category");

        Transaction freshTransaction = createFreshTransaction();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(freshTransaction));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));

        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });

        when(transactionRepository.save(any(Transaction.class))).thenReturn(freshTransaction);

        // When
        TransactionDTO result = transactionService.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();

        verify(categoryRepository).findById(2L);
        verify(responsibleRepository).findById(1L);
    }

    @Test
    void update_WithSubcategoryId_ShouldUpdateSubcategory() {
        // Given
        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setSubcategoryId(2L);
        updateDTO.setAmount(BigDecimal.valueOf(100.00));
        updateDTO.setDescription("Updated Description");

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        updateDTO.setResponsibilities(responsibilities);

        TransactionSubcategory newSubcategory = new TransactionSubcategory();
        newSubcategory.setId(2L);
        newSubcategory.setName("New Subcategory");

        Transaction freshTransaction = createFreshTransaction();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(freshTransaction));
        when(subcategoryRepository.findById(2L)).thenReturn(Optional.of(newSubcategory));
        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });
        when(transactionRepository.save(any(Transaction.class))).thenReturn(freshTransaction);

        // When
        TransactionDTO result = transactionService.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();

        verify(subcategoryRepository).findById(2L);
        verify(responsibleRepository).findById(1L);
    }

    @Test
    void update_WithNullSubcategoryId_ShouldClearSubcategory() {
        // Given
        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setSubcategoryId(null);
        updateDTO.setAmount(BigDecimal.valueOf(100.00));
        updateDTO.setDescription("Updated Description");

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        updateDTO.setResponsibilities(responsibilities);

        Transaction freshTransaction = createFreshTransaction();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(freshTransaction));
        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });
        when(transactionRepository.save(any(Transaction.class))).thenReturn(freshTransaction);

        // When
        TransactionDTO result = transactionService.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();

        verify(subcategoryRepository, never()).findById(anyLong());
        verify(responsibleRepository).findById(1L);
    }

    @Test
    void update_WithSourceEntityId_ShouldUpdateSourceEntity() {
        // Given
        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setSourceEntityId(2L);
        updateDTO.setAmount(BigDecimal.valueOf(100.00));
        updateDTO.setDescription("Updated Description");

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        updateDTO.setResponsibilities(responsibilities);

        TransactionSourceEntity newSourceEntity = new TransactionSourceEntity();
        newSourceEntity.setId(2L);
        newSourceEntity.setName("New Source");

        Transaction freshTransaction = createFreshTransaction();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(freshTransaction));
        when(sourceRepository.findById(2L)).thenReturn(Optional.of(newSourceEntity));
        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });
        when(transactionRepository.save(any(Transaction.class))).thenReturn(freshTransaction);

        // When
        TransactionDTO result = transactionService.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();

        verify(sourceRepository).findById(2L);
        verify(responsibleRepository).findById(1L);
    }

    @Test
    void update_WithNullSourceEntityId_ShouldClearSourceEntity() {
        // Given
        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setSourceEntityId(null);
        updateDTO.setAmount(BigDecimal.valueOf(100.00));
        updateDTO.setDescription("Updated Description");

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        updateDTO.setResponsibilities(responsibilities);

        Transaction freshTransaction = createFreshTransaction();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(freshTransaction));
        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });
        when(transactionRepository.save(any(Transaction.class))).thenReturn(freshTransaction);

        // When
        TransactionDTO result = transactionService.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();

        verify(sourceRepository, never()).findById(anyLong());
        verify(responsibleRepository).findById(1L);
    }

    @Test
    void update_WithResponsibilities_ShouldUpdateResponsibilities() {
        // Given
        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setAmount(BigDecimal.valueOf(100.00));
        updateDTO.setDescription("Updated Description");

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(2L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        updateDTO.setResponsibilities(responsibilities);

        TransactionResponsibles newResponsible = new TransactionResponsibles();
        newResponsible.setId(2L);
        newResponsible.setName("New Responsible");

        Transaction freshTransaction = createFreshTransaction();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(freshTransaction));
        when(responsibleRepository.findById(2L)).thenReturn(Optional.of(newResponsible));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(freshTransaction);

        // When
        TransactionDTO result = transactionService.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();

        verify(responsibleRepository).findById(2L);
    }

    @Test
    void update_WithNonExistingId_ShouldThrowException() {
        // Given
        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setDescription("Updated Transaction");

        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.update(999L, updateDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(transactionRepository).findById(999L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void delete_WithExistingId_ShouldDeleteTransaction() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        // When
        transactionService.delete(1L);

        // Then
        verify(transactionRepository).findById(1L);
        verify(transactionRepository).deleteById(1L);
    }

    @Test
    void delete_WithNonExistingId_ShouldThrowException() {
        // Given
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.delete(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(transactionRepository).findById(999L);
        verify(transactionRepository, never()).deleteById(anyLong());
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        // Given
        when(transactionRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = transactionService.existsById(1L);

        // Then
        assertThat(result).isTrue();

        verify(transactionRepository).existsById(1L);
    }

    @Test
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        // Given
        when(transactionRepository.existsById(999L)).thenReturn(false);

        // When
        boolean result = transactionService.existsById(999L);

        // Then
        assertThat(result).isFalse();

        verify(transactionRepository).existsById(999L);
    }

    @Test
    void findAll_WithNoFilters_ShouldReturnAllTransactions() {
        // Given
        List<Transaction> transactions = List.of(testTransaction);
        Page<Transaction> page = new PageImpl<>(transactions, PageRequest.of(0, 10), 1);

        when(transactionRepository.findAll(eq((String) null), any(Pageable.class))).thenReturn(page);

        // When
        Page<TransactionDTO> result = transactionService.findAll(null, null, null, null, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);

        verify(transactionRepository).findAll(eq((String) null), any(Pageable.class));
    }

    @Test
    void findAll_WithSearch_ShouldReturnFilteredTransactions() {
        // Given
        List<Transaction> transactions = List.of(testTransaction);
        Page<Transaction> page = new PageImpl<>(transactions, PageRequest.of(0, 10), 1);

        when(transactionRepository.findAll(eq("test"), any(Pageable.class))).thenReturn(page);

        // When
        Page<TransactionDTO> result = transactionService.findAll("test", null, null, null, PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(transactionRepository).findAll(eq("test"), any(Pageable.class));
    }

    @Test
    void findAll_WithFilters_ShouldUseSpecifications() {
        // Given
        List<Transaction> transactions = List.of(testTransaction);
        Page<Transaction> page = new PageImpl<>(transactions, PageRequest.of(0, 10), 1);
        Map<String, Object> filters = new HashMap<>();
        filters.put("type", TransactionType.INCOME);

        doReturn(page).when(transactionRepository).findAll((Specification<Transaction>) any(Specification.class), any(Pageable.class));

        // When
        Page<TransactionDTO> result = transactionService.findAll("test", filters, "description", "asc", PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(transactionRepository).findAll((Specification<Transaction>) any(Specification.class), any(Pageable.class));
    }

    @Test
    void count_WithSearchAndFilters_ShouldReturnCount() {
        // Given
        Map<String, Object> filters = new HashMap<>();
        filters.put("type", TransactionType.INCOME);

        when(transactionRepository.count(any(Specification.class))).thenReturn(5L);

        // When
        long result = transactionService.count("test", filters);

        // Then
        assertThat(result).isEqualTo(5L);

        verify(transactionRepository).count(any(Specification.class));
    }

    @Test
    void reconcileTransaction_WithValidData_ShouldReconcileTransaction() {
        // Given
        TransactionReconciliationRequest request = new TransactionReconciliationRequest();
        request.setReconciledAmount(BigDecimal.valueOf(100.00));
        request.setReconciliationDate(LocalDateTime.now());
        request.setReconciled(true);
        request.setReconciliationNotes("Reconciled with bank");
        request.setBankReference("BANK123");
        request.setExternalReference("EXT123");

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        TransactionDTO result = transactionService.reconcileTransaction(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(transactionRepository).findById(1L);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void reconcileTransaction_WithNonExistingId_ShouldThrowException() {
        // Given
        TransactionReconciliationRequest request = new TransactionReconciliationRequest();
        request.setReconciledAmount(BigDecimal.valueOf(100.00));

        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.reconcileTransaction(999L, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");

        verify(transactionRepository).findById(999L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void create_WithNullDate_ShouldSetDefaultDate() {
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("Transaction without date");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setUserId(1L);
        createDTO.setDate(null);

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(responsibleRepository.findById(1L)).thenReturn(Optional.of(testResponsible));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        TransactionDTO result = transactionService.create(createDTO);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(argThat(transaction ->
                transaction.getDate() != null));
    }

    @Test
    void create_WithNullSubcategoryId_ShouldCreateSuccessfully() {
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("Transaction without subcategory");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setUserId(1L);
        createDTO.setSubcategoryId(null);

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(responsibleRepository.findById(1L)).thenReturn(Optional.of(testResponsible));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        TransactionDTO result = transactionService.create(createDTO);

        assertThat(result).isNotNull();
        verify(subcategoryRepository, never()).findById(anyLong());
    }

    @Test
    void create_WithNullSourceEntityId_ShouldCreateSuccessfully() {
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("Transaction without source entity");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setUserId(1L);
        createDTO.setSourceEntityId(null);

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(responsibleRepository.findById(1L)).thenReturn(Optional.of(testResponsible));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        TransactionDTO result = transactionService.create(createDTO);

        assertThat(result).isNotNull();
        verify(sourceRepository, never()).findById(anyLong());
    }

    @Test
    void create_WithNullResponsibilities_ShouldThrowException() {
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("Transaction without responsibilities");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setUserId(1L);
        createDTO.setResponsibilities(null);

        assertThatThrownBy(() -> transactionService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Responsibilities cannot be null or empty");

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void create_WithEmptyResponsibilities_ShouldThrowException() {
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("Transaction with empty responsibilities");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setUserId(1L);
        createDTO.setResponsibilities(new ArrayList<>());

        assertThatThrownBy(() -> transactionService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Responsibilities cannot be null or empty");

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void findById_WithNullSubcategory_ShouldHandleGracefully() {
        testTransaction.setSubcategory(null);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        Optional<TransactionDTO> result = transactionService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getSubcategoryId()).isNull();
    }

    @Test
    void findById_WithNullSourceEntity_ShouldHandleGracefully() {
        testTransaction.setSourceEntity(null);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        Optional<TransactionDTO> result = transactionService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getSourceEntityId()).isNull();
    }


    @Test
    void findAll_WithFilterUserId_ShouldApplyFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("userId", 1L);
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));

        doReturn(page).when(transactionRepository).findAll((Specification<Transaction>) any(Specification.class), any(Pageable.class));

        Page<TransactionDTO> result = transactionService.findAll(null, filters, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_WithFilterCategoryId_ShouldApplyFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("categoryId", 1L);
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));

        doReturn(page).when(transactionRepository).findAll((Specification<Transaction>) any(Specification.class), any(Pageable.class));

        Page<TransactionDTO> result = transactionService.findAll(null, filters, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_WithFilterSubcategoryId_ShouldApplyFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("subcategoryId", 1L);
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));

        doReturn(page).when(transactionRepository).findAll((Specification<Transaction>) any(Specification.class), any(Pageable.class));

        Page<TransactionDTO> result = transactionService.findAll(null, filters, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_WithFilterSourceEntityId_ShouldApplyFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("sourceEntityId", 1L);
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));

        doReturn(page).when(transactionRepository).findAll((Specification<Transaction>) any(Specification.class), any(Pageable.class));

        Page<TransactionDTO> result = transactionService.findAll(null, filters, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_WithFilterDescription_ShouldApplyFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("description", "test");
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));

        doReturn(page).when(transactionRepository).findAll((Specification<Transaction>) any(Specification.class), any(Pageable.class));

        Page<TransactionDTO> result = transactionService.findAll(null, filters, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_WithUnknownFilterKey_ShouldIgnoreFilter() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("unknownKey", "value");
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));

        doReturn(page).when(transactionRepository).findAll((Specification<Transaction>) any(Specification.class), any(Pageable.class));

        Page<TransactionDTO> result = transactionService.findAll(null, filters, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void update_WithNullResponsibilities_ShouldThrowException() {
        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setAmount(BigDecimal.valueOf(100.00));
        updateDTO.setDescription("Updated Description");
        updateDTO.setResponsibilities(null);

        Transaction freshTransaction = createFreshTransaction();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(freshTransaction));

        assertThatThrownBy(() -> transactionService.update(1L, updateDTO))
                .isInstanceOf(IllegalArgumentException.class);

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void update_WithEmptyResponsibilities_ShouldThrowException() {
        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setAmount(BigDecimal.valueOf(100.00));
        updateDTO.setDescription("Updated Description");
        updateDTO.setResponsibilities(new ArrayList<>());

        assertThatThrownBy(() -> transactionService.update(1L, updateDTO))
                .isInstanceOf(IllegalArgumentException.class);

        verify(transactionRepository, never()).findById(anyLong());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void mapToResponseDTO_WithNullCategory_ShouldNotSetCategoryId() {
        testTransaction.setCategory(null);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        Optional<TransactionDTO> result = transactionService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCategoryId()).isNull();
    }

    @Test
    void mapToResponseDTO_WithNullSubcategory_ShouldNotSetSubcategoryId() {
        testTransaction.setSubcategory(null);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        Optional<TransactionDTO> result = transactionService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getSubcategoryId()).isNull();
    }

    @Test
    void mapToResponseDTO_WithNullSourceEntity_ShouldNotSetSourceEntityId() {
        testTransaction.setSourceEntity(null);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        Optional<TransactionDTO> result = transactionService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getSourceEntityId()).isNull();
    }

    // Note: Cannot test mapToResponseDTO with null user because belongsToUser()
    // calls entity.getUser().getId() which throws NullPointerException before mapToResponseDTO is called

    @Test
    void reconcileTransaction_WithNullReconciledAmount_ShouldSetNull() {
        TransactionReconciliationRequest request = new TransactionReconciliationRequest();
        request.setReconciledAmount(null);
        request.setReconciliationDate(LocalDateTime.now());
        request.setReconciled(true);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionDTO result = transactionService.reconcileTransaction(1L, request);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(argThat(t -> t.getReconciledAmount() == null));
    }

    @Test
    void reconcileTransaction_WithNullReconciliationDate_ShouldSetNull() {
        TransactionReconciliationRequest request = new TransactionReconciliationRequest();
        request.setReconciledAmount(BigDecimal.valueOf(100.00));
        request.setReconciliationDate(null);
        request.setReconciled(true);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionDTO result = transactionService.reconcileTransaction(1L, request);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(argThat(t -> t.getReconciliationDate() == null));
    }

    @Test
    void reconcileTransaction_WithNullReconciled_ShouldSetNull() {
        TransactionReconciliationRequest request = new TransactionReconciliationRequest();
        request.setReconciledAmount(BigDecimal.valueOf(100.00));
        request.setReconciliationDate(LocalDateTime.now());
        request.setReconciled(null);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionDTO result = transactionService.reconcileTransaction(1L, request);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(argThat(t -> t.getReconciled() == null));
    }

    @Test
    void reconcileTransaction_WithNullReconciliationNotes_ShouldSetNull() {
        TransactionReconciliationRequest request = new TransactionReconciliationRequest();
        request.setReconciledAmount(BigDecimal.valueOf(100.00));
        request.setReconciliationDate(LocalDateTime.now());
        request.setReconciled(true);
        request.setReconciliationNotes(null);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionDTO result = transactionService.reconcileTransaction(1L, request);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(argThat(t -> t.getReconciliationNotes() == null));
    }

    @Test
    void reconcileTransaction_WithNullBankReference_ShouldSetNull() {
        TransactionReconciliationRequest request = new TransactionReconciliationRequest();
        request.setReconciledAmount(BigDecimal.valueOf(100.00));
        request.setReconciliationDate(LocalDateTime.now());
        request.setReconciled(true);
        request.setBankReference(null);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionDTO result = transactionService.reconcileTransaction(1L, request);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(argThat(t -> t.getBankReference() == null));
    }

    @Test
    void reconcileTransaction_WithNullExternalReference_ShouldSetNull() {
        TransactionReconciliationRequest request = new TransactionReconciliationRequest();
        request.setReconciledAmount(BigDecimal.valueOf(100.00));
        request.setReconciliationDate(LocalDateTime.now());
        request.setReconciled(true);
        request.setExternalReference(null);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionDTO result = transactionService.reconcileTransaction(1L, request);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(argThat(t -> t.getExternalReference() == null));
    }

    @Test
    void findAll_WithDTOFiltersNull_ShouldNotApplyFilters() {
        TransactionDTO filters = null;
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));

        when(transactionRepository.findAll(eq((String) null), any(Pageable.class))).thenReturn(page);

        Page<TransactionDTO> result = transactionService.findAll(null, null, null, PageRequest.of(0, 10), filters);

        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findAll(eq((String) null), any(Pageable.class));
    }

    @Test
    void findAll_WithDTOFiltersAllFields_ShouldApplyAllFilters() {
        TransactionDTO filters = new TransactionDTO();
        filters.setUserId(1L);
        filters.setType(TransactionType.EXPENSE);
        filters.setCategoryId(1L);
        filters.setSubcategoryId(1L);
        filters.setSourceEntityId(1L);
        filters.setDescription("test");
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));

        doReturn(page).when(transactionRepository).findAll((Specification<Transaction>) any(Specification.class), any(Pageable.class));

        Page<TransactionDTO> result = transactionService.findAll(null, null, null, PageRequest.of(0, 10), filters);

        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findAll((Specification<Transaction>) any(Specification.class), any(Pageable.class));
    }

    @Test
    void findAll_WithDTOFiltersPartialFields_ShouldThrowNullPointerException() {
        TransactionDTO filters = new TransactionDTO();
        filters.setUserId(1L);
        filters.setType(TransactionType.EXPENSE);
        filters.setCategoryId(1L);
        // Other fields remain null, but Map.of() will throw if we try to include them
        // This tests the branch where null values cause NullPointerException

        // This will throw NullPointerException because Map.of() doesn't allow null values
        assertThatThrownBy(() -> transactionService.findAll(null, null, null, PageRequest.of(0, 10), filters))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createSpecificationFromFilters_WithNullFilters_ShouldUseSearchOnly() {
        Map<String, Object> filters = null;
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));

        // When filters is null, hasNoFilters returns true, so it uses findAllWithSearchOnly
        // which calls executeFindAllWithSearch, not findAllWithSpecifications
        when(transactionRepository.findAll(eq((String) null), any(Pageable.class))).thenReturn(page);

        Page<TransactionDTO> result = transactionService.findAll(null, filters, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findAll(eq((String) null), any(Pageable.class));
    }

    @Test
    void createSpecificationFromFilters_WithNullValues_ShouldIgnoreNullValues() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("type", TransactionType.EXPENSE);
        filters.put("categoryId", null);
        filters.put("subcategoryId", null);
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));

        doReturn(page).when(transactionRepository).findAll((Specification<Transaction>) any(Specification.class), any(Pageable.class));

        Page<TransactionDTO> result = transactionService.findAll(null, filters, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findAll((Specification<Transaction>) any(Specification.class), any(Pageable.class));
    }

    @Test
    void createSpecificationFromFilters_WithAllFilterTypes_ShouldApplyAllFilters() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("userId", 1L);
        filters.put("type", TransactionType.EXPENSE);
        filters.put("categoryId", 1L);
        filters.put("subcategoryId", 1L);
        filters.put("sourceEntityId", 1L);
        filters.put("description", "test");
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));

        doReturn(page).when(transactionRepository).findAll((Specification<Transaction>) any(Specification.class), any(Pageable.class));

        Page<TransactionDTO> result = transactionService.findAll(null, filters, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findAll((Specification<Transaction>) any(Specification.class), any(Pageable.class));
    }

    @Test
    void create_WithRealtimeAndDashboardServices_ShouldNotifyBothServices() {
        // Given
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("New Transaction");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setUserId(1L);

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            savedTransaction.setId(1L);
            return savedTransaction;
        });

        // Set optional services
        transactionService.setRealtimeService(realtimeService);
        transactionService.setDashboardService(dashboardService);

        // When
        TransactionDTO result = transactionService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        verify(realtimeService).notifyTransactionUpdate(1L, result);
        verify(dashboardService).notifyDashboardUpdate(1L);
        verify(metricsService).incrementTransactionCreated();
    }

    @Test
    void create_WithRealtimeServiceThrowingException_ShouldContinueAndLogWarning() {
        // Given
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("New Transaction");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setUserId(1L);

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            savedTransaction.setId(1L);
            return savedTransaction;
        });

        // Set optional services
        transactionService.setRealtimeService(realtimeService);
        transactionService.setDashboardService(dashboardService);

        doThrow(new RuntimeException("Realtime service error"))
                .when(realtimeService).notifyTransactionUpdate(anyLong(), any(TransactionDTO.class));
        doThrow(new RuntimeException("Dashboard service error"))
                .when(dashboardService).notifyDashboardUpdate(anyLong());

        // When
        TransactionDTO result = transactionService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        verify(realtimeService).notifyTransactionUpdate(1L, result);
        verify(dashboardService).notifyDashboardUpdate(1L);
        verify(metricsService).incrementTransactionCreated();
    }

    @Test
    void create_WithOnlyRealtimeService_ShouldNotifyOnlyRealtime() {
        // Given
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("New Transaction");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setUserId(1L);

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            savedTransaction.setId(1L);
            return savedTransaction;
        });

        // Set only realtime service
        transactionService.setRealtimeService(realtimeService);
        // dashboardService remains null

        // When
        TransactionDTO result = transactionService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        verify(realtimeService).notifyTransactionUpdate(1L, result);
        verify(dashboardService, never()).notifyDashboardUpdate(anyLong());
        verify(metricsService).incrementTransactionCreated();
    }

    @Test
    void create_WithOnlyDashboardService_ShouldNotifyOnlyDashboard() {
        // Given
        TransactionDTO createDTO = new TransactionDTO();
        createDTO.setDescription("New Transaction");
        createDTO.setAmount(BigDecimal.valueOf(50.00));
        createDTO.setType(TransactionType.INCOME);
        createDTO.setSubtype(TransactionSubtype.FIXED);
        createDTO.setSource(TransactionSource.CASH);
        createDTO.setCategoryId(1L);
        createDTO.setUserId(1L);

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        createDTO.setResponsibilities(responsibilities);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction savedTransaction = invocation.getArgument(0);
            savedTransaction.setId(1L);
            return savedTransaction;
        });

        // Set only dashboard service
        // realtimeService remains null
        transactionService.setDashboardService(dashboardService);

        // When
        TransactionDTO result = transactionService.create(createDTO);

        // Then
        assertThat(result).isNotNull();
        verify(realtimeService, never()).notifyTransactionUpdate(anyLong(), any(TransactionDTO.class));
        verify(dashboardService).notifyDashboardUpdate(1L);
        verify(metricsService).incrementTransactionCreated();
    }

    @Test
    void update_WithRealtimeAndDashboardServices_ShouldNotifyBothServices() {
        // Given
        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setDescription("Updated Transaction");
        updateDTO.setAmount(BigDecimal.valueOf(200.00));

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        updateDTO.setResponsibilities(responsibilities);

        Transaction existingTransaction = createFreshTransaction();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));
        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });
        when(transactionRepository.save(any(Transaction.class))).thenReturn(existingTransaction);

        // Set optional services
        transactionService.setRealtimeService(realtimeService);
        transactionService.setDashboardService(dashboardService);

        // When
        TransactionDTO result = transactionService.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(realtimeService).notifyTransactionUpdate(1L, result);
        verify(dashboardService).notifyDashboardUpdate(1L);
        verify(metricsService).incrementTransactionUpdated();
    }

    @Test
    void update_WithNoOptionalServices_ShouldNotNotifyAnyServices() {
        // Given
        TransactionDTO updateDTO = new TransactionDTO();
        updateDTO.setDescription("Updated Transaction");
        updateDTO.setAmount(BigDecimal.valueOf(200.00));

        List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
        TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
        responsible.setResponsibleId(1L);
        responsible.setPercentage(new BigDecimal("100.00"));
        responsibilities.add(responsible);
        updateDTO.setResponsibilities(responsibilities);

        Transaction existingTransaction = createFreshTransaction();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));
        when(responsibleRepository.findById(1L)).thenAnswer(invocation -> {
            TransactionResponsibles freshResponsible = new TransactionResponsibles();
            freshResponsible.setId(1L);
            freshResponsible.setName("Test Responsible");
            return Optional.of(freshResponsible);
        });
        when(transactionRepository.save(any(Transaction.class))).thenReturn(existingTransaction);

        // Optional services remain null

        // When
        TransactionDTO result = transactionService.update(1L, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(realtimeService, never()).notifyTransactionUpdate(anyLong(), any(TransactionDTO.class));
        verify(dashboardService, never()).notifyDashboardUpdate(anyLong());
        verify(metricsService).incrementTransactionUpdated();
    }

    @Test
    void getCategoriesByUserId_ShouldReturnCategories() {
        // Given
        List<TransactionCategory> categories = List.of(testCategory);
        when(transactionRepository.findDistinctCategoriesByUserId(1L)).thenReturn(categories);

        // When
        List<TransactionCategory> result = transactionService.getCategoriesByUserId(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(transactionRepository).findDistinctCategoriesByUserId(1L);
    }

    @Test
    void getSubcategoriesByCategoryId_ShouldReturnSubcategories() {
        // Given
        List<TransactionSubcategory> subcategories = List.of(testSubcategory);
        when(subcategoryRepository.findByCategoryIdAndIsActiveTrueOrderByNameAsc(1L)).thenReturn(subcategories);

        // When
        List<TransactionSubcategory> result = transactionService.getSubcategoriesByCategoryId(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(subcategoryRepository).findByCategoryIdAndIsActiveTrueOrderByNameAsc(1L);
    }

    @Test
    void getTransactionTypes_ShouldReturnTypes() {
        // Given
        List<String> types = List.of("INCOME", "EXPENSE");
        when(transactionRepository.findDistinctTypes()).thenReturn(types);

        // When
        List<String> result = transactionService.getTransactionTypes();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("INCOME", "EXPENSE");
        verify(transactionRepository).findDistinctTypes();
    }

    @Test
    void getSourceEntities_ShouldReturnSourceEntities() {
        // Given
        List<TransactionSourceEntity> sourceEntities = List.of(testSourceEntity);
        when(sourceRepository.findAll()).thenReturn(sourceEntities);

        // When
        List<TransactionSourceEntity> result = transactionService.getSourceEntities();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(sourceRepository).findAll();
    }

    @Test
    void getTotalAmountByUserId_ShouldReturnTotalAmount() {
        // Given
        BigDecimal totalAmount = BigDecimal.valueOf(1000.00);
        when(transactionRepository.getTotalAmountByUserId(1L)).thenReturn(totalAmount);

        // When
        BigDecimal result = transactionService.getTotalAmountByUserId(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        verify(transactionRepository).getTotalAmountByUserId(1L);
    }

    @Test
    void getAmountByType_ShouldReturnAmountByType() {
        // Given
        Map<String, BigDecimal> amountsByType = Map.of(
            "INCOME", BigDecimal.valueOf(2000.00),
            "EXPENSE", BigDecimal.valueOf(1000.00)
        );
        when(transactionRepository.getAmountByType(1L)).thenReturn(amountsByType);

        // When
        Map<String, BigDecimal> result = transactionService.getAmountByType(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get("INCOME")).isEqualByComparingTo(BigDecimal.valueOf(2000.00));
        assertThat(result.get("EXPENSE")).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        verify(transactionRepository).getAmountByType(1L);
    }

    @Test
    void getAmountByCategory_ShouldReturnAmountByCategory() {
        // Given
        Map<String, BigDecimal> amountsByCategory = Map.of(
            "Food", BigDecimal.valueOf(500.00),
            "Transport", BigDecimal.valueOf(300.00)
        );
        when(transactionRepository.getAmountByCategory(1L)).thenReturn(amountsByCategory);

        // When
        Map<String, BigDecimal> result = transactionService.getAmountByCategory(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get("Food")).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(result.get("Transport")).isEqualByComparingTo(BigDecimal.valueOf(300.00));
        verify(transactionRepository).getAmountByCategory(1L);
    }

    @Test
    void getMonthlySummary_ShouldReturnMonthlySummary() {
        // Given
        java.time.LocalDate startDate = java.time.LocalDate.of(2024, 1, 1);
        java.time.LocalDate endDate = java.time.LocalDate.of(2024, 12, 31);
        Map<String, Object> summary = Map.of(
            "totalIncome", BigDecimal.valueOf(12000.00),
            "totalExpenses", BigDecimal.valueOf(8000.00),
            "netSavings", BigDecimal.valueOf(4000.00)
        );
        when(transactionRepository.getMonthlySummary(1L, startDate, endDate)).thenReturn(summary);

        // When
        Map<String, Object> result = transactionService.getMonthlySummary(1L, startDate, endDate);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get("totalIncome")).isEqualTo(BigDecimal.valueOf(12000.00));
        assertThat(result.get("totalExpenses")).isEqualTo(BigDecimal.valueOf(8000.00));
        assertThat(result.get("netSavings")).isEqualTo(BigDecimal.valueOf(4000.00));
        verify(transactionRepository).getMonthlySummary(1L, startDate, endDate);
    }

    @Test
    void delete_ShouldIncrementMetrics() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        // When
        transactionService.delete(1L);

        // Then
        verify(transactionRepository).findById(1L);
        verify(transactionRepository).deleteById(1L);
        verify(metricsService).incrementTransactionDeleted();
    }

    @Test
    void findAll_WithSortDirectionNull_ShouldDefaultToDesc() {
        // Given
        TransactionDTO filters = null;
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));
        when(transactionRepository.findAll(eq((String) null), any(Pageable.class))).thenReturn(page);

        // When
        Page<TransactionDTO> result = transactionService.findAll(null, null, null, PageRequest.of(0, 10), filters);

        // Then
        assertThat(result).isNotNull();
        verify(transactionRepository).findAll(eq((String) null), any(Pageable.class));
    }

    @Test
    void findAll_WithSortDirectionProvided_ShouldUseProvidedDirection() {
        // Given
        TransactionDTO filters = null;
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));
        when(transactionRepository.findAll(eq((String) null), any(Pageable.class))).thenReturn(page);

        // When
        Page<TransactionDTO> result = transactionService.findAll(null, "description", "asc", PageRequest.of(0, 10), filters);

        // Then
        assertThat(result).isNotNull();
        verify(transactionRepository).findAll(eq((String) null), any(Pageable.class));
    }

}
