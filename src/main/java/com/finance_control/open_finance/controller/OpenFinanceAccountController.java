package com.finance_control.open_finance.controller;

import com.finance_control.open_finance.dto.AccountBalanceDTO;
import com.finance_control.open_finance.dto.ConnectedAccountDTO;
import com.finance_control.open_finance.service.OpenFinanceAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Open Finance account management.
 */
@Slf4j
@RestController
@RequestMapping("/api/open-finance/accounts")
@RequiredArgsConstructor
@Tag(name = "Open Finance Accounts", description = "Endpoints for managing connected bank accounts")
@ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
public class OpenFinanceAccountController {

    private final OpenFinanceAccountService accountService;

    @PostMapping("/discover/{consentId}")
    @Operation(summary = "Discover accounts",
               description = "Discovers and creates accounts after consent authorization.")
    public ResponseEntity<List<ConnectedAccountDTO>> discoverAccounts(@PathVariable @NotNull Long consentId) {
        log.debug("Discovering accounts for consent: {}", consentId);
        List<ConnectedAccountDTO> accounts = accountService.discoverAccounts(consentId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping
    @Operation(summary = "List user accounts",
               description = "Retrieves all connected accounts for the current user.")
    public ResponseEntity<List<ConnectedAccountDTO>> getUserAccounts() {
        log.debug("Retrieving accounts for current user");
        List<ConnectedAccountDTO> accounts = accountService.getUserAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID",
               description = "Retrieves a specific connected account by its ID.")
    public ResponseEntity<ConnectedAccountDTO> getAccount(@PathVariable Long id) {
        log.debug("Retrieving account: {}", id);
        ConnectedAccountDTO account = accountService.getAccount(id);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{id}/sync-balance")
    @Operation(summary = "Sync account balance",
               description = "Synchronizes the balance for a specific account.")
    public ResponseEntity<AccountBalanceDTO> syncBalance(@PathVariable @NotNull Long id) {
        log.debug("Syncing balance for account: {}", id);
        AccountBalanceDTO balance = accountService.syncBalance(id);
        return ResponseEntity.ok(balance);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Disconnect account",
               description = "Disconnects a connected account.")
    public ResponseEntity<Void> disconnectAccount(@PathVariable @NotNull Long id) {
        log.debug("Disconnecting account: {}", id);
        accountService.disconnectAccount(id);
        return ResponseEntity.noContent().build();
    }
}
