package com.finance_control.unit.transactions.service;

import com.finance_control.unit.BaseUnitTest;
import com.finance_control.transactions.service.TransactionService;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.repository.responsibles.TransactionResponsiblesRepository;
import com.finance_control.transactions.repository.source.TransactionSourceRepository;
import com.finance_control.transactions.repository.subcategory.TransactionSubcategoryRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TransactionServiceUnitTest extends BaseUnitTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionCategoryRepository categoryRepository;

    @Mock
    private TransactionSubcategoryRepository subcategoryRepository;

    @Mock
    private TransactionSourceRepository sourceEntityRepository;

    @Mock
    private TransactionResponsiblesRepository responsibleRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private TransactionDTO createDTO;
    private Transaction savedTransaction;
    private TransactionCategory testCategory;
    private TransactionResponsibles testResponsible;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");

        testCategory = new TransactionCategory();
        testCategory.setId(1L);
        testCategory.setName("Test Category");

        testResponsible = new TransactionResponsibles();
        testResponsible.setId(1L);
        testResponsible.setName("Test Responsible");

        createDTO = new TransactionDTO();
        createDTO.setDescription("Test transaction");
        createDTO.setAmount(new BigDecimal("100.00"));
        createDTO.setType(TransactionType.EXPENSE);
        createDTO.setUserId(1L);
        createDTO.setCategoryId(1L);
        // Create a responsibility to satisfy the 100% requirement
        TransactionResponsiblesDTO responsibility = new TransactionResponsiblesDTO();
        responsibility.setResponsibleId(1L);
        responsibility.setPercentage(new BigDecimal("100.00"));
        responsibility.setNotes("Test responsibility");
        
        createDTO.setResponsibilities(List.of(responsibility));

        savedTransaction = new Transaction();
        savedTransaction.setId(1L);
        savedTransaction.setDescription("Test transaction");
        savedTransaction.setAmount(new BigDecimal("100.00"));
        savedTransaction.setType(TransactionType.EXPENSE);
        savedTransaction.setUser(testUser);
        savedTransaction.setCategory(testCategory);
        savedTransaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createTransaction_ShouldReturnTransactionDTO_WhenValidData() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(responsibleRepository.findById(1L)).thenReturn(Optional.of(testResponsible));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionDTO result = transactionService.createTransaction(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Test transaction");
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(result.getType()).isEqualTo(TransactionType.EXPENSE);
    }
} 