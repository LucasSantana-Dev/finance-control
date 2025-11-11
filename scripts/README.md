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
| `./scripts/dev.sh code-quality` | Run code quality analysis (file size, complexity) | `--skip-tools` |
| `./scripts/dev.sh security-check` | Run security vulnerability and pattern checks | `--skip-deps` |
| `./scripts/dev.sh checkstyle-clean` | Clean and run Checkstyle with stacktrace | - |
| `./scripts/dev.sh sonarqube-start` | Starts SonarQube service | - |
| `./scripts/dev.sh sonarqube-stop` | Stops SonarQube service | - |
| `./scripts/dev.sh sonarqube-logs` | Shows SonarQube logs | - |
| `./scripts/dev.sh sonarqube-scan` | Runs SonarQube analysis | `--no-test` |
| `./scripts/dev.sh check-env` | Checks environment | - |

### Parameters

- `--no-test`: Skips test compilation and execution (when applicable)

## 🏗️ Modular Architecture

The scripts are now organized in a modular structure for better maintainability:

```
scripts/
├── dev.sh                    # Main entry point
├── README.md                 # This documentation
└── modules/
    ├── core.sh              # Core utilities, colors, usage
    ├── services.sh          # Docker service management
    ├── build.sh             # Build, test, and quality functions
    ├── sonarqube.sh         # SonarQube management
    ├── devshell.sh          # Development shell and environment
    ├── code-quality.sh      # Code quality analysis (file sizes, complexity)
    └── security-check.sh    # Security vulnerability and pattern scanning
```

### Module Responsibilities

- **`core.sh`**: Colors, print functions, Docker checks, usage information
- **`services.sh`**: Start/stop/clean services, logs, application startup
- **`build.sh`**: Build, test, quality checks, Checkstyle operations
- **`sonarqube.sh`**: SonarQube service management and analysis
- **`devshell.sh`**: Development container and environment checks
- **`code-quality.sh`**: Code quality analysis (file size limits, complexity checking)
- **`security-check.sh`**: Security vulnerability scanning and code pattern detection

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

# Run code quality analysis (file sizes, complexity)
./scripts/dev.sh code-quality

# Run code quality analysis (skip running quality tools, use existing reports)
./scripts/dev.sh code-quality --skip-tools

# Run security checks (dependency vulnerabilities, code patterns)
./scripts/dev.sh security-check

# Run security checks (skip dependency checks, use existing reports)
./scripts/dev.sh security-check --skip-deps

# Run Checkstyle with clean and stacktrace
./scripts/dev.sh checkstyle-clean

# Or individually inside the shell:
./gradlew checkstyleMain
./gradlew pmdMain
./gradlew spotbugsMain
```

### 4. SonarQube Analysis
```bash
# Start SonarQube service
./scripts/dev.sh sonarqube-start

# Run SonarQube analysis (starts service if needed)
./scripts/dev.sh sonarqube-scan

# Run SonarQube analysis without tests (faster)
./scripts/dev.sh sonarqube-scan --no-test

# View SonarQube logs
./scripts/dev.sh sonarqube-logs

# Stop SonarQube service
./scripts/dev.sh sonarqube-stop
```

### 5. Build and Tests
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

## ⏱️ Timeout Configuration

All operations have built-in timeouts to prevent hanging processes:

- **Build**: 15 minutes (900 seconds)
- **Tests**: 10 minutes (600 seconds)
- **Quality Checks**: 15 minutes (900 seconds)
- **SonarQube Analysis**: 15 minutes (900 seconds)

If an operation times out, the script will suggest using the `--no-test` flag for faster execution.

## 🔄 Retry Logic

All operations include retry logic with a maximum of 2 attempts:

- **Service operations**: 2-3 second delays between retries
- **Build operations**: 5 second delays between retries
- **Test operations**: 3 second delays between retries
- **Quality operations**: 3 second delays between retries

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
- **SonarQube**: http://localhost:9000 (admin/admin)

## 🐳 Docker Services

The `docker-compose.yml` defines the following services:

- **app**: Main application
- **db**: PostgreSQL 17
- **sonarqube**: SonarQube code quality analysis (profile `sonarqube`)
- **dev**: Development container (profile `dev`)
- **build**: Compilation (profile `build`)
- **test**: Tests (profile `test`)
- **quality**: Quality checks (profile `quality`)

All services support the `SKIP_TESTS` variable to skip tests when needed.

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
chmod +x scripts/modules/*.sh
```

### Clean everything and start over
```bash
./scripts/dev.sh clean
./scripts/dev.sh start
```

### Operation timed out
```bash
# Try with --no-test flag for faster execution
./scripts/dev.sh build --no-test
./scripts/dev.sh test --no-test
./scripts/dev.sh quality --no-test
```

### Module loading issues
```bash
# Check if modules exist
ls -la scripts/modules/

# Ensure proper permissions
chmod +x scripts/modules/*.sh
```

## 🎯 Advantages of Modular Approach

1. **Maintainability**: Each module has a single responsibility
2. **Reusability**: Modules can be used in other scripts
3. **Testability**: Individual modules can be tested separately
4. **Readability**: Main script is clean and focused on CLI logic
5. **Extensibility**: Easy to add new modules for new features
6. **Debugging**: Easier to isolate issues to specific modules

## 🎯 Advantages of Docker Approach

1. **No local setup**: No need to install Java, Gradle, etc.
2. **Consistent environment**: Same environment for all developers
3. **Isolation**: Doesn't interfere with other installations
4. **Simplicity**: One script for everything
5. **Portability**: Works on any system with Docker
6. **Flexibility**: `--no-test` parameter for faster development
7. **Reliability**: Built-in retry logic and timeout handling

## Code Quality and Security Scripts

### Code Quality Analysis

The `code-quality.sh` script provides detailed code quality analysis:

```bash
# Run full code quality check (runs Checkstyle and PMD, then analyzes results)
./scripts/dev.sh code-quality

# Run analysis using existing reports (skip running quality tools)
./scripts/dev.sh code-quality --skip-tools
```

**What it checks:**
- File size limits (max 2000 lines per Java class)
- Cyclomatic complexity violations from Checkstyle and PMD reports
- Generates summary report with suggestions

**Output:**
- Lists files exceeding size limits
- Reports complexity violations with file locations
- Provides refactoring suggestions
- Exits with error code if violations found (for CI/CD integration)

### Security Check

The `security-check.sh` script provides comprehensive security analysis:

```bash
# Run full security check (dependency scanning + code pattern detection)
./scripts/dev.sh security-check

# Run only code pattern checks (skip dependency scanning)
./scripts/dev.sh security-check --skip-deps
```

**What it checks:**
- Dependency vulnerabilities (using OWASP Dependency Check if available)
- Critical and high severity vulnerabilities
- Outdated dependencies
- License compliance
- Security patterns in code:
  - Hardcoded passwords, API keys, secrets
  - SQL injection vulnerabilities
  - Insecure random number generation
  - Weak cryptography usage
  - Exposed sensitive data in logs
- Hardcoded secrets in configuration files

**Output:**
- Security issues (errors) that must be fixed
- Security warnings that should be reviewed
- Detailed suggestions for fixing issues
- Exits with error code if critical issues found

**Integration:**
These scripts are designed to be integrated into:
- Pre-commit hooks
- CI/CD pipelines
- Regular code review processes

See `docs/CODE_QUALITY.md` for more details on quality standards and best practices.
