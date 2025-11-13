package com.finance_control.unit.shared.service;

import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.service.DataExportService;
import com.finance_control.profile.model.Profile;
import com.finance_control.shared.enums.GoalType;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DataExportService Unit Tests")
class DataExportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FinancialGoalRepository financialGoalRepository;

    @InjectMocks
    private DataExportService dataExportService;

    private User testUser;
    private Profile testProfile;
    private Transaction testTransaction;
    private FinancialGoal testFinancialGoal;

    private void setUpTestData() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());

        testProfile = new Profile();
        testProfile.setId(1L);
        testProfile.setFullName("John Doe");
        testUser.setProfile(testProfile);

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setDescription("Test Transaction");
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setType(TransactionType.EXPENSE);
        testTransaction.setDate(LocalDateTime.now());
        testTransaction.setReconciled(false);
        testTransaction.setCreatedAt(LocalDateTime.now());

        testFinancialGoal = new FinancialGoal();
        testFinancialGoal.setId(1L);
        testFinancialGoal.setName("Test Goal");
        testFinancialGoal.setDescription("Test Description");
        testFinancialGoal.setTargetAmount(new BigDecimal("10000.00"));
        testFinancialGoal.setCurrentAmount(new BigDecimal("5000.00"));
        testFinancialGoal.setGoalType(GoalType.SAVINGS);
        testFinancialGoal.setIsActive(true);
        testFinancialGoal.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("exportUserDataAsCsv_WithValidUser_ShouldReturnByteArray")
    void exportUserDataAsCsv_WithValidUser_ShouldReturnByteArray() {
        setUpTestData();
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(transactionRepository.findByUserIdWithResponsibilities(1L)).thenReturn(List.of(testTransaction));
            when(financialGoalRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testFinancialGoal)));

            byte[] result = dataExportService.exportUserDataAsCsv();

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
            String csvContent = new String(result);
            assertThat(csvContent).contains("Finance Control Data Export");
            assertThat(csvContent).contains("User ID: 1");
            assertThat(csvContent).contains("USER PROFILE");
            assertThat(csvContent).contains("TRANSACTIONS");
            assertThat(csvContent).contains("FINANCIAL GOALS");

            verify(userRepository).findById(1L);
            verify(transactionRepository).findByUserIdWithResponsibilities(1L);
            verify(financialGoalRepository).findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
        }
    }

    @Test
    @DisplayName("exportUserDataAsJson_WithValidUser_ShouldReturnJsonString")
    void exportUserDataAsJson_WithValidUser_ShouldReturnJsonString() {
        setUpTestData();
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(transactionRepository.findByUserIdWithResponsibilities(1L)).thenReturn(List.of(testTransaction));
            when(financialGoalRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testFinancialGoal)));

            String result = dataExportService.exportUserDataAsJson();

            assertThat(result).isNotNull();
            assertThat(result).contains("\"exportInfo\"");
            assertThat(result).contains("\"userId\": 1");
            assertThat(result).contains("\"userProfile\"");
            assertThat(result).contains("\"transactions\"");
            assertThat(result).contains("\"financialGoals\"");

            verify(userRepository).findById(1L);
            verify(transactionRepository).findByUserIdWithResponsibilities(1L);
            verify(financialGoalRepository).findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
        }
    }

    @Test
    @DisplayName("exportTransactionsAsCsv_WithTransactions_ShouldReturnByteArray")
    void exportTransactionsAsCsv_WithTransactions_ShouldReturnByteArray() {
        setUpTestData();
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            when(transactionRepository.findByUserIdWithResponsibilities(1L)).thenReturn(List.of(testTransaction));

            byte[] result = dataExportService.exportTransactionsAsCsv();

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
            String csvContent = new String(result);
            assertThat(csvContent).contains("Transaction Export");
            assertThat(csvContent).contains("User ID: 1");
            assertThat(csvContent).contains("TRANSACTIONS");
            assertThat(csvContent).contains("Test Transaction");

            verify(transactionRepository).findByUserIdWithResponsibilities(1L);
        }
    }

    @Test
    @DisplayName("exportFinancialGoalsAsCsv_WithGoals_ShouldReturnByteArray")
    void exportFinancialGoalsAsCsv_WithGoals_ShouldReturnByteArray() {
        setUpTestData();
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            when(financialGoalRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(testFinancialGoal)));

            byte[] result = dataExportService.exportFinancialGoalsAsCsv();

            assertThat(result).isNotNull();
            assertThat(result.length).isGreaterThan(0);
            String csvContent = new String(result);
            assertThat(csvContent).contains("Financial Goals Export");
            assertThat(csvContent).contains("User ID: 1");
            assertThat(csvContent).contains("FINANCIAL GOALS");
            assertThat(csvContent).contains("Test Goal");

            verify(financialGoalRepository).findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
        }
    }

    @Test
    @DisplayName("exportUserDataAsCsv_WithoutUserContext_ShouldThrowException")
    void exportUserDataAsCsv_WithoutUserContext_ShouldThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(null);
            when(userRepository.findById(null)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dataExportService.exportUserDataAsCsv())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Test
    @DisplayName("exportUserDataAsJson_WithEmptyData_ShouldReturnValidJson")
    void exportUserDataAsJson_WithEmptyData_ShouldReturnValidJson() {
        setUpTestData();
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(transactionRepository.findByUserIdWithResponsibilities(1L)).thenReturn(Collections.emptyList());
            when(financialGoalRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            String result = dataExportService.exportUserDataAsJson();

            assertThat(result).isNotNull();
            assertThat(result).contains("\"exportInfo\"");
            assertThat(result).contains("\"userId\": 1");
            assertThat(result).contains("\"transactions\"");
            assertThat(result).contains("\"financialGoals\"");

            verify(userRepository).findById(1L);
            verify(transactionRepository).findByUserIdWithResponsibilities(1L);
            verify(financialGoalRepository).findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
        }
    }

    @Test
    @DisplayName("exportUserDataAsCsv_WhenUserNotFound_ShouldThrowException")
    void exportUserDataAsCsv_WhenUserNotFound_ShouldThrowException() {
        try (var mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dataExportService.exportUserDataAsCsv())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");

            verify(userRepository).findById(1L);
        }
    }
}
