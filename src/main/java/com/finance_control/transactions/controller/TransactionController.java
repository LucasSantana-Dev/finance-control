package com.finance_control.transactions.controller;

import com.finance_control.shared.controller.BaseController;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.dto.TransactionReconciliationRequest;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

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

    @GetMapping("/filtered")
    @Operation(summary = "Get transactions with filtering",
               description = "Retrieve transactions with flexible filtering, sorting, and pagination options")
    public ResponseEntity<Page<TransactionDTO>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            Pageable pageable,
            HttpServletRequest request) {

        log.debug("GET request to retrieve transactions with filtering");

        // Extract all query parameters as filters (excluding standard ones)
        Map<String, Object> filters = extractFiltersFromRequest(request, search, sortBy, sortDirection);

        // Add specific transaction filters
        if (category != null && !category.trim().isEmpty()) {
            filters.put("category", category);
        }
        if (subcategory != null && !subcategory.trim().isEmpty()) {
            filters.put("subcategory", subcategory);
        }
        if (source != null && !source.trim().isEmpty()) {
            filters.put("source", source);
        }
        if (type != null && !type.trim().isEmpty()) {
            filters.put("type", type);
        }
        if (startDate != null) {
            filters.put("startDate", startDate);
        }
        if (endDate != null) {
            filters.put("endDate", endDate);
        }
        if (minAmount != null) {
            filters.put("minAmount", minAmount);
        }
        if (maxAmount != null) {
            filters.put("maxAmount", maxAmount);
        }
        if (isActive != null) {
            filters.put("isActive", isActive);
        }

        // Create pageable with sorting
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy != null ? sortBy : "createdAt");
        Pageable finalPageable = PageRequest.of(page, size, sort);

        Page<TransactionDTO> transactions = transactionService.findAll(search, filters, sortBy, sortDirection, finalPageable);
        return ResponseEntity.ok(transactions);
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
