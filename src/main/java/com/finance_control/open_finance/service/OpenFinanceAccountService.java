package com.finance_control.open_finance.service;

import com.finance_control.open_finance.client.AccountInformationClient;
import com.finance_control.open_finance.dto.AccountBalanceDTO;
import com.finance_control.open_finance.dto.ConnectedAccountDTO;
import com.finance_control.open_finance.mapper.OpenFinanceMapper;
import com.finance_control.open_finance.model.ConnectedAccount;
import com.finance_control.open_finance.model.OpenFinanceConsent;
import com.finance_control.open_finance.repository.ConnectedAccountRepository;
import com.finance_control.open_finance.repository.OpenFinanceConsentRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.shared.service.SupabaseRealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing connected accounts.
 * Handles account discovery, balance synchronization, and account management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
public class OpenFinanceAccountService {

    private final ConnectedAccountRepository accountRepository;
    private final OpenFinanceConsentRepository consentRepository;
    private final AccountInformationClient accountClient;
    private final OpenFinanceConsentService consentService;
    private final OpenFinanceMapper mapper;
    private final MetricsService metricsService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private SupabaseRealtimeService realtimeService;

    /**
     * Discovers and creates accounts after consent authorization.
     *
     * @param consentId the consent ID
     * @return list of discovered accounts
     */
    @Transactional
    public List<ConnectedAccountDTO> discoverAccounts(Long consentId) {
        OpenFinanceConsent consent = consentRepository.findById(consentId)
                .orElseThrow(() -> new EntityNotFoundException("Consent not found: " + consentId));

        Long userId = UserContext.getCurrentUserId();
        if (!consent.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: consent does not belong to current user");
        }

        if (!consent.isActive()) {
            throw new IllegalStateException("Consent is not active");
        }

        String accessToken = consentService.getAccessToken(consentId);

        return accountClient.getAccounts(accessToken)
                .map(accounts -> accounts.stream()
                        .map(accountInfo -> {
                            // Check if account already exists
                            var existingAccount = accountRepository.findByInstitutionIdAndExternalAccountId(
                                    consent.getInstitution().getId(), accountInfo.getAccountId());

                            ConnectedAccount account;
                            if (existingAccount.isPresent()) {
                                account = existingAccount.get();
                                // Update account information
                                account.setAccountType(accountInfo.getAccountType());
                                account.setAccountNumber(accountInfo.getAccountNumber());
                                account.setBranch(accountInfo.getBranch());
                                account.setAccountHolderName(accountInfo.getAccountHolderName());
                                account.setCurrency(accountInfo.getCurrency());
                            } else {
                                account = new ConnectedAccount();
                                account.setUser(consent.getUser());
                                account.setConsent(consent);
                                account.setInstitution(consent.getInstitution());
                                account.setExternalAccountId(accountInfo.getAccountId());
                                account.setAccountType(accountInfo.getAccountType());
                                account.setAccountNumber(accountInfo.getAccountNumber());
                                account.setBranch(accountInfo.getBranch());
                                account.setAccountHolderName(accountInfo.getAccountHolderName());
                                account.setCurrency(accountInfo.getCurrency());
                                account.setSyncStatus("PENDING");
                            }

                            account = accountRepository.save(account);
                            return mapper.toDTO(account);
                        })
                        .collect(Collectors.toList()))
                .block();
    }

    /**
     * Synchronizes account balance.
     *
     * @param accountId the account ID
     * @return updated account balance DTO
     */
    @Transactional
    public AccountBalanceDTO syncBalance(Long accountId) {
        ConnectedAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));

        Long userId = UserContext.getCurrentUserId();
        if (!account.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: account does not belong to current user");
        }

        if (!account.isSyncable()) {
            throw new IllegalStateException("Account is not syncable");
        }

        String accessToken = consentService.getAccessToken(account.getConsent().getId());
        account.setSyncStatus("SYNCING");
        account = accountRepository.save(account);
        final ConnectedAccount finalAccount = account;

        try {
            return accountClient.getAccountBalance(accessToken, finalAccount.getExternalAccountId())
                    .map(balance -> {
                        finalAccount.setBalance(balance.getBalance());
                        finalAccount.setLastSyncedAt(LocalDateTime.now());
                        finalAccount.setSyncStatus("SUCCESS");
                        ConnectedAccount savedAccount = accountRepository.save(finalAccount);

                        // Broadcast realtime update
                        broadcastAccountUpdate(savedAccount);

                        metricsService.incrementOpenFinanceAccountSyncSuccess();
                        log.info("Successfully synced balance for account {}", accountId);
                        AccountBalanceDTO dto = new AccountBalanceDTO();
                        dto.setAccountId(accountId);
                        dto.setExternalAccountId(savedAccount.getExternalAccountId());
                        dto.setBalance(balance.getBalance());
                        dto.setCurrency(balance.getCurrency());
                        dto.setLastUpdated(LocalDateTime.now());
                        return dto;
                    })
                    .block();
        } catch (Exception e) {
            account.setSyncStatus("FAILED");
            accountRepository.save(account);
            metricsService.incrementOpenFinanceAccountSyncFailure();
            log.error("Failed to sync balance for account {}: {}", accountId, e.getMessage());
            throw new RuntimeException("Failed to sync account balance", e);
        }
    }

    /**
     * Gets all accounts for the current user.
     *
     * @return list of account DTOs
     */
    public List<ConnectedAccountDTO> getUserAccounts() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }

        return accountRepository.findByUserId(userId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets an account by ID.
     *
     * @param accountId the account ID
     * @return account DTO
     */
    public ConnectedAccountDTO getAccount(Long accountId) {
        ConnectedAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));

        Long userId = UserContext.getCurrentUserId();
        if (!account.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: account does not belong to current user");
        }

        return mapper.toDTO(account);
    }

    /**
     * Disconnects an account.
     *
     * @param accountId the account ID
     */
    @Transactional
    public void disconnectAccount(Long accountId) {
        ConnectedAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));

        Long userId = UserContext.getCurrentUserId();
        if (!account.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: account does not belong to current user");
        }

        account.setSyncStatus("DISABLED");
        account = accountRepository.save(account);

        // Broadcast realtime update
        broadcastAccountUpdate(account);

        log.info("Successfully disconnected account {}", accountId);
    }

    /**
     * Synchronizes balances for all syncable accounts.
     * Called by scheduler.
     */
    @Transactional
    public void syncAllBalances() {
        List<ConnectedAccount> accounts = accountRepository.findAccountsNeedingSync();

        for (ConnectedAccount account : accounts) {
            try {
                syncBalance(account.getId());
            } catch (Exception e) {
                log.error("Failed to sync balance for account {}: {}", account.getId(), e.getMessage());
            }
        }
    }

    private void broadcastAccountUpdate(ConnectedAccount account) {
        if (realtimeService != null) {
            try {
                ConnectedAccountDTO dto = mapper.toDTO(account);
                realtimeService.broadcastToUser("connected_accounts", account.getUser().getId(), dto);
                log.debug("Broadcasted account update for user {}", account.getUser().getId());
            } catch (Exception e) {
                log.warn("Failed to broadcast account update: {}", e.getMessage());
            }
        }
    }
}
