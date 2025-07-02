# Docker Setup for Finance Control

This document describes how to run the Finance Control application and all scripts inside Docker containers for consistent environments across all platforms.

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
./scripts/docker-manager.sh build

# Start development environment
./scripts/docker-manager.sh dev

# Open shell in container
./scripts/docker-manager.sh shell
```

### 3. Run Commands
```bash
# Inside the container shell:
./gradlew build
./gradlew test
./scripts/quality-check.sh
```

## 🛠️ Available Scripts

### Docker Manager Script
The main script for all Docker operations:

```bash
./scripts/docker-manager.sh <command>
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
./scripts/docker-run.sh <command>
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
- **Command**: `./scripts/check-environment.sh`

#### Environment Fix (fix-env)
- **Profile**: `fix-env`
- **Purpose**: Fix environment issues
- **Command**: `./scripts/fix-environment.sh`

#### Test Fixes (test-fixes)
- **Profile**: `test-fixes`
- **Purpose**: Test build fixes
- **Command**: `./scripts/test-fixes.sh`

#### Interactive Shell (shell)
- **Profile**: `shell`
- **Purpose**: Interactive shell access
- **Command**: `bash`

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
./scripts/docker-manager.sh build

# Start development environment
./scripts/docker-manager.sh dev

# Open shell in container
./scripts/docker-manager.sh shell
```

### 2. Daily Development
```bash
# Start development environment
./scripts/docker-manager.sh dev

# Run commands in container
./scripts/docker-manager.sh gradle build
./scripts/docker-manager.sh gradle test
./scripts/docker-manager.sh quality

# Or open shell for interactive work
./scripts/docker-manager.sh shell
```

### 3. Testing
```bash
# Run tests
./scripts/docker-manager.sh test

# Run quality checks
./scripts/docker-manager.sh quality

# Run environment check
./scripts/docker-manager.sh check-env
```

### 4. Application Deployment
```bash
# Start application with database
./scripts/docker-manager.sh app

# Check logs
./scripts/docker-manager.sh logs app

# Follow logs
./scripts/docker-manager.sh logs-follow app
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
./scripts/docker-manager.sh cleanup
./scripts/docker-manager.sh rebuild

# Check logs
./scripts/docker-manager.sh logs build
```

#### Database Connection Issues
```bash
# Check database status
./scripts/docker-manager.sh status

# Restart database
./scripts/docker-manager.sh restart db

# Check database logs
./scripts/docker-manager.sh logs db
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
./scripts/docker-manager.sh logs app

# Follow logs in real-time
./scripts/docker-manager.sh logs-follow app

# View logs for specific service
./scripts/docker-manager.sh logs db
```

### Build Logs
- Build logs are saved to `build/logs/`
- Timestamped log files for each operation
- Quality check reports in `build/reports/`

### Health Checks
- Database has automatic health checks
- Application health endpoint at `/actuator/health`
- Container restart policies configured

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
./scripts/docker-manager.sh help

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

## 📝 Changelog

### Version 1.0.0
- Initial Docker setup
- Multi-stage Docker build
- Docker Compose services
- Script automation
- Environment configuration 