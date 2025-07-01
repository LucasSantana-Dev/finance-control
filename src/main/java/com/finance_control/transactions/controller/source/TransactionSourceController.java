package com.finance_control.transactions.controller.source;

import com.finance_control.shared.controller.BaseController;
import com.finance_control.transactions.dto.source.TransactionSourceDTO;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import com.finance_control.transactions.service.source.TransactionSourceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transaction-sources")
public class TransactionSourceController extends BaseController<TransactionSourceEntity, Long, TransactionSourceDTO> {

    public TransactionSourceController(TransactionSourceService transactionSourceService) {
        super(transactionSourceService);
    }

}