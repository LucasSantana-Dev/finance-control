package com.finance_control.dashboard.mapper;

import com.finance_control.dashboard.dto.*;
import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.transactions.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * MapStruct mapper for dashboard DTOs.
 */
@Mapper(componentModel = "spring")
public interface DashboardMapper {

    DashboardMapper INSTANCE = Mappers.getMapper(DashboardMapper.class);

    @Mapping(target = "goalId", source = "id")
    @Mapping(target = "goalName", source = "name")
    @Mapping(target = "targetAmount", source = "targetAmount")
    @Mapping(target = "currentAmount", source = "currentAmount")
    @Mapping(target = "progressPercentage", source = "progressPercentage")
    @Mapping(target = "deadline", source = "deadline")
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "goalType", expression = "java(goal.getGoalType().name())")
    GoalProgressDTO toGoalProgressDTO(FinancialGoal goal);

    List<GoalProgressDTO> toGoalProgressDTOList(List<FinancialGoal> goals);

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "subcategoryName", source = "subcategory.name")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "percentage", ignore = true)
    @Mapping(target = "transactionCount", constant = "1")
    @Mapping(target = "color", ignore = true)
    CategorySpendingDTO toCategorySpendingDTO(Transaction transaction);
}
