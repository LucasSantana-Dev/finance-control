# UML Diagrams for Finance Control

This directory contains all the UML diagrams that document the Finance Control application architecture. Each diagram is stored as a separate PlantUML file for better organization and maintainability.

## Diagram Files

### Architecture Overview
- **`high-level-architecture.uml`** - Overall system architecture showing layers and their relationships
- **`layered-architecture.uml`** - Detailed class diagram showing inheritance hierarchy and base classes

### Database Design
- **`database-schema.uml`** - Entity-Relationship Diagram (ERD) showing all database tables and relationships
- **`database-indexes.uml`** - Database index strategy and performance optimization

### Domain Models
- **`entity-relationships.uml`** - Complete domain model showing all entities and their relationships
- **`package-structure.uml`** - Package organization and project structure

### Application Layers
- **`service-layer.uml`** - Service interfaces and implementations
- **`repository-layer.uml`** - Repository interfaces and data access patterns
- **`data-flow.uml`** - Sequence diagram showing request flow through the system

### Cross-Cutting Concerns
- **`security-architecture.uml`** - Authentication and authorization components
- **`validation-architecture.uml`** - Multi-layered validation approach

## How to View the Diagrams

### Option 1: PlantUML Online Server
1. Copy the content of any `.uml` file
2. Go to http://www.plantuml.com/plantuml/uml/
3. Paste the content and view the generated diagram

### Option 2: IDE Integration
- **VS Code**: Install the PlantUML extension
- **IntelliJ IDEA**: Install the PlantUML integration plugin
- **Eclipse**: Install the PlantUML plugin

### Option 3: Command Line
```bash
# Install PlantUML (requires Java)
java -jar plantuml.jar *.uml

# Generate specific diagram
java -jar plantuml.jar high-level-architecture.uml
```

### Option 4: Maven Plugin
Add to your `pom.xml`:
```xml
<plugin>
    <groupId>com.github.jeluard</groupId>
    <artifactId>plantuml-maven-plugin</artifactId>
    <version>1.1</version>
    <configuration>
        <sourceFiles>
            <sourceFile>docs/uml/*.uml</sourceFile>
        </sourceFiles>
        <outputDirectory>docs/uml/generated</outputDirectory>
    </configuration>
</plugin>
```

## Diagram Conventions

### Naming
- Use descriptive names that clearly indicate the diagram's purpose
- Follow the pattern: `{domain}-{type}.uml`
- Examples: `database-schema.uml`, `service-layer.uml`

### Styling
- Use consistent theming across all diagrams
- Apply the following skinparam settings:
  ```plantuml
  !theme plain
  skinparam backgroundColor #FFFFFF
  skinparam classAttributeIconSize 0
  skinparam classFontSize 10
  ```

### Organization
- Group related diagrams logically
- Use packages to organize components
- Maintain clear separation between different architectural layers

## Maintaining the Diagrams

### When to Update
- When adding new entities or relationships
- When modifying the database schema
- When changing service interfaces
- When updating package structure
- When adding new architectural patterns

### Best Practices
1. **Keep Diagrams in Sync**: Update diagrams when code changes
2. **Version Control**: All UML files are tracked in Git
3. **Consistency**: Ensure diagrams reflect the actual implementation
4. **Documentation**: Update the main architecture document when adding new diagrams

### Validation
- Verify that diagrams compile correctly with PlantUML
- Ensure relationships are accurately represented
- Check that inheritance hierarchies are correct
- Validate that all components are properly connected

## Tools and Resources

### PlantUML Resources
- **Official Documentation**: http://plantuml.com/
- **Language Reference**: http://plantuml.com/guide
- **Examples**: http://plantuml.com/examples

### IDE Extensions
- **VS Code**: PlantUML extension by jebbs
- **IntelliJ IDEA**: PlantUML integration
- **Eclipse**: PlantUML plugin

### Maven Integration
- **Maven Plugin**: plantuml-maven-plugin
- **Gradle Plugin**: gradle-plantuml-plugin

## Contributing

When adding or modifying diagrams:

1. **Follow Conventions**: Use established naming and styling conventions
2. **Update Documentation**: Modify this README if adding new diagram types
3. **Test Compilation**: Ensure diagrams compile without errors
4. **Review Relationships**: Verify that all relationships are accurate
5. **Update References**: Update the main architecture document to reference new diagrams

## Troubleshooting

### Common Issues
- **Compilation Errors**: Check PlantUML syntax and ensure all referenced classes exist
- **Missing Components**: Verify that all components are properly defined
- **Relationship Issues**: Ensure relationships use correct syntax and cardinality
- **Styling Problems**: Check skinparam settings and theme configuration

### Getting Help
- Check the PlantUML documentation for syntax issues
- Review existing diagrams for examples
- Use the PlantUML online server for quick testing
- Consult the main architecture documentation for context 