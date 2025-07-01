package com.finance_control.transactions.controller.responsibles;

import com.finance_control.shared.controller.BaseController;
import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.service.responsibles.TransactionResponsiblesService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction-responsibles")
public class TransactionResponsiblesController extends
        BaseController<TransactionResponsibles, Long, TransactionResponsiblesDTO> {

    public TransactionResponsiblesController(TransactionResponsiblesService transactionResponsiblesService) {
        super(transactionResponsiblesService);
    }
}