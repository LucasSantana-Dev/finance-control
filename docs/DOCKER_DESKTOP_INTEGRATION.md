# Docker Desktop Integration

This document explains how to use Gradle tasks through Docker Desktop, allowing you to run all project operations without installing Java locally.

## 📋 Overview

The `docker-compose.override.yml` file creates Docker services for each Gradle task, allowing you to execute them through Docker Desktop or command line.

## 🚀 How to Use

### 1. **Docker Desktop (Graphical Interface)**

1. Open Docker Desktop
2. Go to the "Containers" tab
3. Click "Run" and select the `docker-compose.override.yml` file
4. Choose the desired profile in the "Profiles" section

### 2. **Command Line**

#### **Code Quality**
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

#### **Build and Tests**
```bash
# Complete build
docker-compose --profile build up gradle-build

# Run tests
docker-compose --profile test up gradle-test
```

#### **Docker Operations**
```bash
# Build production image
docker-compose --profile docker-build up docker-build

# Clean Docker resources
docker-compose --profile docker-clean up docker-clean

# View application logs
docker-compose --profile docker-logs up docker-logs
```

## 📁 Available Services

### **Gradle Tasks**
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

### **Docker Operations**
| Service | Profile | Description |
|---------|---------|-------------|
| `docker-build` | `docker-build` | Build production image |
| `docker-clean` | `docker-clean` | Clean unused Docker resources |
| `docker-logs` | `docker-logs` | Display application logs |

## 🔧 Configuration

### **Mounted Volumes**
Each service mounts the following volumes:
- `./src:/app/src` - Source code
- `./build.gradle:/app/build.gradle` - Gradle configuration
- `./settings.gradle:/app/settings.gradle` - Project settings
- Specific configuration files (checkstyle.xml, pmd-ruleset.xml, etc.)

### **Base Image**
All services use `Dockerfile.dev` which contains:
- OpenJDK 21
- Gradle Wrapper
- Development tools

## 💡 Usage Examples

### **Development Workflow**

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

### **CI/CD Pipeline**

```yaml
# Example for GitHub Actions
- name: Quality Check
  run: docker-compose --profile quality-check up gradle-quality-check

- name: Run Tests
  run: docker-compose --profile test up gradle-test

- name: Build Application
  run: docker-compose --profile build up gradle-build
```

## 🎯 Advantages

### **1. Isolation**
- Java 21 only in container
- Doesn't affect local system
- Consistent environment

### **2. Ease of Use**
- Docker Desktop graphical interface
- Simple commands
- No need to install Java

### **3. Portability**
- Works on any machine with Docker
- Same environment for all developers
- Easy CI/CD integration

### **4. Organization**
- Tasks separated by profile
- Easy identification in Docker Desktop
- Organized logs by service

## 🔍 Troubleshooting

### **Problem: Container doesn't start**
```bash
# Check logs
docker-compose logs [service-name]

# Check if Dockerfile.dev exists
ls -la Dockerfile.dev
```

### **Problem: Volumes not mounted**
```bash
# Check if files exist
ls -la build.gradle settings.gradle

# Check permissions
chmod +x gradlew
```

### **Problem: Gradle can't find dependencies**
```bash
# Clear Gradle cache
docker-compose --profile build up gradle-build --build
```

## 📊 Monitoring

### **Check Service Status**
```bash
# List all containers
docker ps -a

# View logs in real time
docker-compose logs -f [service-name]
```

### **Generated Reports**
Reports are generated in `build/reports/` and are available on the host:
- Checkstyle: `build/reports/checkstyle/`
- PMD: `build/reports/pmd/`
- SpotBugs: `build/reports/spotbugs/`
- JaCoCo: `build/reports/jacoco/`

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