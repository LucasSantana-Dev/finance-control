# MapStruct Integration Guide

## Overview

This document describes the MapStruct integration in the Finance Control application, which provides type-safe, compile-time DTO-entity mapping.

## What is MapStruct?

MapStruct is a code generation library that simplifies the mapping between Java bean types based on a convention over configuration approach. It generates mapping code at compile time, ensuring type safety and performance.

## Benefits

- **Type Safety**: Compile-time validation of mappings
- **Performance**: No reflection overhead at runtime
- **Maintainability**: Generated code is easy to debug and understand
- **IDE Support**: Full IDE support with autocomplete and refactoring
- **Null Safety**: Built-in null handling and validation

## Implementation

### Dependencies

The following dependencies are added to `build.gradle`:

```gradle
// MapStruct for DTO-Entity mapping (Industry Standard)
implementation 'org.mapstruct:mapstruct:1.5.5.Final'
annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
```

### Mapper Interfaces

#### FinancialGoalMapper

```java
@Mapper(componentModel = "spring")
public interface FinancialGoalMapper {
    
    FinancialGoalDTO toDTO(FinancialGoal entity);
    
    FinancialGoal toEntity(FinancialGoalDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FinancialGoal toEntityForCreate(FinancialGoalDTO dto);
    
    @Mapping(target = "createdAt", ignore = true)
    FinancialGoal toEntityForUpdate(FinancialGoalDTO dto);
}
```

#### TransactionMapper

```java
@Mapper(componentModel = "spring", uses = {TransactionResponsiblesMapper.class})
public interface TransactionMapper {
    
    TransactionDTO toDTO(Transaction entity);
    
    Transaction toEntity(TransactionDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Transaction toEntityForCreate(TransactionDTO dto);
    
    @Mapping(target = "createdAt", ignore = true)
    Transaction toEntityForUpdate(TransactionDTO dto);
}
```

#### TransactionResponsiblesMapper

```java
@Mapper(componentModel = "spring")
public interface TransactionResponsiblesMapper {
    
    TransactionResponsiblesDTO toDTO(TransactionResponsibles entity);
    
    TransactionResponsibles toEntity(TransactionResponsiblesDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TransactionResponsibles toEntityForCreate(TransactionResponsiblesDTO dto);
}
```

#### UserMapper

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserDTO toDTO(User entity);
    
    User toEntity(UserDTO dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntityForCreate(UserDTO dto);
    
    @Mapping(target = "createdAt", ignore = true)
    User toEntityForUpdate(UserDTO dto);
}
```

## Usage in Services

### Service Implementation

```java
@Service
@RequiredArgsConstructor
public class FinancialGoalServiceImpl implements FinancialGoalService {
    
    private final FinancialGoalRepository repository;
    private final FinancialGoalMapper mapper;
    
    @Override
    public FinancialGoalDTO create(FinancialGoalDTO dto) {
        FinancialGoal entity = mapper.toEntityForCreate(dto);
        FinancialGoal saved = repository.save(entity);
        return mapper.toDTO(saved);
    }
    
    @Override
    public FinancialGoalDTO update(Long id, FinancialGoalDTO dto) {
        FinancialGoal existing = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Goal not found"));
        
        FinancialGoal updated = mapper.toEntityForUpdate(dto);
        updated.setId(id);
        updated.setCreatedAt(existing.getCreatedAt());
        
        FinancialGoal saved = repository.save(updated);
        return mapper.toDTO(saved);
    }
}
```

## Advanced Features

### Custom Mapping Methods

```java
@Mapper(componentModel = "spring")
public interface FinancialGoalMapper {
    
    @Mapping(target = "completionPercentage", expression = "java(calculateCompletion(entity))")
    FinancialGoalDTO toDTO(FinancialGoal entity);
    
    default Double calculateCompletion(FinancialGoal entity) {
        if (entity.getTargetAmount() == null || entity.getTargetAmount().equals(BigDecimal.ZERO)) {
            return 0.0;
        }
        return entity.getCurrentAmount()
            .divide(entity.getTargetAmount(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }
}
```

### Date and Time Mapping

```java
@Mapper(componentModel = "spring")
public interface FinancialGoalMapper {
    
    @Mapping(target = "targetDate", source = "targetDate", dateFormat = "yyyy-MM-dd")
    FinancialGoalDTO toDTO(FinancialGoal entity);
    
    @Mapping(target = "targetDate", source = "targetDate", dateFormat = "yyyy-MM-dd")
    FinancialGoal toEntity(FinancialGoalDTO dto);
}
```

### Collection Mapping

```java
@Mapper(componentModel = "spring")
public interface TransactionMapper {
    
    List<TransactionDTO> toDTOList(List<Transaction> entities);
    
    List<Transaction> toEntityList(List<TransactionDTO> dtos);
}
```

## Best Practices

### 1. Use Separate Methods for Create and Update

```java
@Mapper(componentModel = "spring")
public interface EntityMapper {
    
    // For creating new entities
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Entity toEntityForCreate(EntityDTO dto);
    
    // For updating existing entities
    @Mapping(target = "createdAt", ignore = true)
    Entity toEntityForUpdate(EntityDTO dto);
}
```

### 2. Handle Null Values

```java
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EntityMapper {
    // Mappings will ignore null values in source
}
```

### 3. Use Component Model

Always use `componentModel = "spring"` to integrate with Spring's dependency injection:

```java
@Mapper(componentModel = "spring")
public interface EntityMapper {
    // Spring will manage this as a bean
}
```

### 4. Custom Validation

```java
@Mapper(componentModel = "spring")
public interface EntityMapper {
    
    @Mapping(target = "email", source = "email", qualifiedByName = "validateEmail")
    Entity toEntity(EntityDTO dto);
    
    @Named("validateEmail")
    default String validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        return email.toLowerCase();
    }
}
```

## Testing

### Unit Testing Mappers

```java
@ExtendWith(MockitoExtension.class)
class FinancialGoalMapperTest {
    
    @InjectMocks
    private FinancialGoalMapperImpl mapper;
    
    @Test
    void shouldMapEntityToDTO() {
        // Given
        FinancialGoal entity = new FinancialGoal();
        entity.setId(1L);
        entity.setName("Test Goal");
        entity.setTargetAmount(new BigDecimal("1000.00"));
        
        // When
        FinancialGoalDTO dto = mapper.toDTO(entity);
        
        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Test Goal");
        assertThat(dto.getTargetAmount()).isEqualTo(new BigDecimal("1000.00"));
    }
    
    @Test
    void shouldMapDTOToEntityForCreate() {
        // Given
        FinancialGoalDTO dto = new FinancialGoalDTO();
        dto.setName("Test Goal");
        dto.setTargetAmount(new BigDecimal("1000.00"));
        
        // When
        FinancialGoal entity = mapper.toEntityForCreate(dto);
        
        // Then
        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Test Goal");
        assertThat(entity.getTargetAmount()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }
}
```

## Migration from Manual Mapping

### Before (Manual Mapping)

```java
public FinancialGoalDTO toDTO(FinancialGoal entity) {
    FinancialGoalDTO dto = new FinancialGoalDTO();
    dto.setId(entity.getId());
    dto.setName(entity.getName());
    dto.setDescription(entity.getDescription());
    dto.setTargetAmount(entity.getTargetAmount());
    dto.setCurrentAmount(entity.getCurrentAmount());
    dto.setTargetDate(entity.getTargetDate());
    dto.setIsCompleted(entity.getIsCompleted());
    dto.setUserId(entity.getUser().getId());
    return dto;
}
```

### After (MapStruct)

```java
@Mapper(componentModel = "spring")
public interface FinancialGoalMapper {
    FinancialGoalDTO toDTO(FinancialGoal entity);
}
```

## Troubleshooting

### Common Issues

1. **Compilation Errors**: Ensure annotation processor is properly configured
2. **Null Pointer Exceptions**: Use null value mapping strategies
3. **Circular Dependencies**: Use `@Context` for complex mappings
4. **Performance Issues**: Avoid complex expressions in mappings

### Debugging

Enable debug logging for MapStruct:

```properties
logging.level.org.mapstruct=DEBUG
```

## Conclusion

MapStruct provides a robust, type-safe solution for DTO-entity mapping in the Finance Control application. It eliminates boilerplate code, improves maintainability, and ensures compile-time safety for all data transformations.

For more information, refer to the [official MapStruct documentation](https://mapstruct.org/).
