package com.finance_control.transactions.mapper;

import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.service.TransactionService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * MapStruct mapper for Transaction DTO-Entity conversion.
 * Industry standard approach for clean, type-safe mapping.
 */
@Mapper(componentModel = "spring", uses = {TransactionResponsiblesMapper.class})
public abstract class TransactionMapper {

    @Autowired
    protected TransactionService transactionService;

    /**
     * Maps Transaction entity to TransactionDTO.
     * Handles complex relationships and nested objects.
     */
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "subcategoryId", source = "subcategory.id")
    @Mapping(target = "sourceEntityId", source = "sourceEntity.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "responsibilities", ignore = true)
    public abstract TransactionDTO toDTO(Transaction transaction);

    /**
     * Maps TransactionDTO to Transaction entity.
     * Excludes complex relationships that need manual handling.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subcategory", ignore = true)
    @Mapping(target = "sourceEntity", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "responsibilities", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Transaction toEntity(TransactionDTO dto);

    /**
     * Updates existing Transaction entity from DTO.
     * Preserves existing relationships and audit fields.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subcategory", ignore = true)
    @Mapping(target = "sourceEntity", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "responsibilities", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntityFromDTO(TransactionDTO dto, @MappingTarget Transaction entity);

    /**
     * Maps list of Transaction entities to DTOs.
     */
    public abstract List<TransactionDTO> toDTOList(List<Transaction> transactions);

    /**
     * Maps list of Transaction DTOs to entities.
     */
    public abstract List<Transaction> toEntityList(List<TransactionDTO> dtos);
}
