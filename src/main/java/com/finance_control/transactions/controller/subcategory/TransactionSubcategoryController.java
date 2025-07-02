package com.finance_control.transactions.controller.subcategory;

import com.finance_control.shared.controller.BaseController;
import com.finance_control.transactions.dto.subcategory.TransactionSubcategoryDTO;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import com.finance_control.transactions.service.subcategory.TransactionSubcategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction-subcategories")
public class TransactionSubcategoryController extends BaseController<TransactionSubcategory, Long, TransactionSubcategoryDTO> {

    private final TransactionSubcategoryService transactionSubcategoryService;

    public TransactionSubcategoryController(TransactionSubcategoryService transactionSubcategoryService) {
        super(transactionSubcategoryService);
        this.transactionSubcategoryService = transactionSubcategoryService;
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<TransactionSubcategoryDTO>> getTransactionSubcategoriesByCategory(@PathVariable Long categoryId) {
        List<TransactionSubcategoryDTO> subcategories = transactionSubcategoryService.findByCategoryId(categoryId);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/category/{categoryId}/usage")
    public ResponseEntity<List<TransactionSubcategoryDTO>> getTransactionSubcategoriesByCategoryOrderByUsage(@PathVariable Long categoryId) {
        List<TransactionSubcategoryDTO> subcategories = transactionSubcategoryService.findByCategoryIdOrderByUsage(categoryId);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/category/{categoryId}/count")
    public ResponseEntity<Long> getTransactionSubcategoryCountByCategory(@PathVariable Long categoryId) {
        long count = transactionSubcategoryService.countByCategoryId(categoryId);
        return ResponseEntity.ok(count);
    }
} 