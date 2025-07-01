# Finance Control - Architecture Diagram

## Overview
This document provides a visual representation of the Finance Control application architecture, showing the layered structure, inheritance hierarchy, and component relationships.

## UML Diagrams

The architecture is documented through the following UML diagrams, each stored as a separate file in the `docs/uml/` directory:

### 1. High-Level Architecture
**File:** `docs/uml/high-level-architecture.uml`
- Shows the overall layered architecture from Client to Database
- Illustrates the flow between different architectural layers
- Demonstrates separation of concerns across the application

### 2. Database Schema
**File:** `docs/uml/database-schema.uml`
- Complete Entity-Relationship Diagram (ERD) of the database
- Shows all tables, fields, data types, and constraints
- Illustrates relationships between entities with cardinality
- Maps the complete database structure from migration files

### 3. Entity Relationships
**File:** `docs/uml/entity-relationships.uml`
- Detailed class diagram showing all domain entities
- Inheritance relationships from BaseEntity
- Associations between entities with multiplicity
- Business methods and properties for each entity

### 4. Layered Architecture with Inheritance
**File:** `docs/uml/layered-architecture.uml`
- Shows inheritance hierarchy for all base classes
- Demonstrates how domain classes extend base classes
- Illustrates the template method pattern implementation
- Shows the complete object model structure

### 5. Service Layer Architecture
**File:** `docs/uml/service-layer.uml`
- Service interfaces and their implementations
- Dependency relationships between services and repositories
- Domain-specific service methods
- Shows the business logic layer structure

### 6. Repository Layer Architecture
**File:** `docs/uml/repository-layer.uml`
- Base repository interface and domain-specific repositories
- Query methods for each repository
- Inheritance relationships
- Shows the data access layer structure

### 7. Package Structure
**File:** `docs/uml/package-structure.uml`
- Complete package organization showing all domains
- Clear separation between shared infrastructure and domain-specific code
- Sub-package structure for complex domains like transactions
- Shows the overall project organization

### 8. Data Flow Architecture
**File:** `docs/uml/data-flow.uml`
- Sequence diagram showing how requests flow through the system
- Interaction between all layers during a typical operation
- Illustrates the complete request-response cycle

### 9. Security Architecture
**File:** `docs/uml/security-architecture.uml`
- JWT authentication flow
- Security components and their relationships
- Authorization mechanisms
- User context and role-based access

### 10. Validation Architecture
**File:** `docs/uml/validation-architecture.uml`
- Multi-layered validation approach
- Different validation strategies and their relationships
- Shows validation flow from DTO to business rules

### 11. Database Index Strategy
**File:** `docs/uml/database-indexes.uml`
- Visual representation of all database indexes
- Categorized by type (Primary, Unique, Performance, Foreign Key)
- Shows the performance optimization strategy

## Key Design Patterns

### 1. Template Method Pattern
- **BaseService**: Provides common CRUD operations with abstract methods for domain-specific logic
- **BaseController**: Provides standard REST endpoints with customizable behavior
- **BaseEntity**: Common audit fields and ID management

### 2. Strategy Pattern
- **Validation**: Different validation strategies for different operations (create, update, response)
- **Repository**: Different query strategies using specifications

### 3. Factory Pattern
- **EntityMapper**: Creates entities from DTOs and vice versa
- **SpecificationUtils**: Creates JPA specifications for dynamic queries

### 4. Observer Pattern
- **JPA Auditing**: Automatic timestamp management
- **Event-driven validation**: Validation triggered by entity lifecycle events

### 5. Composite Pattern
- **Transaction Responsibilities**: Complex transaction distribution logic
- **Category Hierarchy**: Category and subcategory relationships

### 6. Repository Pattern
- **Data Access Abstraction**: Consistent data access across all domains
- **Query Specifications**: Dynamic query building for complex searches

## Benefits of This Architecture

1. **Consistency**: All domains follow the same patterns and conventions
2. **Maintainability**: Common functionality is centralized in base classes
3. **Extensibility**: Easy to add new domains by extending base classes
4. **Testability**: Clear separation of concerns enables focused testing
5. **Security**: Centralized security with user context and role-based access
6. **Validation**: Multi-layered validation ensures data integrity
7. **Performance**: Optimized queries with specifications and pagination
8. **Documentation**: Automatic API documentation with OpenAPI/Swagger
9. **Scalability**: Modular design allows for easy scaling and feature additions
10. **Data Integrity**: Proper foreign key constraints and validation rules

## How to Use These Diagrams

### Viewing the Diagrams
1. **PlantUML**: Use any PlantUML-compatible tool or IDE plugin
2. **Online Viewer**: Use the PlantUML online server
3. **IDE Integration**: Most IDEs support PlantUML rendering
4. **Documentation**: Generate PNG/SVG images for documentation

### Maintaining the Diagrams
1. **Keep in Sync**: Update diagrams when code changes
2. **Version Control**: All UML files are version controlled
3. **Consistency**: Ensure diagrams reflect the actual implementation
4. **Documentation**: Update this file when adding new diagrams

### Tools and Resources
- **PlantUML**: http://plantuml.com/
- **VS Code Extension**: PlantUML extension
- **IntelliJ IDEA**: PlantUML integration plugin
- **Maven Plugin**: plantuml-maven-plugin for automated generation 