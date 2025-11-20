package com.finance_control.open_finance.service.mapper;

import com.finance_control.open_finance.client.AccountInformationClient;
import com.finance_control.open_finance.model.ConnectedAccount;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper for converting Open Finance transactions to internal TransactionDTO.
 * Extracted from OpenFinanceTransactionSyncService to reduce class fan-out complexity.
 */
@Component
public class OpenFinanceTransactionMapper {

    public TransactionDTO mapToTransactionDTO(AccountInformationClient.Transaction ofTransaction,
                                             ConnectedAccount account,
                                             TransactionCategory category,
                                             TransactionSourceEntity sourceEntity) {
        TransactionDTO dto = new TransactionDTO();
        dto.setUserId(account.getUser().getId());
        dto.setDescription(ofTransaction.getDescription() != null ?
                          ofTransaction.getDescription() : "Open Finance Transaction");
        dto.setAmount(ofTransaction.getAmount().abs());
        dto.setDate(ofTransaction.getBookingDate() != null ?
                   ofTransaction.getBookingDate() : LocalDateTime.now());
        dto.setExternalReference(ofTransaction.getTransactionId());
        dto.setBankReference(ofTransaction.getTransactionId());

        if ("CREDIT".equalsIgnoreCase(ofTransaction.getCreditDebitIndicator())) {
            dto.setType(TransactionType.INCOME);
            dto.setSubtype(TransactionSubtype.VARIABLE);
        } else {
            dto.setType(TransactionType.EXPENSE);
            dto.setSubtype(TransactionSubtype.VARIABLE);
        }

        dto.setSource(mapAccountTypeToSource(account.getAccountType()));
        dto.setCategoryId(category.getId());
        dto.setSourceEntityId(sourceEntity.getId());

        return dto;
    }

    private TransactionSource mapAccountTypeToSource(String accountType) {
        if (accountType == null) {
            return TransactionSource.OTHER;
        }
        return switch (accountType.toUpperCase()) {
            case "CHECKING", "SAVINGS" -> TransactionSource.BANK_TRANSACTION;
            case "CREDIT_CARD" -> TransactionSource.CREDIT_CARD;
            case "DEBIT_CARD" -> TransactionSource.DEBIT_CARD;
            default -> TransactionSource.OTHER;
        };
    }
}
