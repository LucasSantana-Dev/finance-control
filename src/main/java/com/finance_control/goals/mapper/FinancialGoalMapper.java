package com.finance_control.goals.mapper;

import com.finance_control.goals.dto.FinancialGoalDTO;
import com.finance_control.goals.model.FinancialGoal;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MapStruct mapper for FinancialGoal DTO-Entity conversion.
 * Handles date/time conversions and complex field mappings.
 */
@Mapper(componentModel = "spring")
public interface FinancialGoalMapper {

    /**
     * Maps FinancialGoal entity to DTO.
     * Handles date conversions and field mappings.
     */
    @Mapping(target = "targetDate", source = "deadline", qualifiedByName = "localDateToLocalDateTime")
    @Mapping(target = "deadline", source = "deadline", qualifiedByName = "localDateToLocalDateTime")
    FinancialGoalDTO toDTO(FinancialGoal entity);

    /**
     * Maps FinancialGoalDTO to entity.
     * Handles date conversions and excludes complex relationships.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "deadline", source = "deadline", qualifiedByName = "localDateTimeToLocalDate")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FinancialGoal toEntity(FinancialGoalDTO dto);

    /**
     * Updates existing FinancialGoal entity from DTO.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "deadline", source = "deadline", qualifiedByName = "localDateTimeToLocalDate")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(FinancialGoalDTO dto, @MappingTarget FinancialGoal entity);

    /**
     * Maps list of FinancialGoal entities to DTOs.
     */
    List<FinancialGoalDTO> toDTOList(List<FinancialGoal> goals);

    /**
     * Maps list of FinancialGoal DTOs to entities.
     */
    List<FinancialGoal> toEntityList(List<FinancialGoalDTO> dtos);

    /**
     * Converts LocalDate to LocalDateTime (for DTO).
     */
    @Named("localDateToLocalDateTime")
    default LocalDateTime localDateToLocalDateTime(LocalDate localDate) {
        return localDate != null ? localDate.atStartOfDay() : null;
    }

    /**
     * Converts LocalDateTime to LocalDate (for Entity).
     */
    @Named("localDateTimeToLocalDate")
    default LocalDate localDateTimeToLocalDate(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.toLocalDate() : null;
    }
}
