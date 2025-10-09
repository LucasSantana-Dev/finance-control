# 💰 Finance Control

A comprehensive financial management system built with Spring Boot, designed to help users track transactions, manage financial goals, and gain insights into their spending patterns.

[![Java](https://img.shields.io/badge/Java-24-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.7+-green.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 🚀 Features

### 📊 Transaction Management
- **Multi-source tracking**: Credit cards, bank accounts, cash, and more
- **Categorization system**: Hierarchical categories and subcategories with full CRUD operations
- **Category management**: Complete REST API for managing transaction categories and subcategories
- **Responsibility sharing**: Split transactions between multiple people
- **Installment support**: Track recurring payments and installments
- **Advanced filtering**: Search and filter by date, type, category, and amount

### 🎯 Financial Goals
- **Goal tracking**: Set and monitor financial objectives
- **Progress visualization**: Real-time progress percentage calculation
- **Deadline management**: Track goal completion deadlines
- **Auto-calculation**: Automatic goal progress updates from transactions

### 👥 User Management
- **Secure authentication**: JWT-based authentication system
- **User isolation**: Multi-tenant architecture with data isolation
- **Profile management**: User profile and preferences

### 🔧 Technical Features
- **RESTful API**: Comprehensive REST endpoints with OpenAPI documentation
- **Database migrations**: Flyway-based schema versioning
- **Audit trails**: Automatic creation and update timestamps
- **Validation**: Comprehensive input validation and error handling
- **Testing**: Unit, integration, and E2E test coverage with 80% minimum coverage
- **DTO Mapping**: Type-safe MapStruct mappers for all entities
- **JPA Auditing**: Automatic timestamp management for all entities

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.5.3 with Java 24
- **Database**: PostgreSQL 17 with Flyway migrations
- **Security**: Spring Security with JWT authentication
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Testing**: JUnit 5, TestContainers, Selenium
- **Build Tool**: Gradle 8.7+
- **Code Quality**: Checkstyle, PMD, SpotBugs, SonarQube
- **Coverage**: JaCoCo with 80% minimum requirement
- **Mapping**: MapStruct 1.5.5.Final for type-safe DTO-entity conversion

### Project Structure
```
src/main/java/com/finance_control/
├── auth/                 # Authentication and authorization
├── goals/               # Financial goals management
├── shared/              # Common utilities and base classes
├── transactions/        # Transaction management
│   ├── category/        # Transaction categories
│   ├── responsibles/    # Transaction responsibility sharing
│   ├── source/          # Transaction sources (accounts, cards)
│   └── subcategory/     # Transaction subcategories
└── users/               # User management
```

### Design Patterns
- **Layered Architecture**: Controller → Service → Repository → Entity
- **Base Classes**: Reusable base classes for common operations
- **DTO Pattern**: Data transfer objects for API communication with MapStruct mapping
- **Specification Pattern**: Dynamic query building
- **Audit Pattern**: Automatic timestamp management with JPA auditing
- **Mapper Pattern**: Type-safe DTO-entity conversion with MapStruct

## 🚀 Quick Start

### Prerequisites
- Java 21 or 22
- Gradle 8.7+ (or use the included wrapper)
- PostgreSQL 17
- Docker (optional)

### Environment Setup
Create a `.env` file in the project root:
```env
# Database Configuration
DB_URL=jdbc:postgresql://localhost
DB_PORT=5432
DB_NAME=finance_control
DB_USERNAME=postgres
DB_PASSWORD=your_password

# PostgreSQL Container
POSTGRES_DB=finance_control
```

### Running with Docker Compose
```bash
# Start the application with PostgreSQL
docker-compose up -d

# The application will be available at http://localhost:${APPLICATION_PORT}
```

### Running Locally
```bash
# Clone the repository
git clone https://github.com/yourusername/finance-control.git
cd finance-control

# Build the project
./gradlew build

# Run database migrations
./gradlew flywayMigrate

# Start the application
./gradlew bootRun
```

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test categories
./gradlew test --tests "*UnitTest"
./gradlew test --tests "*IntegrationTest"
./gradlew test --tests "*SeleniumTest"

# Run tests with coverage
./gradlew jacocoTestReport
./gradlew jacocoTestCoverageVerification
```

### Code Quality Checks
```bash
# Run all quality checks
./gradlew qualityCheck

# Individual quality checks
./gradlew checkstyleMain    # Code style validation
./gradlew pmdMain          # Static code analysis
./gradlew spotbugsMain     # Bug detection
./gradlew sonarqube        # SonarQube analysis

# Quality check with enhanced script (includes retry logic)
./scripts/dev.sh quality

# Quality check without tests (faster)
./scripts/dev.sh quality --no-test
```

### Coverage Requirements
- **Minimum Coverage**: 80% (line and branch coverage)
- **Exclusions**: Configuration classes, DTOs, models, exceptions, enums, utilities, and validation classes
- **Reports**: HTML and XML formats available in `build/reports/jacoco/`

## 📚 API Documentation

Once the application is running, you can access:
- **Swagger UI**: http://localhost:${APPLICATION_PORT}/swagger-ui.html
- **OpenAPI JSON**: http://localhost:${APPLICATION_PORT}/v3/api-docs

### Key Endpoints

#### Authentication
- `POST /auth/login` - User login
- `POST /auth/validate` - Validate JWT token

#### Transactions
- `GET /transactions` - List transactions with filtering
- `POST /transactions` - Create new transaction
- `PUT /transactions/{id}` - Update transaction
- `DELETE /transactions/{id}` - Delete transaction

#### Financial Goals
- `GET /goals` - List financial goals
- `POST /goals` - Create new goal
- `PUT /goals/{id}` - Update goal
- `DELETE /goals/{id}` - Delete goal

#### Transaction Categories
- `GET /transaction-categories` - List transaction categories
- `POST /transaction-categories` - Create new category
- `PUT /transaction-categories/{id}` - Update category
- `DELETE /transaction-categories/{id}` - Delete category

#### Transaction Subcategories
- `GET /transaction-subcategories` - List transaction subcategories
- `POST /transaction-subcategories` - Create new subcategory
- `PUT /transaction-subcategories/{id}` - Update subcategory
- `DELETE /transaction-subcategories/{id}` - Delete subcategory
- `GET /transaction-subcategories/category/{categoryId}` - Get subcategories by category
- `GET /transaction-subcategories/category/{categoryId}/usage` - Get subcategories ordered by usage
- `GET /transaction-subcategories/category/{categoryId}/count` - Get subcategory count by category

#### Other Resources
- `GET /transaction-sources` - List transaction sources
- `GET /transaction-responsibles` - List responsible parties

## 🧪 Testing Strategy

### Test Categories
- **Unit Tests**: Individual component testing with mocked dependencies
- **Integration Tests**: Database and service layer integration testing
- **E2E Tests**: Full application testing with Selenium WebDriver

### Test Organization
```
src/test/java/com/finance_control/
├── unit/           # Unit tests
├── integration/    # Integration tests
├── e2e/           # End-to-end tests
└── selenium/      # Selenium test utilities
```

### Running Tests
```bash
# Unit tests only
./gradlew test --tests "*UnitTest"

# Integration tests only
./gradlew test --tests "*IntegrationTest"

# E2E tests only
./gradlew test --tests "*SeleniumTest"

# All tests with coverage
./gradlew test jacocoTestReport
```

### Test Infrastructure
The project includes comprehensive test coverage with multiple testing strategies:

- **Unit Tests**: Fast, isolated tests for individual components
  - Service layer tests with mocked dependencies
  - Repository tests with in-memory H2 database
  - Controller tests with MockMvc
  - Model validation tests

- **Integration Tests**: Full application context tests
  - TestContainers with real PostgreSQL database
  - End-to-end service integration testing
  - Database transaction testing
  - JPA auditing verification

- **Test Coverage**: 80% minimum coverage requirement
  - JaCoCo coverage reports
  - Branch and line coverage analysis
  - Quality gates enforcement

## 🛠️ Development

### Recent Improvements

The project has recently undergone significant improvements:

- **✅ MapStruct Integration**: Type-safe DTO-entity mapping with compile-time validation
- **✅ JPA Auditing**: Automatic timestamp management for all entities
- **✅ Enhanced Testing**: Comprehensive unit and integration test coverage
- **✅ Quality Gates**: All code quality checks passing consistently
- **✅ Docker Compatibility**: Full macOS and Docker Compose v2 support
- **✅ Test Isolation**: Fixed optimistic locking issues in integration tests
- **✅ Development Scripts**: Enhanced `dev.sh` with retry logic and better error handling

### Code Standards
- **Java 24**: Latest LTS version with modern features
- **Lombok**: Reduces boilerplate code
- **Spring Boot**: Latest stable version
- **PostgreSQL**: Primary database with optimized queries
- **Flyway**: Database migration management
- **Code Quality**: Enforced by Checkstyle, PMD, and SpotBugs
- **Coverage**: Minimum 80% test coverage required
- **SonarQube**: Code quality gates and analysis

### Architecture Guidelines
- **Base Classes**: Extend appropriate base classes for consistency
- **DTO Pattern**: Use DTOs for API communication
- **Validation**: Comprehensive input validation
- **Error Handling**: Centralized exception handling
- **Security**: JWT-based authentication with user isolation

### Database Design
- **Normalized Schema**: Proper normalization for data integrity
- **Indexes**: Optimized indexes for query performance
- **Foreign Keys**: Referential integrity constraints
- **Audit Fields**: Automatic timestamp management

## 📖 Documentation

Comprehensive documentation is available in the [`docs/`](docs/) folder:

- **[Architecture Guide](docs/BASE_CLASSES_GUIDE.md)** - Base classes and architecture patterns
- **[API Patterns](docs/API_PATTERNS.md)** - REST API conventions and best practices
- **[Testing Strategy](docs/TESTING_STRATEGY.md)** - Testing guidelines and examples
- **[Naming Conventions](docs/NAMING_EXAMPLES.md)** - Code naming standards
- **[Service Patterns](docs/SERVICE_IMPROVEMENTS.md)** - Service layer improvements
- **[Code Quality Tools](docs/CODE_QUALITY_TOOLS.md)** - Checkstyle, PMD, SpotBugs configuration
- **[Gradle Scripts](docs/GRADLE_SCRIPTS.md)** - Custom Gradle tasks and scripts

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Follow coding standards**: Use the established patterns and conventions
4. **Write tests**: Ensure new features have proper test coverage
5. **Update documentation**: Keep documentation in sync with code changes
6. **Submit a pull request**: Provide clear description of changes

### Development Setup
```bash
# Fork and clone
git clone https://github.com/yourusername/finance-control.git
cd finance-control

# Create feature branch
git checkout -b feature/your-feature

# Make changes and test
./gradlew clean test

# Run quality checks
./gradlew qualityCheck

# Commit with conventional commits
git commit -m "feat: add new transaction filtering feature"

# Push and create PR
git push origin feature/your-feature
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Issues**: Report bugs and feature requests via [GitHub Issues](https://github.com/yourusername/finance-control/issues)
- **Discussions**: Join community discussions in [GitHub Discussions](https://github.com/yourusername/finance-control/discussions)
- **Documentation**: Check the [docs/](docs/) folder for detailed guides

## 🗺️ Roadmap

- [ ] **Dashboard**: Interactive financial dashboard with charts
- [ ] **Reports**: Advanced financial reporting and analytics
- [ ] **Budgeting**: Budget planning and tracking features
- [ ] **Mobile App**: React Native mobile application
- [ ] **Export**: Data export to CSV, PDF, and Excel
- [ ] **Notifications**: Email and push notifications
- [ ] **Multi-currency**: Support for multiple currencies
- [ ] **Recurring Transactions**: Automated recurring transaction management

---

**Built with ❤️ using Spring Boot and modern Java technologies**

---

## 📚 Further Reference & Guides

### Reference Documentation
- [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
- [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.3/maven-plugin)
- [Create an OCI image](https://docs.spring.io/spring-boot/3.5.3/maven-plugin/build-image.html)
- [Spring Web](https://docs.spring.io/spring-boot/3.5.3/reference/web/servlet.html)
- [Spring Security](https://docs.spring.io/spring-boot/3.5.3/reference/web/spring-security.html)
- [Spring Data JPA](https://docs.spring.io/spring-boot/3.5.3/reference/data/sql.html#data.sql.jpa-and-spring-data)
- [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.5.3/reference/using/devtools.html)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/3.5.3/reference/actuator/index.html)
- [Validation](https://docs.spring.io/spring-boot/3.5.3/reference/io/validation.html)

### Guides
- [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
- [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
- [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
- [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
- [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
- [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
- [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
- [Validation](https://spring.io/guides/gs/validating-form-input/)

### Code Quality Tools

The project uses several code quality tools to maintain high standards:

- **Checkstyle**: Enforces coding standards and conventions
- **PMD**: Static code analysis for potential bugs and code smells
- **SpotBugs**: Bytecode analysis for bug detection
- **SonarQube**: Comprehensive code quality analysis and reporting
- **JaCoCo**: Test coverage analysis with 80% minimum requirement

### Quality Reports

Quality reports are generated in `build/reports/`:
- **Checkstyle**: `build/reports/checkstyle/`
- **PMD**: `build/reports/pmd/`
- **SpotBugs**: `build/reports/spotbugs/`
- **JaCoCo**: `build/reports/jacoco/`
- **SonarQube**: Available via SonarQube server
