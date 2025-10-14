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

import jakarta.servlet.http.HttpServletRequest;

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

    @GetMapping("/metadata")
    @Operation(summary = "Get transaction categories metadata",
               description = "Retrieve transaction categories metadata (all, count, usage-stats)")
    public ResponseEntity<Object> getMetadata(
            @RequestParam String data) {

        log.debug("GET request to retrieve transaction categories metadata: {}", data);

        return switch (data) {
            case "all" -> ResponseEntity.ok(transactionCategoryService.findAllActive());
            case "count" -> ResponseEntity.ok(transactionCategoryService.getTotalCount());
            case "usage-stats" -> ResponseEntity.ok(transactionCategoryService.getUsageStats());
            default -> throw new IllegalArgumentException("Invalid data type: " + data);
        };
    }

}
