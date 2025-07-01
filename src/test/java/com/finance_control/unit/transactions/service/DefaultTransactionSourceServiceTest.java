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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultTransactionSourceServiceTest extends BaseUnitTest {

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
        testUser.setFullName("John Doe");

        testSourceEntity = new TransactionSourceEntity();
        testSourceEntity.setId(1L);
        testSourceEntity.setName("Nubank Credit Card");
        testSourceEntity.setDescription("Main credit card");
        testSourceEntity.setSourceType(TransactionSource.CREDIT_CARD);
        testSourceEntity.setBankName("Nubank");
        testSourceEntity.setCardType("Credit");
        testSourceEntity.setCardLastFour("1234");
        testSourceEntity.setAccountBalance(new BigDecimal("5000.00"));
        testSourceEntity.setUser(testUser);
        testSourceEntity.setIsActive(true);
        testSourceEntity.setCreatedAt(LocalDateTime.now());
        testSourceEntity.setUpdatedAt(LocalDateTime.now());

        createDTO = new TransactionSourceDTO();
        createDTO.setName("Nubank Credit Card");
        createDTO.setDescription("Main credit card");
        createDTO.setSourceType(TransactionSource.CREDIT_CARD);
        createDTO.setBankName("Nubank");
        createDTO.setCardType("Credit");
        createDTO.setCardLastFour("1234");
        createDTO.setAccountBalance(new BigDecimal("5000.00"));
        createDTO.setUserId(1L);
    }

    @Test
    void createTransactionSource_ShouldCreateSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionSourceRepository.existsByNameAndUserId(anyString(), anyLong())).thenReturn(false);
        when(transactionSourceRepository.save(any(TransactionSourceEntity.class))).thenReturn(testSourceEntity);

        TransactionSourceDTO result = transactionSourceService.create(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Nubank Credit Card");
        assertThat(result.getSourceType()).isEqualTo(TransactionSource.CREDIT_CARD);
        assertThat(result.getBankName()).isEqualTo("Nubank");
        assertThat(result.getUserId()).isEqualTo(1L);

        verify(userRepository).findById(1L);
        verify(transactionSourceRepository).existsByNameAndUserId("Nubank Credit Card", 1L);
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
    void createTransactionSource_ShouldThrowException_WhenNameAlreadyExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionSourceRepository.existsByNameAndUserId(anyString(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> transactionSourceService.create(createDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction source with this name already exists for this user");

        verify(userRepository).findById(1L);
        verify(transactionSourceRepository).existsByNameAndUserId("Nubank Credit Card", 1L);
    }

    @Test
    void updateTransactionSource_ShouldUpdateSuccessfully() {
        when(transactionSourceRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testSourceEntity));
        when(transactionSourceRepository.save(any(TransactionSourceEntity.class))).thenReturn(testSourceEntity);

        TransactionSourceDTO result = transactionSourceService.update(1L, createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Nubank Credit Card");

        verify(transactionSourceRepository).findByIdAndUserId(1L, 1L);
        verify(transactionSourceRepository).save(any(TransactionSourceEntity.class));
    }

    @Test
    void updateTransactionSource_ShouldThrowException_WhenSourceNotFound() {
        when(transactionSourceRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionSourceService.update(1L, createDTO))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Transaction source not found");

        verify(transactionSourceRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    void findById_ShouldReturnSource_WhenExists() {
        when(transactionSourceRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testSourceEntity));

        Optional<TransactionSourceDTO> result = transactionSourceService.findById(1L, 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Nubank Credit Card");

        verify(transactionSourceRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        when(transactionSourceRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        Optional<TransactionSourceDTO> result = transactionSourceService.findById(1L, 1L);

        assertThat(result).isEmpty();

        verify(transactionSourceRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    void findByUserId_ShouldReturnAllSources() {
        TransactionSourceEntity source2 = new TransactionSourceEntity();
        source2.setId(2L);
        source2.setName("Itaú Account");
        source2.setUser(testUser);

        List<TransactionSourceEntity> entities = Arrays.asList(testSourceEntity, source2);
        org.springframework.data.domain.Page<TransactionSourceEntity> page = new org.springframework.data.domain.PageImpl<>(entities);
        when(transactionSourceRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        List<TransactionSourceDTO> result = transactionSourceService.findByUserId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Nubank Credit Card");
        assertThat(result.get(1).getName()).isEqualTo("Itaú Account");
    }

    @Test
    void findActiveByUserId_ShouldReturnOnlyActiveSources() {
        TransactionSourceEntity inactiveSource = new TransactionSourceEntity();
        inactiveSource.setId(2L);
        inactiveSource.setName("Inactive Source");
        inactiveSource.setIsActive(false);
        inactiveSource.setUser(testUser);

        List<TransactionSourceEntity> entities = Arrays.asList(testSourceEntity);
        org.springframework.data.domain.Page<TransactionSourceEntity> page = new org.springframework.data.domain.PageImpl<>(entities);
        when(transactionSourceRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        List<TransactionSourceDTO> result = transactionSourceService.findActiveByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Nubank Credit Card");
        assertThat(result.get(0).getIsActive()).isTrue();
    }

    @Test
    void deleteTransactionSource_ShouldDeactivateSource() {
        when(transactionSourceRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testSourceEntity));
        when(transactionSourceRepository.save(any(TransactionSourceEntity.class))).thenReturn(testSourceEntity);

        transactionSourceService.deleteTransactionSource(1L, 1L);

        verify(transactionSourceRepository).findByIdAndUserId(1L, 1L);
        verify(transactionSourceRepository).save(any(TransactionSourceEntity.class));
    }

    @Test
    void deleteTransactionSource_ShouldThrowException_WhenSourceNotFound() {
        when(transactionSourceRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionSourceService.deleteTransactionSource(1L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Transaction source not found");

        verify(transactionSourceRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    void getTransactionSourceEntity_ShouldReturnEntity() {
        when(transactionSourceRepository.findById(1L)).thenReturn(Optional.of(testSourceEntity));

        TransactionSourceEntity result = transactionSourceRepository.findById(1L).orElseThrow();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Nubank Credit Card");

        verify(transactionSourceRepository).findById(1L);
    }

    @Test
    void getTransactionSourceEntity_ShouldThrowException_WhenNotFound() {
        when(transactionSourceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionSourceRepository.findById(1L).orElseThrow(() -> new EntityNotFoundException("Transaction source not found")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Transaction source not found");

        verify(transactionSourceRepository).findById(1L);
    }
} 