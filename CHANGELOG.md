# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **MapStruct Integration**: Added MapStruct 1.5.5.Final for type-safe DTO-entity mapping
  - `FinancialGoalMapper` with date conversion support
  - `TransactionMapper` with complex relationship handling
  - `TransactionResponsiblesMapper` for responsibility DTOs
  - `UserMapper` for user entity conversions
- **JPA Auditing Configuration**: Added `JpaConfig` for automatic timestamp management
- **Comprehensive Test Suite**: 
  - Unit tests for Auth, Goals, Transactions, and Users
  - Integration tests with TestContainers
  - Enhanced test coverage targeting 80% minimum
- **Enhanced Development Scripts**: Improved `dev.sh` with retry logic and better error handling

### Changed
- **Gradle Upgrade**: Updated Gradle wrapper to latest version
- **Entity Models**: Enhanced `FinancialGoal` model with completion tracking
- **Repository Layer**: Improved `FinancialGoalRepository` with custom queries
- **Service Layer**: Refactored `TransactionService` for better separation of concerns

### Fixed
- **Database Migration**: Fixed missing `updated_at` column in `transaction_responsibilities` table
- **Migration Cleanup**: Removed unnecessary SonarQube database migration (runs in separate container)

### Technical Improvements
- **Build Configuration**: Added MapStruct annotation processor
- **Code Quality**: Enhanced development workflow with better error messages
- **Test Infrastructure**: Added comprehensive unit and integration test coverage
- **Documentation**: Updated README with new test infrastructure details

## [0.0.1-SNAPSHOT] - 2024-12-19

### Added
- Initial project setup with Spring Boot 3.5.3
- PostgreSQL database with Flyway migrations
- JWT-based authentication system
- Transaction management with categorization
- Financial goals tracking
- RESTful API with OpenAPI documentation
- Docker containerization
- Code quality tools (Checkstyle, PMD, SpotBugs, SonarQube)
- Comprehensive test coverage with JUnit 5 and TestContainers

### Features
- Multi-source transaction tracking
- Hierarchical categorization system
- Responsibility sharing for transactions
- Installment support for recurring payments
- Financial goal tracking with progress visualization
- User isolation with multi-tenant architecture
- Comprehensive audit trails
- Input validation and error handling

### Technical Stack
- **Backend**: Spring Boot 3.5.3 with Java 21
- **Database**: PostgreSQL 17 with Flyway migrations
- **Security**: Spring Security with JWT authentication
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Testing**: JUnit 5, TestContainers, Selenium
- **Build Tool**: Gradle 8.7+
- **Code Quality**: Checkstyle, PMD, SpotBugs, SonarQube
- **Coverage**: JaCoCo with 80% minimum requirement
