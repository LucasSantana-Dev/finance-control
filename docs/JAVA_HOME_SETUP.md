# JAVA_HOME Setup for JDK 21

This document explains how to configure JAVA_HOME to use JDK 21 in the Finance Control project.

## 🎯 Objective

Configure the environment to use JDK 21, allowing Gradle and the application to work correctly.

## 📋 Prerequisites

1. **JDK 21 installed** on the system
2. **Docker Desktop** (optional, for isolated development)
3. **Git Bash** or **PowerShell** (Windows)

## 🚀 Configuration Methods

### **Method 1: Automatic Script (Recommended)**

#### **Windows (PowerShell)**
```powershell
# Run the PowerShell script
.\scripts\setup-java-env.ps1
```

#### **Windows/Linux/macOS (Git Bash)**
```bash
# Run the Bash script
./scripts/setup-java-env.sh
```

### **Method 2: Manual Configuration**

#### **1. Create .env file**
```bash
# Copy the template
cp env-template.txt .env
```

#### **2. Edit the .env file**
```env
# Java Configuration
JAVA_HOME=C:\Program Files\Java\jdk-21
JAVA_VERSION=21

# Other configurations...
```

#### **3. Configure environment variables**

**Windows (PowerShell):**
```powershell
# Load variables from .env file
. .env

# Verify configuration
echo $env:JAVA_HOME
java -version
```

**Windows (CMD):**
```cmd
# Load variables from .env file
set /p < .env

# Verify configuration
echo %JAVA_HOME%
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

## 📁 File Structure

```
finance-control/
├── .env                    # Environment variables (created automatically)
├── env-template.txt        # Configuration template
├── scripts/
│   ├── setup-java-env.sh   # Bash script
│   └── setup-java-env.ps1  # PowerShell script
└── docs/
    └── JAVA_HOME_SETUP.md  # This documentation
```

## 🔍 Configuration Verification

### **1. Check JAVA_HOME**
```bash
# Windows (PowerShell)
echo $env:JAVA_HOME

# Windows (CMD)
echo %JAVA_HOME%

# Linux/macOS
echo $JAVA_HOME
```

### **2. Check Java version**
```bash
java -version
```

**Expected output:**
```
openjdk version "21.0.1" 2023-10-17
OpenJDK Runtime Environment (build 21.0.1+12-29)
OpenJDK 64-Bit Server VM (build 21.0.1+12-29, mixed mode, sharing)
```

### **3. Test Gradle**
```bash
./gradlew --version
```

## 🐳 Configuration with Docker

If you prefer to use only Docker (without installing Java locally):

### **1. Use only Docker**
```bash
# Build and execution via Docker
docker-compose --profile build up gradle-build
docker-compose -f docker-compose.dev.yml up -d
```

### **2. Hybrid configuration**
```bash
# Configure JAVA_HOME for local development
# Use Docker for production
```

## 🔧 Permanent Configuration (Windows)

### **1. System Environment Variables**
1. Open "System Environment Variables"
2. Click "Environment Variables..."
3. In "System Variables", click "New..."
4. Variable name: `JAVA_HOME`
5. Variable value: `C:\Program Files\Java\jdk-21`
6. Click "OK"

### **2. Add to PATH**
1. Select the "Path" variable in "System Variables"
2. Click "Edit..."
3. Click "New..."
4. Add: `%JAVA_HOME%\bin`
5. Click "OK"

### **3. Verify**
1. Open a new terminal
2. Run: `java -version`
3. Run: `echo %JAVA_HOME%`

## 📦 JDK 21 Distributions

### **Oracle JDK 21**
- **Download**: https://www.oracle.com/java/technologies/downloads/#java21
- **Default path**: `C:\Program Files\Java\jdk-21`

### **Eclipse Temurin (OpenJDK)**
- **Download**: https://adoptium.net/temurin/releases/?version=21
- **Default path**: `C:\Program Files\Eclipse Adoptium\jdk-21`

### **Microsoft OpenJDK**
- **Download**: https://docs.microsoft.com/en-us/java/openjdk/download
- **Default path**: `C:\Program Files\Microsoft\jdk-21`

## 🔍 Troubleshooting

### **Problem: JAVA_HOME not found**
```bash
# Check if JDK is installed
ls "C:\Program Files\Java\"

# Check if path is correct
echo $env:JAVA_HOME
```

### **Problem: Gradle doesn't work**
```bash
# Check Java version
java -version

# Check if Gradle wrapper exists
ls gradlew*

# Run with verbose
./gradlew --version --info
```

### **Problem: Docker can't find Java**
```bash
# Check if Dockerfile.dev is correct
cat Dockerfile.dev

# Rebuild image
docker-compose build --no-cache
```

## 📝 Important Notes

1. **.env file**: Is in `.gitignore` to not be committed
2. **Scripts**: Automatically detect different JDK distributions
3. **Docker**: Uses Java 21 isolated in container
4. **Compatibility**: Gradle 8.7 officially supports Java 21

## 🎯 Next Steps

After configuring JAVA_HOME:

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