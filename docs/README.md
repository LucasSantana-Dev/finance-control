# Finance Control - Documentation

Welcome to the Finance Control documentation. This comprehensive guide covers all aspects of the application, from getting started to advanced development topics.

## Documentation Structure

### Getting Started

Essential documentation for setting up and understanding the project:

- **[README.md](../README.md)** - Project overview, quick start guide, and installation instructions
- **[SETUP.md](SETUP.md)** - Detailed setup instructions for local development environment
- **[ENVIRONMENT.md](ENVIRONMENT.md)** - Environment configuration and variables

### Development

Core development guides and patterns:

- **[DEVELOPMENT.md](DEVELOPMENT.md)** - Comprehensive development guide covering:
  - Code quality standards and tools
  - Dependency management
  - Base classes architecture
  - MapStruct integration
  - Naming conventions
  - Logging configuration
  - Development workflow
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System architecture diagrams and design patterns
- **[TESTING.md](TESTING.md)** - Testing strategy, best practices, and API testing guide

### API Reference

Documentation for API development and usage:

- **[API.md](API.md)** - REST API patterns, conventions, and best practices
- **[FEATURES.md](FEATURES.md)** - Comprehensive overview of all application features:
  - Transaction Management
  - Category Management
  - Financial Goals
  - Dashboard and Analytics
  - Feature Flags

### Operations

Deployment, CI/CD, and security documentation:

- **[DEPLOYMENT.md](DEPLOYMENT.md)** - CI/CD setup, quality gates, and deployment guide
- **[SECURITY.md](SECURITY.md)** - Security best practices and guidelines
- **[DOCKER.md](DOCKER.md)** - Docker setup, integration, and desktop usage

## Quick Navigation

### For New Developers

1. Start with the main **[README.md](../README.md)** for project overview
2. Follow **[SETUP.md](SETUP.md)** to set up your development environment
3. Review **[DEVELOPMENT.md](DEVELOPMENT.md)** for coding standards and patterns
4. Read **[ARCHITECTURE.md](ARCHITECTURE.md)** to understand the system design
5. Check **[TESTING.md](TESTING.md)** for testing guidelines

### For API Development

1. **[API.md](API.md)** - REST endpoint patterns and conventions
2. **[FEATURES.md](FEATURES.md)** - Complete feature documentation
3. **[DEVELOPMENT.md](DEVELOPMENT.md)** - Base classes and MapStruct integration
4. **[TESTING.md](TESTING.md)** - API testing with Postman

### For DevOps

1. **[DEPLOYMENT.md](DEPLOYMENT.md)** - CI/CD pipeline and quality gates
2. **[DOCKER.md](DOCKER.md)** - Container configuration and deployment
3. **[SECURITY.md](SECURITY.md)** - Security best practices
4. **[ENVIRONMENT.md](ENVIRONMENT.md)** - Environment configuration

## Documentation Standards

All documentation follows these standards:

- **Markdown format** for easy reading and version control
- **Clear structure** with headers and sections
- **Code examples** for practical implementation
- **Cross-references** between related documents
- **Regular updates** to reflect current codebase

## Key Features Documented

### Transaction Management
- CRUD operations for income and expenses
- Category and subcategory organization
- Shared responsibilities and splitting
- Advanced filtering and search

### Financial Goals
- Goal setting and tracking
- Progress monitoring
- Priority management
- Deadline tracking

### Dashboard and Analytics
- Financial summaries and metrics
- Spending analysis by category
- Income vs expenses tracking
- Trend analysis and reporting

### Feature Flags
- Flexible feature toggling
- Environment-based configuration
- Centralized feature management

## Recent Updates

- **Documentation Consolidation** - Merged related files for better organization:
  - Logging documentation merged into DEVELOPMENT.md
  - Postman testing guide merged into TESTING.md
  - Created comprehensive FEATURES.md
- **File Renaming** - Improved naming clarity:
  - ARCHITECTURE_DIAGRAM.md → ARCHITECTURE.md
  - CI_CD_SETUP.md → DEPLOYMENT.md
- **Cleanup** - Removed obsolete files:
  - API_RESPONSE_IMPLEMENTATION.md (covered in API.md)
  - POSTGRESQL_INTEGRATION_STATUS.md (outdated status doc)
  - POSTMAN_COLLECTION_SUMMARY.md (artifact)
  - UI_UX_LOVABLE_PROMPT.md (frontend-specific)
  - TRANSACTION_CATEGORIES_MANAGEMENT.md (merged into FEATURES.md)
  - FEATURE_FLAGS.md (merged into FEATURES.md)

## Contributing to Documentation

When adding new documentation:

1. Follow the existing structure and format
2. Include practical examples
3. Cross-reference related documents
4. Update this README with new entries
5. Ensure accuracy with current codebase
6. Consider consolidating related information to reduce redundancy

## Documentation Goals

- **Comprehensive coverage** of all major components
- **Clear examples** for common use cases
- **Consistent structure** across all documents
- **Easy navigation** with clear organization
- **Regular maintenance** to keep information current
- **Consolidated information** to reduce redundancy

## Support

For questions or issues:

1. Check the relevant documentation section
2. Review code examples and patterns
3. Consult API documentation at `http://localhost:8080/swagger-ui.html`
4. Check GitHub issues and discussions

---

*Last updated: January 2025*
