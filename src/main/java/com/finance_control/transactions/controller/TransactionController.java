package com.finance_control.transactions.controller;

import com.finance_control.shared.controller.BaseController;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.service.TransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Endpoints for managing financial transactions")
public class TransactionController extends BaseController<Transaction, Long, TransactionDTO> {

    public TransactionController(TransactionService transactionService) {
        super(transactionService);
    }
}