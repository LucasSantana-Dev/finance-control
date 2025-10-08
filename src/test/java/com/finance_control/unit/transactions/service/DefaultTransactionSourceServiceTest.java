package com.finance_control.unit.transactions.service;

import com.finance_control.unit.BaseUnitTest;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.transactions.dto.source.TransactionSourceDTO;
import com.finance_control.transactions.service.source.TransactionSourceService;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import com.finance_control.transactions.repository.source.TransactionSourceRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultTransactionSourceServiceTest extends BaseUnitTest {

    private static final String SOURCE_NAME = "Nubank Credit Card";
    private static final String BANK_NAME = "Nubank";

    @Mock
    private TransactionSourceRepository transactionSourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionSourceService transactionSourceService;

    private User testUser;
    private TransactionSourceEntity testSourceEntity;
    private TransactionSourceDTO createDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password123");
        testUser.setIsActive(true);

        testSourceEntity = new TransactionSourceEntity();
        testSourceEntity.setId(1L);
        testSourceEntity.setName(SOURCE_NAME);
        testSourceEntity.setDescription("Main credit card");
        testSourceEntity.setSourceType(TransactionSource.CREDIT_CARD);
        testSourceEntity.setBankName(BANK_NAME);
        testSourceEntity.setCardType("Credit");
        testSourceEntity.setCardLastFour("1234");
        testSourceEntity.setAccountBalance(new BigDecimal("5000.00"));
        testSourceEntity.setUser(testUser);
        testSourceEntity.setIsActive(true);
        testSourceEntity.setCreatedAt(LocalDateTime.now());
        testSourceEntity.setUpdatedAt(LocalDateTime.now());

        createDTO = new TransactionSourceDTO();
        createDTO.setName(SOURCE_NAME);
        createDTO.setDescription("Main credit card");
        createDTO.setSourceType(TransactionSource.CREDIT_CARD);
        createDTO.setBankName(BANK_NAME);
        createDTO.setCardType("Credit");
        createDTO.setCardLastFour("1234");
        createDTO.setAccountBalance(new BigDecimal("5000.00"));
        createDTO.setUserId(1L);
    }

    @Test
    void createTransactionSource_ShouldCreateSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionSourceRepository.save(any(TransactionSourceEntity.class))).thenReturn(testSourceEntity);

        TransactionSourceDTO result = transactionSourceService.create(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo(SOURCE_NAME);
        assertThat(result.getSourceType()).isEqualTo(TransactionSource.CREDIT_CARD);
        assertThat(result.getBankName()).isEqualTo(BANK_NAME);
        assertThat(result.getUserId()).isEqualTo(1L);

        verify(userRepository).findById(1L);
        verify(transactionSourceRepository).save(any(TransactionSourceEntity.class));
    }

    @Test
    void createTransactionSource_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionSourceService.create(createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(1L);
    }

    @Test
    void createTransactionSource_ShouldCreateSuccessfully_WhenNameExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionSourceRepository.save(any(TransactionSourceEntity.class))).thenReturn(testSourceEntity);

        TransactionSourceDTO result = transactionSourceService.create(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(SOURCE_NAME);
        verify(userRepository).findById(1L);
        verify(transactionSourceRepository).save(any(TransactionSourceEntity.class));
    }

    @Test
    void updateTransactionSource_ShouldUpdateSuccessfully() {
        when(transactionSourceRepository.findById(1L)).thenReturn(Optional.of(testSourceEntity));
        when(transactionSourceRepository.save(any(TransactionSourceEntity.class))).thenReturn(testSourceEntity);

        TransactionSourceDTO result = transactionSourceService.update(1L, createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo(SOURCE_NAME);

        verify(transactionSourceRepository).findById(1L);
        verify(transactionSourceRepository).save(any(TransactionSourceEntity.class));
    }

    @Test
    void updateTransactionSource_ShouldThrowException_WhenSourceNotFound() {
        when(transactionSourceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionSourceService.update(1L, createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("TransactionSource not found with id: 1");

        verify(transactionSourceRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnSource_WhenExists() {
        when(transactionSourceRepository.findById(1L)).thenReturn(Optional.of(testSourceEntity));

        Optional<TransactionSourceDTO> result = transactionSourceService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo(SOURCE_NAME);

        verify(transactionSourceRepository).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        when(transactionSourceRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<TransactionSourceDTO> result = transactionSourceService.findById(1L);

        assertThat(result).isEmpty();

        verify(transactionSourceRepository).findById(1L);
    }

    @Test
    void deleteTransactionSource_ShouldDeleteSuccessfully() {
        when(transactionSourceRepository.findById(1L)).thenReturn(Optional.of(testSourceEntity));

        transactionSourceService.delete(1L);

        verify(transactionSourceRepository).findById(1L);
        verify(transactionSourceRepository).deleteById(1L);
    }

    @Test
    void deleteTransactionSource_ShouldThrowException_WhenSourceNotFound() {
        when(transactionSourceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionSourceService.delete(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("TransactionSource not found with id: 1");

        verify(transactionSourceRepository).findById(1L);
    }
}
