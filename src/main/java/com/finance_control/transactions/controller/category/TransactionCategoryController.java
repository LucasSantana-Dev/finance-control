package com.finance_control.transactions.controller.category;

import com.finance_control.shared.controller.BaseController;
import com.finance_control.transactions.dto.category.TransactionCategoryDTO;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.service.category.TransactionCategoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction-categories")
public class TransactionCategoryController extends BaseController<TransactionCategory, Long, TransactionCategoryDTO> {

    public TransactionCategoryController(TransactionCategoryService transactionCategoryService) {
        super(transactionCategoryService);
    }
} 