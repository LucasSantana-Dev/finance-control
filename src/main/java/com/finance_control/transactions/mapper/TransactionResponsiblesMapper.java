package com.finance_control.transactions.mapper;

import com.finance_control.transactions.dto.responsibles.TransactionResponsiblesDTO;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for TransactionResponsibles DTO-Entity conversion.
 * Handles the nested responsibilities mapping.
 */
@Mapper(componentModel = "spring")
public interface TransactionResponsiblesMapper {

    /**
     * Maps TransactionResponsibles entity to DTO.
     */
    @Mapping(target = "responsibleId", source = "id")
    @Mapping(target = "percentage", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "responsibleName", ignore = true)
    @Mapping(target = "calculatedAmount", ignore = true)
    TransactionResponsiblesDTO toDTO(TransactionResponsibles entity);

    /**
     * Maps TransactionResponsiblesDTO to entity.
     */
    @Mapping(target = "id", source = "responsibleId")
    @Mapping(target = "responsibilities", ignore = true)
    TransactionResponsibles toEntity(TransactionResponsiblesDTO dto);
}
