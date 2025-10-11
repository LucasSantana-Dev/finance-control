# Docker Setup and Integration

This document provides comprehensive information about Docker usage in the Finance Control application, covering setup, integration, and desktop usage.

## 🐳 Overview

The project is designed to run entirely within Docker containers, ensuring:
- **Consistent environments** across different operating systems
- **No local Java/Gradle installation** required
- **Isolated development environments**
- **Easy deployment and testing**

## 📋 Prerequisites

### Required Software
- **Docker Desktop** (Windows/Mac) or **Docker Engine** (Linux)
- **Docker Compose** (usually included with Docker Desktop)

### Verify Installation
```bash
# Check Docker
docker --version

# Check Docker Compose
docker-compose --version

# Verify Docker is running
docker info
```

## 🚀 Quick Start

### 1. Clone and Navigate
```bash
git clone <repository-url>
cd finance-control
```

### 2. Build and Start
```bash
# Build the Docker image
./scripts/docker/docker-manager.sh build

# Start development environment
./scripts/docker/docker-manager.sh dev

# Open shell in container
./scripts/docker/docker-manager.sh shell
```

### 3. Run Commands
```bash
# Inside the container shell:
./gradlew build
./gradlew test
./scripts/quality/quality-check.sh
```

## 🛠️ Available Scripts

### Docker Manager Script
The main script for all Docker operations:

```bash
./scripts/docker/docker-manager.sh <command>
```

#### Build Commands
- `build [target]` - Build Docker image (default: base)
- `rebuild [target]` - Force rebuild Docker image

#### Service Management
- `start [service]` - Start services (default: all)
- `stop [service]` - Stop services (default: all)
- `restart [service]` - Restart services
- `status` - Show service status

#### Development
- `dev` - Start development environment
- `shell` - Open shell in dev container
- `app` - Start application with database
- `db` - Start database only

#### Scripts
- `check-env` - Run environment check
- `fix-env` - Run environment fix
- `quality` - Run quality checks
- `test` - Run tests
- `build-app` - Run build

#### Gradle Commands
- `gradle <task>` - Run Gradle task
- `build` - Run Gradle build
- `test` - Run Gradle tests
- `qualityCheck` - Run quality checks

#### Maintenance
- `logs [service]` - Show logs (default: app)
- `logs-follow [service]` - Follow logs
- `cleanup` - Clean up Docker resources

### Docker Run Script
Alternative script for running commands in Docker:

```bash
./scripts/docker/docker-run.sh <command>
```

#### Available Commands
- `check-env` - Run environment check in Docker
- `fix-env` - Run environment fix in Docker
- `quality` - Run quality checks in Docker
- `test` - Run tests in Docker
- `build` - Run build in Docker
- `gradle <task>` - Run any Gradle task in Docker
- `dev` - Start development container
- `app` - Start application with database
- `shell` - Open shell in container

## 🏗️ Docker Services

### Core Services

#### Database (db)
- **Image**: `postgres:17`
- **Port**: `5432`
- **Environment**: Configurable via `docker.env`
- **Health Check**: Automatic PostgreSQL readiness check

#### Application (app)
- **Build**: From `Dockerfile`
- **Port**: `8080` (configurable)
- **Debug Port**: `5005`
- **Dependencies**: Database
- **Environment**: Spring Boot configuration

### Development Services

#### Development Container (dev)
- **Profile**: `dev`
- **Purpose**: Interactive development
- **Volumes**: Source code, build artifacts, logs
- **Command**: Keeps container running for interactive use

#### Quality Check (quality)
- **Profile**: `quality`
- **Purpose**: Run code quality checks
- **Command**: `./gradlew qualityCheck`

#### Test Runner (test)
- **Profile**: `test`
- **Purpose**: Run tests
- **Environment**: Test profile
- **Command**: `./gradlew test`

#### Build Runner (build)
- **Profile**: `build`
- **Purpose**: Build application
- **Command**: `./gradlew build`

### Utility Services

#### Environment Check (check-env)
- **Profile**: `check-env`
- **Purpose**: Validate environment
- **Command**: `./scripts/environment/check-environment.sh`

#### Environment Fix (fix-env)
- **Profile**: `fix-env`
- **Purpose**: Fix environment issues
- **Command**: `./scripts/environment/fix-environment.sh`

#### Test Fixes (test-fixes)
- **Profile**: `test-fixes`
- **Purpose**: Test build fixes
- **Command**: `./scripts/environment/test-fixes.sh`

#### Interactive Shell (shell)
- **Profile**: `shell`
- **Purpose**: Interactive shell access
- **Command**: `bash`

## Docker Compose Integration Improvements

The `docker-compose.yml` has been enhanced to provide better integration with the scripts and development workflow.

### New Services

#### 1. Development Service (`dev`)
- **Profile**: `dev`
- **Purpose**: Interactive development container
- **Usage**:
  ```bash
  ./scripts/docker/docker-compose-run.sh dev
  docker-compose exec dev ./gradlew build
  docker-compose exec dev ./scripts/quality/quality-check.sh
  ```

#### 2. Quality Check Service (`quality`)
- **Profile**: `quality`
- **Purpose**: Run quality checks in isolated container
- **Usage**:
  ```bash
  ./scripts/docker/docker-compose-run.sh quality
  ```

#### 3. Test Service (`test`)
- **Profile**: `test`
- **Purpose**: Run tests with database dependency
- **Usage**:
  ```bash
  ./scripts/docker/docker-compose-run.sh test
  ```

#### 4. Build Service (`build`)
- **Profile**: `build`
- **Purpose**: Run full build in isolated container
- **Usage**:
  ```bash
  ./scripts/docker/docker-compose-run.sh build
  ```

#### 5. Application Service (`app`)
- **Profile**: `app`
- **Purpose**: Start the full application
- **Usage**:
  ```bash
  ./scripts/docker/docker-compose-run.sh app
  ```

### Improvements Made

#### 1. **Health Checks**
- Database service now has health checks
- Application waits for database to be healthy before starting

#### 2. **Volume Management**
- Added `gradle-cache` volume for faster builds
- Mounted `./logs` and `./build` directories
- Source code mounted for development

#### 3. **Environment Variables**
- Default values for all environment variables
- Better database URL configuration
- Gradle options configured for rich console output

#### 4. **Service Dependencies**
- Proper dependency management with health checks
- Services start in correct order

#### 5. **Profiles**
- Services organized into profiles for selective startup
- `dev`, `quality`, `test`, `build`, `app` profiles available

### Usage Examples

#### Quick Commands
```bash
# Start development environment
./scripts/docker/docker-compose-run.sh dev

# Run quality checks
./scripts/docker/docker-compose-run.sh quality

# Run tests
./scripts/docker/docker-compose-run.sh test

# Run build
./scripts/docker/docker-compose-run.sh build

# Start full application
./scripts/docker/docker-compose-run.sh app
```

#### Interactive Development
```bash
# Start dev container
./scripts/docker/docker-compose-run.sh dev

# Run commands in container
docker-compose exec dev ./gradlew build
docker-compose exec dev ./scripts/quality/quality-check.sh
docker-compose exec dev ./scripts/build/gradle-with-logs.sh test
```

## Docker Desktop Integration

This section explains how to use Gradle tasks through Docker Desktop, allowing you to run all project operations without installing Java locally.

### 📋 Overview

The `docker-compose.override.yml` file creates Docker services for each Gradle task, allowing you to execute them through Docker Desktop or command line.

### 🚀 How to Use

#### 1. **Docker Desktop (Graphical Interface)**

1. Open Docker Desktop
2. Go to the "Containers" tab
3. Click "Run" and select the `docker-compose.override.yml` file
4. Choose the desired profile in the "Profiles" section

#### 2. **Command Line**

##### **Code Quality**
```bash
# Run all quality checks
docker-compose --profile quality-check up gradle-quality-check

# Individual checks
docker-compose --profile checkstyle up gradle-checkstyle
docker-compose --profile pmd up gradle-pmd
docker-compose --profile spotbugs up gradle-spotbugs
docker-compose --profile jacoco up gradle-jacoco
docker-compose --profile sonar up gradle-sonar
```

##### **Build and Tests**
```bash
# Complete build
docker-compose --profile build up gradle-build

# Run tests
docker-compose --profile test up gradle-test
```

##### **Docker Operations**
```bash
# Build production image
docker-compose --profile docker-build up docker-build

# Clean Docker resources
docker-compose --profile docker-clean up docker-clean

# View application logs
docker-compose --profile docker-logs up docker-logs
```

### 📁 Available Services

#### **Gradle Tasks**
| Service | Profile | Description |
|---------|---------|-------------|
| `gradle-quality-check` | `quality-check` | Runs all quality checks |
| `gradle-build` | `build` | Complete project build |
| `gradle-test` | `test` | Runs unit tests |
| `gradle-checkstyle` | `checkstyle` | Code style verification |
| `gradle-pmd` | `pmd` | Static code analysis |
| `gradle-spotbugs` | `spotbugs` | Bug detection |
| `gradle-jacoco` | `jacoco` | Code coverage report |
| `gradle-sonar` | `sonar` | SonarQube analysis |

#### **Docker Operations**
| Service | Profile | Description |
|---------|---------|-------------|
| `docker-build` | `docker-build` | Build production image |
| `docker-clean` | `docker-clean` | Clean unused Docker resources |
| `docker-logs` | `docker-logs` | Display application logs |

### 🔧 Configuration

#### **Mounted Volumes**
Each service mounts the following volumes:
- `./src:/app/src` - Source code
- `./build.gradle:/app/build.gradle` - Gradle configuration
- `./settings.gradle:/app/settings.gradle` - Project settings
- Specific configuration files (checkstyle.xml, pmd-ruleset.xml, etc.)

#### **Base Image**
All services use `Dockerfile.dev` which contains:
- OpenJDK 21
- Gradle Wrapper
- Development tools

### 💡 Usage Examples

#### **Development Workflow**

1. **Start development environment:**
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

2. **Run quality checks:**
   ```bash
   docker-compose --profile quality-check up gradle-quality-check
   ```

3. **View application logs:**
   ```bash
   docker-compose --profile docker-logs up docker-logs
   ```

4. **Production build:**
   ```bash
   docker-compose --profile docker-build up docker-build
   ```

#### **CI/CD Pipeline**

```yaml
# Example for GitHub Actions
- name: Quality Check
  run: docker-compose --profile quality-check up gradle-quality-check

- name: Run Tests
  run: docker-compose --profile test up gradle-test

- name: Build Application
  run: docker-compose --profile build up gradle-build
```

### 🎯 Advantages

#### **1. Isolation**
- Java 21 only in container
- Doesn't affect local system
- Consistent environment

#### **2. Ease of Use**
- Docker Desktop graphical interface
- Simple commands
- No need to install Java

#### **3. Portability**
- Works on any machine with Docker
- Same environment for all developers
- Easy CI/CD integration

#### **4. Organization**
- Tasks separated by profile
- Easy identification in Docker Desktop
- Organized logs by service

## 🔧 Configuration

### Environment Variables
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
- **test-fixes**: `test-fixes` (test fixes)
- **shell**: `shell` (interactive shell)

## 📁 Project Structure

```
finance-control/
├── docker-compose.yml          # Docker Compose configuration
├── Dockerfile                  # Multi-stage Docker build
├── docker.env                  # Environment variables
├── scripts/
│   ├── docker-manager.sh       # Main Docker management script
│   ├── docker-run.sh          # Alternative Docker runner
│   ├── check-environment.sh   # Environment validation
│   ├── fix-environment.sh     # Environment fixes
│   ├── quality-check.sh       # Quality checks
│   ├── test-fixes.sh          # Test fixes
│   └── help.sh                # Help documentation
├── build/                     # Build artifacts (mounted)
├── logs/                      # Application logs (mounted)
└── src/                       # Source code (mounted)
```

## 🔄 Development Workflow

### 1. Initial Setup
```bash
# Build the base image
./scripts/docker/docker-manager.sh build

# Start development environment
./scripts/docker/docker-manager.sh dev

# Open shell in container
./scripts/docker/docker-manager.sh shell
```

### 2. Daily Development
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

### 3. Testing
```bash
# Run tests
./scripts/docker/docker-manager.sh test

# Run quality checks
./scripts/docker/docker-manager.sh quality

# Run environment check
./scripts/docker/docker-manager.sh check-env
```

### 4. Application Deployment
```bash
# Start application with database
./scripts/docker/docker-manager.sh app

# Check logs
./scripts/docker/docker-manager.sh logs app

# Follow logs
./scripts/docker/docker-manager.sh logs-follow app
```

## 🐛 Troubleshooting

### Common Issues

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

### Debugging Docker Desktop Integration

#### **Problem: Container doesn't start**
```bash
# Check logs
docker-compose logs [service-name]

# Check if Dockerfile.dev exists
ls -la Dockerfile.dev
```

#### **Problem: Volumes not mounted**
```bash
# Check if files exist
ls -la build.gradle settings.gradle

# Check permissions
chmod +x gradlew
```

#### **Problem: Gradle can't find dependencies**
```bash
# Clear Gradle cache
docker-compose --profile build up gradle-build --build
```

## 🔒 Security Considerations

### Environment Variables
- Never commit sensitive data to version control
- Use `.env` files for local development
- Use Docker secrets for production

### Network Security
- Services communicate via Docker network
- Database is not exposed to host by default
- Application port can be configured

### File Permissions
- Source code is mounted as read-write
- Build artifacts are preserved in volumes
- Logs are accessible from host

## 📊 Monitoring and Logging

### Application Logs
```bash
# View application logs
./scripts/docker/docker-manager.sh logs app

# Follow logs in real-time
./scripts/docker/docker-manager.sh logs-follow app

# View logs for specific service
./scripts/docker/docker-manager.sh logs db
```

### Build Logs
- Build logs are saved to `logs/`
- Timestamped log files for each operation
- Quality check reports in `build/reports/`

### Health Checks
- Database has automatic health checks
- Application health endpoint at `/actuator/health`
- Container restart policies configured

### Monitoring Docker Desktop Integration

#### **Check Service Status**
```bash
# List all containers
docker ps -a

# View logs in real time
docker-compose logs -f [service-name]
```

#### **Generated Reports**
Reports are generated in `build/reports/` and are available on the host:
- Checkstyle: `build/reports/checkstyle/`
- PMD: `build/reports/pmd/`
- SpotBugs: `build/reports/spotbugs/`
- JaCoCo: `build/reports/jacoco/`

## 🚀 Production Deployment

### Build Production Image
```bash
# Build production image
docker build -t finance-control:prod .

# Run production container
docker run -d \
  --name finance-control-prod \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  finance-control:prod
```

### Docker Compose Production
```bash
# Use production profile
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## 📚 Additional Resources

### Documentation
- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)

### Scripts Help
```bash
# Show help for docker-manager
./scripts/docker/docker-manager.sh help

# Show help for specific script
./scripts/help.sh docker-manager
```

### Project Documentation
- `docs/` - Project documentation
- `README.md` - Main project README
- `docs/API_PATTERNS.md` - API documentation

## 🤝 Contributing

When contributing to the project:

1. **Use Docker for development** - All development should be done in containers
2. **Test in Docker** - Ensure all tests pass in Docker environment
3. **Update documentation** - Keep this document updated with changes
4. **Follow conventions** - Use the established Docker patterns

## 🔄 Updates

To update tasks:
1. Modify `docker-compose.override.yml`
2. Rebuild containers:
   ```bash
   docker-compose build --no-cache
   ```

## 📝 Important Notes

- **Profiles**: Use `--profile` to run specific services
- **Volumes**: Reports are persisted on the host
- **Cache**: Gradle cache is maintained between runs
- **Resources**: Each service uses independent resources

## Benefits

1. **Consistent Environment**: All developers use the same environment
2. **Isolated Services**: Each task runs in its own container
3. **Better Performance**: Gradle cache shared between runs
4. **Health Monitoring**: Database health checks prevent startup issues
5. **Easy Debugging**: Interactive development container available
6. **GUI Integration**: Services visible in Docker Desktop

## Migration from Old Scripts

The existing scripts (`quality-check.sh`, `gradle-with-logs.sh`, etc.) still work and will use Docker when available. The new Docker Compose services provide an alternative, more integrated approach.

### Comparison

| Approach | Pros | Cons |
|----------|------|------|
| **Existing Scripts** | Simple, direct | Manual Docker management |
| **Docker Compose Services** | Integrated, GUI visible | More complex setup |

Both approaches work together - use what fits your workflow best!

## 📝 Changelog

### Version 1.0.0
- Initial Docker setup
- Multi-stage Docker build
- Docker Compose services
- Script automation
- Environment configuration
