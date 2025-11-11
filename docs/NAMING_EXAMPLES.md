# Naming Convention Examples

This document shows examples of how to apply the naming conventions consistently across different modules.

> **Rule Reference**: For concise naming patterns and conventions, see `.cursor/rules/naming-conventions.mdc`

## Transaction Module (Current)

### Service Layer
- **Interface**: `TransactionService.java`
- **Implementation**: `DefaultTransactionService.java`

### Controller Layer
- **Interface**: `TransactionController.java` (if needed)
- **Implementation**: `DefaultTransactionController.java` (if needed)

### Repository Layer
- **Interface**: `TransactionRepository.java`
- **Custom Implementation**: `TransactionRepositoryImpl.java` (if needed)

### DTOs
- **Create**: `TransactionCreateDTO.java`
- **Update**: `TransactionCreateDTO.java` (currently using same as create)
- **Response**: `TransactionDTO.java`

## User Module (Future)

### Service Layer
- **Interface**: `UserService.java`
- **Implementation**: `DefaultUserService.java`

### Controller Layer
- **Interface**: `UserController.java`
- **Implementation**: `DefaultUserController.java`

### Repository Layer
- **Interface**: `UserRepository.java`

### DTOs
- **Create**: `UserCreateDTO.java`
- **Update**: `UserUpdateDTO.java`
- **Response**: `UserDTO.java`

## Financial Goal Module (Future)

### Service Layer
- **Interface**: `FinancialGoalService.java`
- **Implementation**: `DefaultFinancialGoalService.java`

### Controller Layer
- **Interface**: `FinancialGoalController.java`
- **Implementation**: `DefaultFinancialGoalController.java`

### Repository Layer
- **Interface**: `FinancialGoalRepository.java`

### DTOs
- **Create**: `FinancialGoalCreateDTO.java`
- **Update**: `FinancialGoalUpdateDTO.java`
- **Response**: `FinancialGoalDTO.java`

## Alternative Implementations

When you need multiple implementations of the same service:

### Cached Implementation
- **Interface**: `TransactionService.java`
- **Default Implementation**: `DefaultTransactionService.java`
- **Cached Implementation**: `CachedTransactionService.java`

### Mock Implementation (for testing)
- **Interface**: `TransactionService.java`
- **Default Implementation**: `DefaultTransactionService.java`
- **Mock Implementation**: `MockTransactionService.java`

## Benefits of This Naming Convention

1. **Clarity**: It's immediately clear which file is the interface vs implementation
2. **Consistency**: All modules follow the same pattern
3. **Scalability**: Easy to add alternative implementations
4. **Spring Integration**: Works well with Spring's dependency injection
5. **IDE Support**: IDEs can easily distinguish between interfaces and implementations

## File Structure Example

```
src/main/java/com/finance_control/
├── transactions/
│   ├── service/
│   │   ├── TransactionService.java          (interface)
│   │   └── DefaultTransactionService.java   (implementation)
│   ├── controller/
│   │   ├── TransactionController.java       (interface)
│   │   └── DefaultTransactionController.java (implementation)
│   ├── repository/
│   │   └── TransactionRepository.java       (interface)
│   └── dto/
│       ├── TransactionCreateDTO.java
│       ├── TransactionUpdateDTO.java
│       └── TransactionDTO.java
├── users/
│   ├── service/
│   │   ├── UserService.java                 (interface)
│   │   └── DefaultUserService.java          (implementation)
│   └── ...
└── goals/
    ├── service/
    │   ├── FinancialGoalService.java        (interface)
    │   └── DefaultFinancialGoalService.java (implementation)
    └── ...
```
