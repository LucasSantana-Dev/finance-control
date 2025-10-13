package com.finance_control.transactions.controller.category;

import com.finance_control.shared.controller.BaseController;
import com.finance_control.transactions.dto.category.TransactionCategoryDTO;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.service.category.TransactionCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction-categories")
@Slf4j
@Tag(name = "Transaction Categories", description = "Endpoints for managing transaction categories")
public class TransactionCategoryController extends BaseController<TransactionCategory, Long, TransactionCategoryDTO> {

    private final TransactionCategoryService transactionCategoryService;

    public TransactionCategoryController(TransactionCategoryService transactionCategoryService) {
        super(transactionCategoryService);
        this.transactionCategoryService = transactionCategoryService;
    }

    @GetMapping("/unified")
    @Operation(summary = "Get transaction categories with filtering",
               description = "Retrieve transaction categories with flexible filtering, sorting, and pagination options, or metadata")
    public ResponseEntity<Object> getTransactionCategories(
            @Parameter(description = "Search term for name")
            @RequestParam(required = false) String search,
            @Parameter(description = "Sort field")
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Type of data to retrieve (metadata types: all, count, usage-stats)")
            @RequestParam(required = false) String data) {

        log.debug("GET request to retrieve transaction categories with filtering");

        // If data parameter is provided, return metadata
        if (data != null && !data.trim().isEmpty()) {
            return switch (data) {
                case "all" -> ResponseEntity.ok(transactionCategoryService.findAllActive());
                case "count" -> ResponseEntity.ok(transactionCategoryService.getTotalCount());
                case "usage-stats" -> ResponseEntity.ok(transactionCategoryService.getUsageStats());
                default -> throw new IllegalArgumentException("Invalid data type: " + data);
            };
        }

        // Create pageable with sorting
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TransactionCategoryDTO> categories = transactionCategoryService.findAll(search, null, sortBy, sortDirection, pageable);
        return ResponseEntity.ok(categories);
    }

}
