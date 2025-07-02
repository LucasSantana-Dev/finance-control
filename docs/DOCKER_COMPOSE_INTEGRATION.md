# Docker Compose Integration Improvements

## Overview

The `docker-compose.yml` has been enhanced to provide better integration with the scripts and development workflow.

## New Services

### 1. Development Service (`dev`)
- **Profile**: `dev`
- **Purpose**: Interactive development container
- **Usage**: 
  ```bash
  ./scripts/docker-compose-run.sh dev
  docker-compose exec dev ./gradlew build
  docker-compose exec dev ./scripts/quality-check.sh
  ```

### 2. Quality Check Service (`quality`)
- **Profile**: `quality`
- **Purpose**: Run quality checks in isolated container
- **Usage**:
  ```bash
  ./scripts/docker-compose-run.sh quality
  ```

### 3. Test Service (`test`)
- **Profile**: `test`
- **Purpose**: Run tests with database dependency
- **Usage**:
  ```bash
  ./scripts/docker-compose-run.sh test
  ```

### 4. Build Service (`build`)
- **Profile**: `build`
- **Purpose**: Run full build in isolated container
- **Usage**:
  ```bash
  ./scripts/docker-compose-run.sh build
  ```

## Improvements Made

### 1. **Health Checks**
- Database service now has health checks
- Application waits for database to be healthy before starting

### 2. **Volume Management**
- Added `gradle-cache` volume for faster builds
- Mounted `./logs` and `./build` directories
- Source code mounted for development

### 3. **Environment Variables**
- Default values for all environment variables
- Better database URL configuration
- Gradle options configured for rich console output

### 4. **Service Dependencies**
- Proper dependency management with health checks
- Services start in correct order

### 5. **Profiles**
- Services organized into profiles for selective startup
- `dev`, `quality`, `test`, `build` profiles available

## Usage Examples

### Quick Commands
```bash
# Start development environment
./scripts/docker-compose-run.sh dev

# Run quality checks
./scripts/docker-compose-run.sh quality

# Run tests
./scripts/docker-compose-run.sh test

# Run build
./scripts/docker-compose-run.sh build

# Start full application
./scripts/docker-compose-run.sh app
```

### Interactive Development
```bash
# Start dev container
./scripts/docker-compose-run.sh dev

# Run commands in container
docker-compose exec dev ./gradlew build
docker-compose exec dev ./gradlew qualityCheck
docker-compose exec dev ./scripts/quality-check.sh
docker-compose exec dev ./scripts/gradle-with-logs.sh test
```

### Docker Desktop Integration
- All services appear in Docker Desktop GUI
- Can start/stop services from GUI
- Logs and volumes visible in GUI
- Health status displayed

## Benefits

1. **Consistent Environment**: All developers use the same environment
2. **Isolated Services**: Each task runs in its own container
3. **Better Performance**: Gradle cache shared between runs
4. **Health Monitoring**: Database health checks prevent startup issues
5. **Easy Debugging**: Interactive development container available
6. **GUI Integration**: Services visible in Docker Desktop

## Environment Variables

Create a `.env` file with:
```bash
# Database Configuration
POSTGRES_DB=finance_control
DB_USERNAME=postgres
DB_PASSWORD=password
DB_PORT=5432

# Application Configuration
APPLICATION_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Development Configuration
DEV_MODE=false

# Gradle Configuration
GRADLE_OPTS=-Dorg.gradle.console=rich -Dorg.gradle.daemon=false
```

## Migration from Old Scripts

The existing scripts (`quality-check.sh`, `gradle-with-logs.sh`, etc.) still work and will use Docker when available. The new Docker Compose services provide an alternative, more integrated approach.

### Comparison

| Approach | Pros | Cons |
|----------|------|------|
| **Existing Scripts** | Simple, direct | Manual Docker management |
| **Docker Compose Services** | Integrated, GUI visible | More complex setup |

Both approaches work together - use what fits your workflow best! 