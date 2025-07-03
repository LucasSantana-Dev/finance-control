package com.finance_control.transactions.controller;

import com.finance_control.shared.controller.BaseController;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.dto.TransactionReconciliationRequest;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Endpoints for managing financial transactions")
public class TransactionController extends BaseController<Transaction, Long, TransactionDTO> {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        super(transactionService);
        this.transactionService = transactionService;
    }

    @PutMapping("/{id}/reconcile")
    @Operation(summary = "Reconcile transaction", description = "Complete reconciliation of transaction data")
    public ResponseEntity<TransactionDTO> reconcileTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionReconciliationRequest request) {
        log.debug("PUT request to reconcile transaction ID: {} with data: {}", id, request);

        TransactionDTO reconciledTransaction = transactionService.reconcileTransaction(id, request);
        log.info("Transaction reconciled successfully with ID: {}", id);
        return ResponseEntity.ok(reconciledTransaction);
    }
}