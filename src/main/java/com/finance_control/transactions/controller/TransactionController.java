package com.finance_control.transactions.controller;

import com.finance_control.shared.controller.BaseController;
import com.finance_control.transactions.controller.helper.TransactionFilterHelper;
import com.finance_control.transactions.controller.helper.TransactionPageableHelper;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.dto.TransactionReconciliationRequest;
import com.finance_control.transactions.dto.importer.TransactionImportRequest;
import com.finance_control.transactions.dto.importer.TransactionImportResponse;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.service.TransactionService;
import com.finance_control.transactions.service.TransactionImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import com.finance_control.transactions.dto.TransactionInstallmentRequest;

@RestController
@Slf4j
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Endpoints for managing financial transactions")
public class TransactionController extends BaseController<Transaction, Long, TransactionDTO> {

    private final TransactionService transactionService;
    private final TransactionImportService transactionImportService;
    private final TransactionFilterHelper filterHelper;
    private final TransactionPageableHelper pageableHelper;

    public TransactionController(TransactionService transactionService,
            TransactionImportService transactionImportService,
            TransactionFilterHelper filterHelper,
            TransactionPageableHelper pageableHelper) {
        super(transactionService);
        this.transactionService = transactionService;
        this.transactionImportService = transactionImportService;
        this.filterHelper = filterHelper;
        this.pageableHelper = pageableHelper;
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

        Map<String, Object> filters = extractFiltersFromRequest(request, search, sortBy, sortDirection);
        filterHelper.addTransactionFilters(category, subcategory, source, type, startDate, endDate, minAmount, maxAmount, isActive, filters);

        Pageable finalPageable = pageableHelper.createPageableWithSort(page, size, sortBy, sortDirection);

        Page<TransactionDTO> transactions = transactionService.findAll(search, filters, sortBy, sortDirection, finalPageable);
        return ResponseEntity.ok(transactions);
    }


    @PutMapping("/{id}/reconcile")
    @Operation(summary = "Reconcile transaction", description = "Complete reconciliation of transaction data")
    public ResponseEntity<TransactionDTO> reconcileTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionReconciliationRequest request) {
        log.debug("PUT request to reconcile transaction (ID length: {}) with data: [REDACTED]", String.valueOf(id).length());

        TransactionDTO reconciledTransaction = transactionService.reconcileTransaction(id, request);
        log.info("Transaction reconciled successfully (ID present: {})", id != null);
        return ResponseEntity.ok(reconciledTransaction);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import transactions from statement files",
            description = "Supports CSV and OFX statements to generate transactions automatically.")
    public ResponseEntity<TransactionImportResponse> importTransactions(
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("config") TransactionImportRequest request) {
        log.debug("POST request to import transactions from statement file");
        TransactionImportResponse response = transactionImportService.importStatements(file, request);
        log.info("Transaction import completed - total entries: {}, created: {}", response.getTotalEntries(), response.getCreatedTransactions());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/installments")
    @Operation(summary = "Create transaction installments", description = "Create a series of installment transactions")
    public ResponseEntity<List<TransactionDTO>> createInstallments(@Valid @RequestBody TransactionInstallmentRequest request) {
        log.debug("POST request to create transaction installments");
        return ResponseEntity.ok(transactionService.createInstallments(request));
    }


}
