# 💰 Finance Control

A comprehensive financial management system built with Spring Boot, designed to help users track transactions, manage financial goals, and gain insights into their spending patterns.

[![Java](https://img.shields.io/badge/Java-24-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-red.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 🚀 Features

### 📊 Transaction Management
- **Multi-source tracking**: Credit cards, bank accounts, cash, and more
- **Categorization system**: Hierarchical categories and subcategories
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
- **Testing**: Unit, integration, and E2E test coverage

## 🏗️ Architecture

### Technology Stack
- **Backend**: Spring Boot 3.5.3 with Java 24
- **Database**: PostgreSQL 17 with Flyway migrations
- **Security**: Spring Security with JWT authentication
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Testing**: JUnit 5, TestContainers, Selenium
- **Build Tool**: Maven 3.9+

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
- **DTO Pattern**: Data transfer objects for API communication
- **Specification Pattern**: Dynamic query building
- **Audit Pattern**: Automatic timestamp management

## 🚀 Quick Start

### Prerequisites
- Java 24 or higher
- Maven 3.9+
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
./mvnw clean compile

# Run database migrations
./mvnw flyway:migrate

# Start the application
./mvnw spring-boot:run
```

### Running Tests
```bash
# Run all tests
./mvnw test

# Run specific test categories
./mvnw test -Dtest=*UnitTest
./mvnw test -Dtest=*IntegrationTest
./mvnw test -Dtest=*SeleniumTest
```

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

#### Categories & Sources
- `GET /transaction-categories` - List transaction categories
- `GET /transaction-sources` - List transaction sources
- `GET /transaction-subcategories` - List subcategories
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
./mvnw test -Dtest=*UnitTest

# Integration tests only
./mvnw test -Dtest=*IntegrationTest

# E2E tests only
./mvnw test -Dtest=*SeleniumTest

# All tests with coverage
./mvnw test jacoco:report
```

## 🛠️ Development

### Code Standards
- **Java 24**: Latest LTS version with modern features
- **Lombok**: Reduces boilerplate code
- **Spring Boot**: Latest stable version
- **PostgreSQL**: Primary database with optimized queries
- **Flyway**: Database migration management

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
./mvnw clean test

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

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM. While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent. To prevent this, the project POM contains empty overrides for these elements. If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides. 