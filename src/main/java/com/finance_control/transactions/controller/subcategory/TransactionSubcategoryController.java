package com.finance_control.transactions.controller.subcategory;

import com.finance_control.shared.controller.BaseController;
import com.finance_control.transactions.dto.subcategory.TransactionSubcategoryDTO;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import com.finance_control.transactions.service.subcategory.TransactionSubcategoryService;
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

import java.util.List;

@RestController
@RequestMapping("/transaction-subcategories")
@Slf4j
@Tag(name = "Transaction Subcategories", description = "Endpoints for managing transaction subcategories")
public class TransactionSubcategoryController
        extends BaseController<TransactionSubcategory, Long, TransactionSubcategoryDTO> {

    private final TransactionSubcategoryService transactionSubcategoryService;

    public TransactionSubcategoryController(TransactionSubcategoryService transactionSubcategoryService) {
        super(transactionSubcategoryService);
        this.transactionSubcategoryService = transactionSubcategoryService;
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<TransactionSubcategoryDTO>> getTransactionSubcategoriesByCategory(
            @PathVariable Long categoryId) {
        List<TransactionSubcategoryDTO> subcategories = transactionSubcategoryService.findByCategoryId(categoryId);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/category/{categoryId}/usage")
    public ResponseEntity<List<TransactionSubcategoryDTO>> getTransactionSubcategoriesByCategoryOrderByUsage(
            @PathVariable Long categoryId) {
        List<TransactionSubcategoryDTO> subcategories =
                transactionSubcategoryService.findByCategoryIdOrderByUsage(categoryId);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/category/{categoryId}/count")
    public ResponseEntity<Long> getTransactionSubcategoryCountByCategory(@PathVariable Long categoryId) {
        long count = transactionSubcategoryService.countByCategoryId(categoryId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/unified")
    @Operation(summary = "Get transaction subcategories with filtering",
               description = "Retrieve transaction subcategories with flexible filtering, sorting, and pagination options, or metadata")
    public ResponseEntity<Object> getTransactionSubcategories(
            @Parameter(description = "Category ID filter")
            @RequestParam(required = false) Long categoryId,
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
            @Parameter(description = "Sort by usage (true/false)")
            @RequestParam(required = false, defaultValue = "false") boolean sortByUsage,
            @Parameter(description = "Type of data to retrieve (metadata types: all, by-category, by-category-usage, count, count-by-category)")
            @RequestParam(required = false) String data) {

        log.debug("GET request to retrieve transaction subcategories with filtering");

        // If data parameter is provided, return metadata
        if (data != null && !data.trim().isEmpty()) {
            return switch (data) {
                case "all" -> ResponseEntity.ok(transactionSubcategoryService.findAllActive());
                case "by-category" -> {
                    if (categoryId == null) {
                        throw new IllegalArgumentException("Category ID is required for by-category data");
                    }
                    yield ResponseEntity.ok(transactionSubcategoryService.findByCategoryId(categoryId));
                }
                case "by-category-usage" -> {
                    if (categoryId == null) {
                        throw new IllegalArgumentException("Category ID is required for by-category-usage data");
                    }
                    yield ResponseEntity.ok(transactionSubcategoryService.findByCategoryIdOrderByUsage(categoryId));
                }
                case "count" -> ResponseEntity.ok(transactionSubcategoryService.getTotalCount());
                case "count-by-category" -> {
                    if (categoryId == null) {
                        throw new IllegalArgumentException("Category ID is required for count-by-category data");
                    }
                    yield ResponseEntity.ok(transactionSubcategoryService.countByCategoryId(categoryId));
                }
                default -> throw new IllegalArgumentException("Invalid data type: " + data);
            };
        }

        // Create pageable with sorting
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TransactionSubcategoryDTO> subcategories;
        if (categoryId != null) {
            if (sortByUsage) {
                subcategories = transactionSubcategoryService.findByCategoryIdOrderByUsage(categoryId, pageable);
            } else {
                subcategories = transactionSubcategoryService.findByCategoryId(categoryId, pageable);
            }
        } else {
            subcategories = transactionSubcategoryService.findAll(search, null, sortBy, sortDirection, pageable);
        }

        return ResponseEntity.ok(subcategories);
    }

}
