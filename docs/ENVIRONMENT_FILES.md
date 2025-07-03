# Environment Files Configuration

This document explains the different environment files in the Finance Control project and how to use them properly.

## Overview

The project uses two different environment files for different purposes:

1. **`.env`** - Main application configuration file
2. **`docker.env`** - Docker-specific configuration file

## File Purposes

### `.env` File

**Purpose**: Main application configuration file for local development and application settings.

**Contains**:
- Complete application configuration (50+ variables)
- Database connection settings
- Security configuration (JWT, CORS)
- Server settings
- Logging configuration
- JPA/Hibernate settings
- Flyway migration settings
- Actuator configuration
- OpenAPI documentation settings
- Pagination settings

**Usage**:
- Used by the Spring Boot application
- Loaded by the `spring-dotenv` dependency
- Contains all possible configuration options
- Used for local development

**Example**:
```env
# Database Configuration
DB_URL=jdbc:postgresql://localhost
DB_PORT=5432
DB_NAME=finance_control
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Security Configuration
JWT_SECRET=your-super-secret-jwt-key
JWT_EXPIRATION_MS=86400000

# Server Configuration
SERVER_PORT=8080
LOGGING_LEVEL=DEBUG
```

### `docker.env` File

**Purpose**: Docker-specific configuration for container deployment.

**Contains**:
- Docker Compose variables
- Container runtime settings
- Docker-specific overrides
- Build configuration
- Network settings

**Usage**:
- Used by Docker Compose
- Contains only essential variables for container deployment
- Overrides some `.env` values for Docker environment
- Used for production deployment

**Example**:
```env
# Docker Configuration
DOCKER_BUILDKIT=1
COMPOSE_DOCKER_CLI_BUILD=1

# Container Runtime
JAVA_OPTS=-Xmx2g -Xms1g -XX:+UseG1GC
GRADLE_OPTS=-Dorg.gradle.console=rich -Dorg.gradle.daemon=false

# Docker Overrides
SPRING_PROFILES_ACTIVE=prod
DEV_MODE=false
```

## Why Two Files?

### Separation of Concerns

1. **`.env`** - Application configuration
2. **`docker.env`** - Container/deployment configuration

### Different Environments

- **Development**: Uses `.env` for local development
- **Production**: Uses `docker.env` for container deployment

### Security

- **`.env`** - Contains development settings, may be committed to version control
- **`docker.env`** - Contains production overrides, should not be committed

## Setup Instructions

### 1. Create `.env` File

#### Option A: Use the Setup Script (Recommended)

**Linux/macOS**:
```bash
chmod +x scripts/environment/setup-env.sh
./scripts/environment/setup-env.sh
```

**Windows (PowerShell)**:
```powershell
.\scripts\setup-env.ps1
```

#### Option B: Manual Setup

```bash
# Copy the template
cp env-template.txt .env

# Edit the file with your values
nano .env
```

### 2. Configure `docker.env` (if needed)

The `docker.env` file is already configured with sensible defaults. You may need to modify it for:

- Production database credentials
- Different Java memory settings
- Custom Docker network configuration

## Environment Variable Priority

When using Docker Compose, the priority order is:

1. **Docker Compose environment variables** (highest)
2. **`docker.env` file**
3. **`.env` file**
4. **Default values** (lowest)

## Usage Examples

### Local Development

```bash
# Load .env file
source .env

# Start application
./gradlew bootRun
```

### Docker Development

```bash
# Use docker.env for Docker Compose
docker-compose --env-file docker.env up
```

### Production Deployment

```bash
# Use docker.env with production settings
docker-compose --env-file docker.env -f docker-compose.prod.yml up
```

## File Structure

```
finance-control/
├── .env                    # Main application configuration (create from template)
├── docker.env              # Docker-specific configuration
├── env-template.txt        # Template for .env file
├── scripts/
│   ├── setup-env.sh        # Bash setup script
│   └── setup-env.ps1       # PowerShell setup script
└── docs/
    ├── ENVIRONMENT_VARIABLES.md    # Complete environment variables reference
    └── ENVIRONMENT_FILES.md        # This file
```

## Best Practices

### 1. Never Commit Sensitive Data

```bash
# .gitignore should include
.env
docker.env
*.env
```

### 2. Use Templates

- Always use `env-template.txt` as a base for `.env`
- Keep `docker.env` minimal and focused

### 3. Environment-Specific Configuration

```env
# .env (Development)
SPRING_PROFILES_ACTIVE=dev
JPA_DDL_AUTO=update
LOGGING_LEVEL=DEBUG

# docker.env (Production)
SPRING_PROFILES_ACTIVE=prod
JPA_DDL_AUTO=validate
LOGGING_LEVEL=WARN
```

### 4. Security Considerations

- Change default JWT secret in production
- Use strong database passwords
- Restrict CORS origins in production
- Use environment-specific logging levels

## Troubleshooting

### Common Issues

1. **Environment variables not loading**
   - Check if `.env` file exists
   - Verify variable names match `env-template.txt`
   - Ensure `spring-dotenv` dependency is included

2. **Docker Compose not using correct values**
   - Check `docker.env` file exists
   - Verify Docker Compose references correct env file
   - Check variable priority order

3. **Configuration not applying**
   - Restart application after changing `.env`
   - Check Spring Boot logs for configuration errors
   - Verify `@ConfigurationProperties` is enabled

### Debug Configuration

```bash
# Check environment variables
env | grep DB_
env | grep JWT_
env | grep SERVER_

# Check application configuration
curl http://localhost:8080/api/config
```

## Migration from Old Configuration

If you're migrating from a previous configuration system:

1. **Backup existing configuration**
2. **Create `.env` from template**
3. **Map old variables to new structure**
4. **Test configuration**
5. **Update deployment scripts**

## Summary

- **`.env`** = Application configuration (development)
- **`docker.env`** = Container configuration (production)
- Use setup scripts for easy configuration
- Keep sensitive data out of version control
- Use environment-specific settings
- Follow security best practices

For complete environment variable reference, see [ENVIRONMENT_VARIABLES.md](ENVIRONMENT_VARIABLES.md). 