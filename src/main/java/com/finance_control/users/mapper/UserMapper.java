package com.finance_control.users.mapper;

import com.finance_control.users.dto.UserDTO;
import com.finance_control.users.model.User;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for User DTO-Entity conversion.
 * Handles user data mapping with security considerations.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Maps User entity to DTO.
     * Excludes sensitive fields like password.
     */
    @Mapping(target = "password", ignore = true)
    UserDTO toDTO(User entity);

    /**
     * Maps UserDTO to entity.
     * Excludes audit fields and complex relationships.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "financialGoals", ignore = true)
    User toEntity(UserDTO dto);

    /**
     * Updates existing User entity from DTO.
     * Preserves audit fields and excludes password.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "financialGoals", ignore = true)
    void updateEntityFromDTO(UserDTO dto, @MappingTarget User entity);

    /**
     * Maps list of User entities to DTOs.
     */
    List<UserDTO> toDTOList(List<User> users);

    /**
     * Maps list of User DTOs to entities.
     */
    List<User> toEntityList(List<UserDTO> dtos);
}
