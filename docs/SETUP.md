# Finance Control - Complete Setup Guide

This comprehensive guide covers environment setup, Docker configuration, Java/JDK configuration, and getting started with the Finance Control application.

## Table of Contents

1. [Quick Start Guide](#quick-start-guide)
2. [Environment Setup](#environment-setup)
3. [Docker Configuration](#docker-configuration)
4. [Java/JDK Setup](#javajdk-setup)
5. [Troubleshooting](#troubleshooting)
6. [Additional Resources](#additional-resources)

---

## Quick Start Guide

### Prerequisites

- **Docker Desktop** (Windows/Mac) or **Docker Engine** (Linux)
- **Docker Compose** (usually included with Docker Desktop)
- **JDK 21** (optional if using Docker exclusively)
- **Git**

### Fastest Way to Get Started

#### Option A: Docker Only (No Local Java Installation Required)

```bash
# 1. Clone the repository
git clone <repository-url>
cd finance-control

# 2. Build Docker image
./scripts/docker/docker-manager.sh build

# 3. Start development environment
./scripts/docker/docker-manager.sh dev

# 4. Open shell in container
./scripts/docker/docker-manager.sh shell

# 5. Run commands inside container
./gradlew build
./gradlew test
./scripts/quality/quality-check.sh
```

#### Option B: Local Development with JDK 21

```bash
# 1. Clone the repository
git clone <repository-url>
cd finance-control

# 2. Setup environment variables (Automated)
# Linux/macOS
./scripts/environment/setup-env.sh

# Windows (PowerShell)
.\scripts\setup-env.ps1

# 3. Setup Java environment
# Linux/macOS
./scripts/setup-java-env.sh

# Windows (PowerShell)
.\scripts\setup-java-env.ps1

# 4. Build and run
./gradlew build
./gradlew bootRun
```

### Verify Installation

```bash
# Check Docker
docker --version
docker-compose --version
docker info

# Check Java (if installed locally)
java -version
echo $JAVA_HOME  # Linux/macOS
echo %JAVA_HOME%  # Windows CMD
echo $env:JAVA_HOME  # Windows PowerShell

# Test Gradle
./gradlew --version
```

---

## Environment Setup

### Overview

The application uses a centralized configuration system based on `@ConfigurationProperties` that binds environment variables to typed Java properties, providing:

- **Type Safety**: All configuration values are strongly typed
- **Centralized Management**: All configuration is managed through `AppProperties`
- **Environment Flexibility**: Easy configuration for different environments
- **Validation**: Built-in validation and default values
- **Documentation**: Self-documenting configuration structure

### Environment Files

The project uses two different environment files:

1. **`.env`** - Main application configuration file
2. **`docker.env`** - Docker-specific configuration file

#### `.env` File

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

#### `docker.env` File

**Purpose**: Docker-specific configuration for container deployment.

**Contains**:
- Docker Compose variables
- Container runtime settings
- Docker-specific overrides
- Build configuration
- Network settings

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

### Setup Instructions

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

### Environment Variable Priority

When using Docker Compose, the priority order is:

1. **Docker Compose environment variables** (highest)
2. **`docker.env` file**
3. **`.env` file**
4. **Default values** (lowest)

### Key Environment Variables Reference

#### Database Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost` | Database connection URL |
| `DB_PORT` | `5432` | Database port |
| `DB_NAME` | `finance_control` | Database name |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `DB_POOL_MAX_SIZE` | `20` | Maximum connection pool size |

#### Security Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | `defaultSecretKeyForDevelopmentOnly` | JWT secret key |
| `JWT_EXPIRATION_MS` | `86400000` | JWT expiration time (ms) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:8080` | Allowed CORS origins |

#### Server Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8080` | Server port |
| `LOGGING_LEVEL` | `INFO` | Root logging level |

For a complete list of all environment variables, see the `env-template.txt` file.

### Environment-Specific Configuration

#### Development Environment

```env
SPRING_PROFILES_ACTIVE=dev
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true
LOGGING_LEVEL=DEBUG
ACTUATOR_ENABLED=true
```

#### Production Environment

```env
SPRING_PROFILES_ACTIVE=prod
JPA_DDL_AUTO=validate
JPA_SHOW_SQL=false
LOGGING_LEVEL=WARN
ACTUATOR_ENABLED=true
ACTUATOR_SHOW_DETAILS=never
```

#### Test Environment

```env
SPRING_PROFILES_ACTIVE=test
JPA_DDL_AUTO=create-drop
FLYWAY_ENABLED=false
ACTUATOR_ENABLED=false
```

---

## Docker Configuration

### Overview

The project is designed to run entirely within Docker containers, ensuring:
- **Consistent environments** across different operating systems
- **No local Java/Gradle installation** required
- **Isolated development environments**
- **Easy deployment and testing**

### Docker Services

#### Core Services

**Database (db)**
- **Image**: `postgres:17`
- **Port**: `5432`
- **Environment**: Configurable via `docker.env`
- **Health Check**: Automatic PostgreSQL readiness check

**Application (app)**
- **Build**: From `Dockerfile`
- **Port**: `8080` (configurable)
- **Debug Port**: `5005`
- **Dependencies**: Database
- **Environment**: Spring Boot configuration

#### Development Services

**Development Container (dev)**
- **Profile**: `dev`
- **Purpose**: Interactive development
- **Volumes**: Source code, build artifacts, logs
- **Command**: Keeps container running for interactive use

**Quality Check (quality)**
- **Profile**: `quality`
- **Purpose**: Run code quality checks
- **Command**: `./gradlew qualityCheck`

**Test Runner (test)**
- **Profile**: `test`
- **Purpose**: Run tests
- **Environment**: Test profile
- **Command**: `./gradlew test`

**Build Runner (build)**
- **Profile**: `build`
- **Purpose**: Build application
- **Command**: `./gradlew build`

### Available Scripts

#### Docker Manager Script

The main script for all Docker operations:

```bash
./scripts/docker/docker-manager.sh <command>
```

**Build Commands**
- `build [target]` - Build Docker image (default: base)
- `rebuild [target]` - Force rebuild Docker image

**Service Management**
- `start [service]` - Start services (default: all)
- `stop [service]` - Stop services (default: all)
- `restart [service]` - Restart services
- `status` - Show service status

**Development**
- `dev` - Start development environment
- `shell` - Open shell in dev container
- `app` - Start application with database
- `db` - Start database only

**Scripts**
- `check-env` - Run environment check
- `fix-env` - Run environment fix
- `quality` - Run quality checks
- `test` - Run tests
- `build-app` - Run build

**Gradle Commands**
- `gradle <task>` - Run Gradle task
- `build` - Run Gradle build
- `test` - Run Gradle tests
- `qualityCheck` - Run quality checks

**Maintenance**
- `logs [service]` - Show logs (default: app)
- `logs-follow [service]` - Follow logs
- `cleanup` - Clean up Docker resources

### Docker Desktop Integration

#### Using Docker Desktop GUI

1. Open Docker Desktop
2. Go to the "Containers" tab
3. Click "Run" and select the `docker-compose.override.yml` file
4. Choose the desired profile in the "Profiles" section

#### Command Line Examples

**Code Quality**
```bash
# Run all quality checks
docker-compose --profile quality-check up gradle-quality-check

# Individual checks
docker-compose --profile checkstyle up gradle-checkstyle
docker-compose --profile pmd up gradle-pmd
docker-compose --profile spotbugs up gradle-spotbugs
docker-compose --profile jacoco up gradle-jacoco
```

**Build and Tests**
```bash
# Complete build
docker-compose --profile build up gradle-build

# Run tests
docker-compose --profile test up gradle-test
```

### Development Workflow

#### 1. Initial Setup
```bash
# Build the base image
./scripts/docker/docker-manager.sh build

# Start development environment
./scripts/docker/docker-manager.sh dev

# Open shell in container
./scripts/docker/docker-manager.sh shell
```

#### 2. Daily Development
```bash
# Start development environment
./scripts/docker/docker-manager.sh dev

# Run commands in container
./scripts/docker/docker-manager.sh gradle build
./scripts/docker/docker-manager.sh gradle test
./scripts/docker/docker-manager.sh quality

# Or open shell for interactive work
./scripts/docker/docker-manager.sh shell
```

#### 3. Testing
```bash
# Run tests
./scripts/docker/docker-manager.sh test

# Run quality checks
./scripts/docker/docker-manager.sh quality

# Run environment check
./scripts/docker/docker-manager.sh check-env
```

#### 4. Application Deployment
```bash
# Start application with database
./scripts/docker/docker-manager.sh app

# Check logs
./scripts/docker/docker-manager.sh logs app

# Follow logs
./scripts/docker/docker-manager.sh logs-follow app
```

### Docker Configuration File

Create a `docker.env` file in the project root:

```bash
# Database Configuration
POSTGRES_DB=finance_control
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_PORT=5432

# Application Configuration
APPLICATION_PORT=8080
SPRING_PROFILES_ACTIVE=prod
DEV_MODE=false

# Java Configuration
JAVA_OPTS=-Xmx2g -Xms1g -XX:+UseG1GC

# Gradle Configuration
GRADLE_OPTS=-Dorg.gradle.console=rich -Dorg.gradle.daemon=false

# Logging Configuration
LOG_LEVEL=INFO

# Docker Configuration
DOCKER_BUILDKIT=1
COMPOSE_DOCKER_CLI_BUILD=1
```

### Docker Compose Profiles

The project uses Docker Compose profiles to organize services:

- **Default**: `db`, `app`
- **dev**: `dev` (development container)
- **quality**: `quality` (quality checks)
- **test**: `test` (test runner)
- **build**: `build` (build runner)
- **check-env**: `check-env` (environment check)
- **fix-env**: `fix-env` (environment fix)
- **shell**: `shell` (interactive shell)

---

## Java/JDK Setup

### Objective

Configure the environment to use JDK 21, allowing Gradle and the application to work correctly.

### Configuration Methods

#### Method 1: Automatic Script (Recommended)

**Windows (PowerShell)**
```powershell
# Run the PowerShell script
.\scripts\setup-java-env.ps1
```

**Windows/Linux/macOS (Bash)**
```bash
# Run the Bash script
./scripts/setup-java-env.sh
```

#### Method 2: Manual Configuration

**1. Create .env file**
```bash
# Copy the template
cp env-template.txt .env
```

**2. Edit the .env file**
```env
# Java Configuration
JAVA_HOME=C:\Program Files\Java\jdk-21
JAVA_VERSION=21

# Other configurations...
```

**3. Configure environment variables**

**Windows (PowerShell):**
```powershell
# Load variables from .env file
. .env

# Verify configuration
echo $env:JAVA_HOME
java -version
```

**Linux/macOS:**
```bash
# Load variables from .env file
source .env

# Verify configuration
echo $JAVA_HOME
java -version
```

### Configuration Verification

**1. Check JAVA_HOME**
```bash
# Windows (PowerShell)
echo $env:JAVA_HOME

# Windows (CMD)
echo %JAVA_HOME%

# Linux/macOS
echo $JAVA_HOME
```

**2. Check Java version**
```bash
java -version
```

**Expected output:**
```
openjdk version "21.0.1" 2023-10-17
OpenJDK Runtime Environment (build 21.0.1+12-29)
OpenJDK 64-Bit Server VM (build 21.0.1+12-29, mixed mode, sharing)
```

**3. Test Gradle**
```bash
./gradlew --version
```

### Permanent Configuration (Windows)

#### 1. System Environment Variables
1. Open "System Environment Variables"
2. Click "Environment Variables..."
3. In "System Variables", click "New..."
4. Variable name: `JAVA_HOME`
5. Variable value: `C:\Program Files\Java\jdk-21`
6. Click "OK"

#### 2. Add to PATH
1. Select the "Path" variable in "System Variables"
2. Click "Edit..."
3. Click "New..."
4. Add: `%JAVA_HOME%\bin`
5. Click "OK"

#### 3. Verify
1. Open a new terminal
2. Run: `java -version`
3. Run: `echo %JAVA_HOME%`

### JDK 21 Distributions

#### Oracle JDK 21
- **Download**: https://www.oracle.com/java/technologies/downloads/#java21
- **Default path**: `C:\Program Files\Java\jdk-21`

#### Eclipse Temurin (OpenJDK)
- **Download**: https://adoptium.net/temurin/releases/?version=21
- **Default path**: `C:\Program Files\Eclipse Adoptium\jdk-21`

#### Microsoft OpenJDK
- **Download**: https://docs.microsoft.com/en-us/java/openjdk/download
- **Default path**: `C:\Program Files\Microsoft\jdk-21`

### Configuration with Docker

If you prefer to use only Docker (without installing Java locally):

**1. Use only Docker**
```bash
# Build and execution via Docker
docker-compose --profile build up gradle-build
docker-compose -f docker-compose.dev.yml up -d
```

**2. Hybrid configuration**
```bash
# Configure JAVA_HOME for local development
# Use Docker for production
```

### Next Steps After Java Setup

1. **Test the build:**
   ```bash
   ./gradlew build
   ```

2. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

3. **Quality checks:**
   ```bash
   ./gradlew qualityCheck
   ```

4. **Docker (optional):**
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

---

## Troubleshooting

### Environment Variable Issues

#### Environment variables not loading
- Check if `.env` file exists
- Verify variable names match `env-template.txt`
- Ensure `spring-dotenv` dependency is included

#### Docker Compose not using correct values
- Check `docker.env` file exists
- Verify Docker Compose references correct env file
- Check variable priority order

#### Configuration not applying
- Restart application after changing `.env`
- Check Spring Boot logs for configuration errors
- Verify `@ConfigurationProperties` is enabled

**Debug Configuration:**
```bash
# Check environment variables
env | grep DB_
env | grep JWT_
env | grep SERVER_

# Check application configuration
curl http://localhost:8080/api/config
```

### Docker Issues

#### Docker Not Running
```bash
# Check Docker status
docker info

# Start Docker Desktop (Windows/Mac)
# Or start Docker service (Linux)
sudo systemctl start docker
```

#### Port Conflicts
```bash
# Check what's using the port
netstat -tulpn | grep :8080

# Change port in docker.env
APPLICATION_PORT=8081
```

#### Permission Issues
```bash
# Make scripts executable
chmod +x scripts/*.sh

# Fix Docker permissions (Linux)
sudo usermod -aG docker $USER
```

#### Build Failures
```bash
# Clean and rebuild
./scripts/docker/docker-manager.sh cleanup
./scripts/docker/docker-manager.sh rebuild

# Check logs
./scripts/docker/docker-manager.sh logs build
```

#### Database Connection Issues
```bash
# Check database status
./scripts/docker/docker-manager.sh status

# Restart database
./scripts/docker/docker-manager.sh restart db

# Check database logs
./scripts/docker/docker-manager.sh logs db
```

#### Container doesn't start
```bash
# Check logs
docker-compose logs [service-name]

# Check if Dockerfile.dev exists
ls -la Dockerfile.dev
```

#### Volumes not mounted
```bash
# Check if files exist
ls -la build.gradle settings.gradle

# Check permissions
chmod +x gradlew
```

#### Gradle can't find dependencies
```bash
# Clear Gradle cache
docker-compose --profile build up gradle-build --build
```

### Java/JDK Issues

#### JAVA_HOME not found
```bash
# Check if JDK is installed
ls "C:\Program Files\Java\"

# Check if path is correct
echo $env:JAVA_HOME
```

#### Gradle doesn't work
```bash
# Check Java version
java -version

# Check if Gradle wrapper exists
ls gradlew*

# Run with verbose
./gradlew --version --info
```

#### Docker can't find Java
```bash
# Check if Dockerfile.dev is correct
cat Dockerfile.dev

# Rebuild image
docker-compose build --no-cache
```

### Debug Commands

#### Check Container Status
```bash
# List all containers
docker ps -a

# Check container logs
docker logs <container-name>

# Inspect container
docker inspect <container-name>
```

#### Check Images
```bash
# List images
docker images

# Remove unused images
docker image prune -f
```

#### Check Volumes
```bash
# List volumes
docker volume ls

# Inspect volume
docker volume inspect <volume-name>
```

### Monitoring and Logging

#### Application Logs
```bash
# View application logs
./scripts/docker/docker-manager.sh logs app

# Follow logs in real-time
./scripts/docker/docker-manager.sh logs-follow app

# View logs for specific service
./scripts/docker/docker-manager.sh logs db
```

#### Build Logs
- Build logs are saved to `logs/`
- Timestamped log files for each operation
- Quality check reports in `build/reports/`

#### Health Checks
- Database has automatic health checks
- Application health endpoint at `/actuator/health`
- Container restart policies configured

---

## Additional Resources

### File Structure

```
finance-control/
├── .env                          # Main application configuration (create from template)
├── docker.env                    # Docker-specific configuration
├── env-template.txt              # Template for .env file
├── docker-compose.yml            # Docker Compose configuration
├── Dockerfile                    # Multi-stage Docker build
├── scripts/
│   ├── docker/
│   │   ├── docker-manager.sh     # Main Docker management script
│   │   └── docker-run.sh         # Alternative Docker runner
│   ├── environment/
│   │   ├── setup-env.sh          # Bash setup script
│   │   └── setup-env.ps1         # PowerShell setup script
│   ├── setup-java-env.sh         # Bash Java setup script
│   └── setup-java-env.ps1        # PowerShell Java setup script
├── build/                        # Build artifacts (mounted)
├── logs/                         # Application logs (mounted)
├── src/                          # Source code (mounted)
└── docs/
    └── SETUP.md                  # This file
```

### Best Practices

1. **Use Environment Variables**: Always use environment variables for configuration
2. **Default Values**: Provide sensible defaults for all properties
3. **Validation**: Validate critical configuration values
4. **Documentation**: Document all configuration options
5. **Security**: Never commit secrets to version control
6. **Profiles**: Use Spring profiles for environment-specific configuration
7. **Monitoring**: Log configuration values for debugging
8. **Type Safety**: Use typed properties instead of raw strings
9. **Use Docker for development**: All development should be done in containers
10. **Test in Docker**: Ensure all tests pass in Docker environment

### Security Considerations

#### Production Security

1. **Change JWT Secret**: Always change `JWT_SECRET` in production
2. **Database Credentials**: Use strong passwords
3. **CORS Origins**: Restrict to specific domains
4. **Actuator Security**: Configure appropriate access controls
5. **Logging**: Avoid logging sensitive information

**Production example:**
```env
JWT_SECRET=your-super-secret-production-jwt-key-here
DB_PASSWORD=your-strong-database-password
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://api.yourdomain.com
ACTUATOR_SHOW_DETAILS=never
```

#### .gitignore Configuration

```bash
# .gitignore should include
.env
docker.env
*.env
```

### External Documentation

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Spring Boot Configuration Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)

### Project Documentation

- `docs/API_PATTERNS.md` - API documentation
- `README.md` - Main project README
- `CHANGELOG.md` - Project changelog

### Getting Help

```bash
# Show help for docker-manager
./scripts/docker/docker-manager.sh help

# Show help for specific script
./scripts/help.sh docker-manager
```

### Important Notes

1. **`.env` file**: Is in `.gitignore` to not be committed
2. **Scripts**: Automatically detect different JDK distributions
3. **Docker**: Uses Java 21 isolated in container
4. **Compatibility**: Gradle 8.7 officially supports Java 21
5. **Profiles**: Use `--profile` to run specific services
6. **Volumes**: Reports are persisted on the host
7. **Cache**: Gradle cache is maintained between runs
8. **Resources**: Each service uses independent resources

---

## Summary

This setup guide covers:
- **Quick Start**: Fastest way to get the application running
- **Environment Setup**: Comprehensive configuration management
- **Docker Configuration**: Complete containerized development workflow
- **Java/JDK Setup**: JDK 21 installation and configuration
- **Troubleshooting**: Solutions for common issues

Choose the approach that best fits your needs:
- **Docker-only**: No local Java installation required, fully isolated
- **Local Development**: Direct Java/Gradle usage with optional Docker for deployment
- **Hybrid**: Local development with Docker for testing and production

The Finance Control application provides flexible configuration options to support various development and deployment scenarios.
