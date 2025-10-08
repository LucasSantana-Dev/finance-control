package com.finance_control.unit.transactions.service;

import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.repository.responsibles.TransactionResponsiblesRepository;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.service.TransactionService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import com.finance_control.shared.context.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionCategoryRepository categoryRepository;

    @Mock
    private TransactionResponsiblesRepository responsibleRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;
    private User testUser;
    private TransactionCategory testCategory;
    private TransactionResponsibles testResponsible;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setDescription("Test Transaction");
        testTransaction.setAmount(BigDecimal.valueOf(100.00));
        testTransaction.setDate(LocalDateTime.now());
        testTransaction.setUser(testUser);
        testTransaction.setType(TransactionType.INCOME);
        testTransaction.setSubtype(TransactionSubtype.FIXED);
        testTransaction.setSource(TransactionSource.CASH);

        testCategory = new TransactionCategory();
        testCategory.setId(1L);
        testCategory.setName("Test Category");

        testResponsible = new TransactionResponsibles();
        testResponsible.setId(1L);
        testResponsible.setName("Test Responsible");
    }

    @Test
    void shouldCreateTransaction() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            TransactionDTO dto = new TransactionDTO();
            dto.setDescription("New Transaction");
            dto.setAmount(BigDecimal.valueOf(50.00));
            dto.setType(TransactionType.INCOME);
            dto.setSubtype(TransactionSubtype.FIXED);
            dto.setSource(TransactionSource.CASH);
            dto.setCategoryId(1L);
            dto.setUserId(1L);

            List<TransactionResponsiblesDTO> responsibilities = new ArrayList<>();
            TransactionResponsiblesDTO responsible = new TransactionResponsiblesDTO();
            responsible.setId(1L);
            responsible.setName("Test Responsible");
            responsible.setResponsibleId(1L);
            responsible.setPercentage(BigDecimal.valueOf(100.00));
            responsibilities.add(responsible);
            dto.setResponsibilities(responsibilities);

            when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(testUser));
            when(categoryRepository.findById(1L)).thenReturn(java.util.Optional.of(testCategory));
            when(responsibleRepository.findById(1L)).thenReturn(java.util.Optional.of(testResponsible));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
                Transaction savedTransaction = invocation.getArgument(0);
                savedTransaction.setId(1L);
                return savedTransaction;
            });

            TransactionDTO result = transactionService.create(dto);

            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isEqualTo("New Transaction");
        }
    }
}
