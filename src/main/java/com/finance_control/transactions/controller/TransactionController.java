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

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @GetMapping("/unified")
    @Operation(summary = "Get transactions with filtering",
               description = "Retrieve transactions with flexible filtering, sorting, and pagination options, or metadata")
    public ResponseEntity<Object> getTransactions(
            @Parameter(description = "User ID for filtering")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "Transaction type filter")
            @RequestParam(required = false) com.finance_control.shared.enums.TransactionType type,
            @Parameter(description = "Category ID filter")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Subcategory ID filter")
            @RequestParam(required = false) Long subcategoryId,
            @Parameter(description = "Source entity ID filter")
            @RequestParam(required = false) Long sourceEntityId,
            @Parameter(description = "Search term for description")
            @RequestParam(required = false) String search,
            @Parameter(description = "Sort field")
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Minimum amount filter")
            @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Maximum amount filter")
            @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "Start date filter (yyyy-MM-dd)")
            @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "End date filter (yyyy-MM-dd)")
            @RequestParam(required = false) LocalDate endDate,
            @Parameter(description = "Type of data to retrieve (metadata types: categories, subcategories, types, sources, total-amount, amount-by-type, amount-by-category, monthly-summary)")
            @RequestParam(required = false) String data) {

        log.debug("GET request to retrieve transactions with filtering");

        // If data parameter is provided, return metadata
        if (data != null && !data.trim().isEmpty()) {
            return switch (data) {
                case "categories" -> ResponseEntity.ok(transactionService.getCategoriesByUserId(userId));
                case "subcategories" -> {
                    if (categoryId == null) {
                        throw new IllegalArgumentException("Category ID is required for subcategories data");
                    }
                    yield ResponseEntity.ok(transactionService.getSubcategoriesByCategoryId(categoryId));
                }
                case "types" -> ResponseEntity.ok(transactionService.getTransactionTypes());
                case "sources" -> ResponseEntity.ok(transactionService.getSourceEntities());
                case "total-amount" -> ResponseEntity.ok(transactionService.getTotalAmountByUserId(userId));
                case "amount-by-type" -> ResponseEntity.ok(transactionService.getAmountByType(userId));
                case "amount-by-category" -> ResponseEntity.ok(transactionService.getAmountByCategory(userId));
                case "monthly-summary" -> {
                    if (startDate == null || endDate == null) {
                        throw new IllegalArgumentException("Start date and end date are required for monthly summary");
                    }
                    yield ResponseEntity.ok(transactionService.getMonthlySummary(userId, startDate, endDate));
                }
                default -> throw new IllegalArgumentException("Invalid data type: " + data);
            };
        }

        // Create filters DTO
        TransactionDTO filters = new TransactionDTO();
        filters.setUserId(userId);
        filters.setType(type);
        filters.setCategoryId(categoryId);
        filters.setSubcategoryId(subcategoryId);
        filters.setSourceEntityId(sourceEntityId);
        filters.setDescription(search);

        // Create pageable with sorting
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TransactionDTO> transactions = transactionService.findAll(search, sortBy, sortDirection, pageable, filters);
        return ResponseEntity.ok(transactions);
    }

}
