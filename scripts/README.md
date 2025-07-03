# Finance Control - Scripts

This project uses **Docker for everything** - no need to set up a local environment.

## 🚀 Quick Start

```bash
# 1. Start application (with tests)
./scripts/dev.sh start

# 1. Start application (without tests - faster)
./scripts/dev.sh start --no-test

# 2. Open development shell
./scripts/dev.sh dev

# 3. Inside the shell, run commands
./gradlew build
./gradlew test
./gradlew qualityCheck
```

## 📋 Available Commands

| Command | Description | Parameters |
|---------|-------------|------------|
| `./scripts/dev.sh start` | Starts application + database | `--no-test` |
| `./scripts/dev.sh dev` | Starts development container | - |
| `./scripts/dev.sh build` | Builds the project | `--no-test` |
| `./scripts/dev.sh test` | Runs tests | `--no-test` |
| `./scripts/dev.sh quality` | Runs quality checks | `--no-test` |
| `./scripts/dev.sh logs` | Shows application logs | - |
| `./scripts/dev.sh stop` | Stops all services | - |
| `./scripts/dev.sh clean` | Cleans containers and volumes | `--no-test` |
| `./scripts/dev.sh quality-local` | Runs complete quality analysis (local) | `--no-test` |

### Parameters

- `--no-test`: Skips test compilation and execution (when applicable)

## 🔧 Development Workflow

### 1. First time
```bash
# Clone project
git clone <repo>
cd finance-control

# Start application (with tests)
./scripts/dev.sh start

# Start application (without tests - faster)
./scripts/dev.sh start --no-test

# Check if it's working
curl http://localhost:8080/actuator/health
```

### 2. Daily development
```bash
# Start environment (without tests for faster development)
./scripts/dev.sh start --no-test

# Open shell for development
./scripts/dev.sh dev

# Inside the shell:
./gradlew build    # Compile
./gradlew test     # Test
./gradlew run      # Run locally
```

### 3. Quality checks
```bash
# Run all checks in Docker
./scripts/dev.sh quality

# Run only static analysis (without tests)
./scripts/dev.sh quality --no-test

# Or run locally (if you have Java/Gradle installed):
./scripts/dev.sh quality-local

# Run only local static analysis (without tests)
./scripts/dev.sh quality-local --no-test

# Or individually inside the shell:
./gradlew checkstyleMain
./gradlew pmdMain
./gradlew spotbugsMain
```

### 4. Build and Tests
```bash
# Complete build (with tests)
./scripts/dev.sh build

# Fast build (without tests)
./scripts/dev.sh build --no-test

# Complete tests
./scripts/dev.sh test

# Tests without recompilation
./scripts/dev.sh test --no-test
```

## 🎯 When to Use `--no-test`

### ✅ Use `--no-test` for:
- **Active development**: When coding and testing
- **Quick analysis**: To check quality without running tests
- **Development build**: To compile quickly
- **Debug**: When investigating specific issues
- **Fast CI/CD**: For development builds in pipelines

### ❌ DON'T use `--no-test` for:
- **Before commits**: Always run complete tests
- **Production builds**: Always include tests
- **Final validation**: To ensure everything works
- **Pull Requests**: To validate changes

## 🌐 Access Points

- **Application**: http://localhost:8080
- **Swagger**: http://localhost:8080/swagger-ui.html
- **Actuator**: http://localhost:8080/actuator
- **Database**: localhost:5432

## 🐳 Docker Services

The `docker-compose.yml` defines the following services:

- **app**: Main application
- **db**: PostgreSQL 17
- **dev**: Development container (profile `dev`)
- **build**: Compilation (profile `build`)
- **test**: Tests (profile `test`)
- **quality**: Quality checks (profile `quality`)

All services support the `SKIP_TESTS` variable to skip tests when needed.

## 📁 Simplified Structure

```
scripts/
├── dev.sh              # Main script (everything in Docker)
└── README.md           # This documentation
```

## ❌ Removed Scripts

The following scripts were removed as they were unnecessary:

- `scripts/environment/` - Local environment checks
- `scripts/quality/` - Separate quality scripts
- `scripts/build/` - Separate build scripts
- `scripts/docker/` - Separate Docker scripts
- `scripts/help.sh` - Complex documentation

**Reason**: Everything runs in Docker, so we don't need scripts for local environment.

## 🔍 Troubleshooting

### Docker is not running
```bash
# Check if Docker is running
docker info

# If not, start Docker Desktop
```

### Port already in use
```bash
# Stop all services
./scripts/dev.sh stop

# Or change port in docker-compose.yml
```

### Permission issues
```bash
# Give execution permission
chmod +x scripts/dev.sh
```

### Clean everything and start over
```bash
./scripts/dev.sh clean
./scripts/dev.sh start
```

## 🎯 Advantages of Docker Approach

1. **No local setup**: No need to install Java, Gradle, etc.
2. **Consistent environment**: Same environment for all developers
3. **Isolation**: Doesn't interfere with other installations
4. **Simplicity**: One script for everything
5. **Portability**: Works on any system with Docker
6. **Flexibility**: `--no-test` parameter for faster development 