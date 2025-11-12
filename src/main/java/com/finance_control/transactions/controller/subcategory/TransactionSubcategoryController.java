package com.finance_control.transactions.controller.subcategory;

import com.finance_control.shared.controller.BaseController;
import com.finance_control.transactions.dto.subcategory.TransactionSubcategoryDTO;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import com.finance_control.transactions.service.subcategory.TransactionSubcategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/metadata")
    @Operation(summary = "Get transaction subcategories metadata",
               description = "Retrieve transaction subcategories metadata (all, by-category, by-category-usage, count, count-by-category)")
    public ResponseEntity<Object> getMetadata(
            @RequestParam String data,
            @RequestParam(required = false) Long categoryId) {

        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("Data parameter is required");
        }

        log.debug("GET request to retrieve transaction subcategories metadata (data length: {})", data.length());

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

}
