package com.finance_control.integration.transactions.repository;

import com.finance_control.integration.BaseIntegrationTest;
import com.finance_control.transactions.repository.source.TransactionSourceRepository;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionSourceEntityRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionSourceRepository transactionSourceRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private TransactionSourceEntity testSourceEntity;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("password123");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);

        testSourceEntity = new TransactionSourceEntity();
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
    }

    @Test
    void save_ShouldPersistTransactionSource() {
        TransactionSourceEntity saved = transactionSourceRepository.save(testSourceEntity);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Nubank Credit Card");
        assertThat(saved.getSourceType()).isEqualTo(TransactionSource.CREDIT_CARD);
        assertThat(saved.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findById_ShouldReturnTransactionSource_WhenExists() {
        TransactionSourceEntity saved = transactionSourceRepository.save(testSourceEntity);

        Optional<TransactionSourceEntity> found = transactionSourceRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Nubank Credit Card");
        assertThat(found.get().getSourceType()).isEqualTo(TransactionSource.CREDIT_CARD);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        Optional<TransactionSourceEntity> found = transactionSourceRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findByUserId_ShouldReturnUserSources() {
        transactionSourceRepository.save(testSourceEntity);

        TransactionSourceEntity source2 = new TransactionSourceEntity();
        source2.setName("Itaú Account");
        source2.setSourceType(TransactionSource.BANK_TRANSACTION);
        source2.setBankName("Itaú");
        source2.setUser(testUser);
        source2.setIsActive(true);
        source2 = transactionSourceRepository.save(source2);

        User otherUser = new User();
        otherUser.setEmail("jane.smith@example.com");
        otherUser.setPassword("password123");
        otherUser.setIsActive(true);
        otherUser = userRepository.save(otherUser);

        TransactionSourceEntity otherSource = new TransactionSourceEntity();
        otherSource.setName("Other User Source");
        otherSource.setSourceType(TransactionSource.CREDIT_CARD);
        otherSource.setUser(otherUser);
        otherSource.setIsActive(true);
        transactionSourceRepository.save(otherSource);

        List<TransactionSourceEntity> userSources = transactionSourceRepository.findAllByUserIdOrderByNameAsc(testUser.getId());

        assertThat(userSources).hasSize(2);
        assertThat(userSources).extracting("name")
                .containsExactlyInAnyOrder("Nubank Credit Card", "Itaú Account");
    }

    @Test
    void findByUserIdAndIsActiveTrue_ShouldReturnOnlyActiveSources() {
        transactionSourceRepository.save(testSourceEntity);

        TransactionSourceEntity inactiveSource = new TransactionSourceEntity();
        inactiveSource.setName("Inactive Source");
        inactiveSource.setSourceType(TransactionSource.CREDIT_CARD);
        inactiveSource.setUser(testUser);
        inactiveSource.setIsActive(false);
        transactionSourceRepository.save(inactiveSource);

        List<TransactionSourceEntity> activeSources = transactionSourceRepository.findByUserIdAndIsActiveTrue(testUser.getId());

        assertThat(activeSources).hasSize(1);
        assertThat(activeSources.get(0).getName()).isEqualTo("Nubank Credit Card");
        assertThat(activeSources.get(0).getIsActive()).isTrue();
    }

    @Test
    void findByIdAndUserId_ShouldReturnSource_WhenExists() {
        TransactionSourceEntity saved = transactionSourceRepository.save(testSourceEntity);

        Optional<TransactionSourceEntity> found = transactionSourceRepository.findByIdAndUserId(saved.getId(), testUser.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Nubank Credit Card");
    }

    @Test
    void findByIdAndUserId_ShouldReturnEmpty_WhenWrongUser() {
        TransactionSourceEntity saved = transactionSourceRepository.save(testSourceEntity);

        User otherUser = new User();
        otherUser.setEmail("jane.smith@example.com");
        otherUser.setPassword("password123");
        otherUser.setIsActive(true);
        otherUser = userRepository.save(otherUser);

        Optional<TransactionSourceEntity> found = transactionSourceRepository.findByIdAndUserId(saved.getId(), otherUser.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void existsByNameAndUserId_ShouldReturnTrue_WhenExists() {
        transactionSourceRepository.save(testSourceEntity);

        boolean exists = transactionSourceRepository.existsByNameIgnoreCaseAndUserId("Nubank Credit Card", testUser.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameAndUserId_ShouldReturnFalse_WhenNotExists() {
        boolean exists = transactionSourceRepository.existsByNameIgnoreCaseAndUserId("Non-existent Source", testUser.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void existsByNameAndUserId_ShouldReturnFalse_WhenWrongUser() {
        transactionSourceRepository.save(testSourceEntity);

        User otherUser = new User();
        otherUser.setEmail("jane.smith@example.com");
        otherUser.setPassword("password123");
        otherUser.setIsActive(true);
        otherUser = userRepository.save(otherUser);

        boolean exists = transactionSourceRepository.existsByNameIgnoreCaseAndUserId("Nubank Credit Card", otherUser.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void findByUserIdAndSourceType_ShouldReturnSourcesByType() {
        transactionSourceRepository.save(testSourceEntity);

        TransactionSourceEntity bankAccount = new TransactionSourceEntity();
        bankAccount.setName("Itaú Account");
        bankAccount.setSourceType(TransactionSource.BANK_TRANSACTION);
        bankAccount.setBankName("Itaú");
        bankAccount.setUser(testUser);
        bankAccount.setIsActive(true);
        transactionSourceRepository.save(bankAccount);

        List<TransactionSourceEntity> creditCards = transactionSourceRepository.findByUserIdAndSourceType(testUser.getId(), TransactionSource.CREDIT_CARD);

        assertThat(creditCards).hasSize(1);
        assertThat(creditCards.get(0).getSourceType()).isEqualTo(TransactionSource.CREDIT_CARD);
    }
}
