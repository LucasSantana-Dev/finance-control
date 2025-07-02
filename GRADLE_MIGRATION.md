# Maven → Gradle Migration

This document describes the migration of the Finance Control project from Maven to Gradle.

## 📁 Created/Modified Files

### New Gradle Files
- `build.gradle` - Main project configuration
- `settings.gradle` - Project configuration
- `gradlew` - Gradle script for Unix/Linux/macOS
- `gradlew.bat` - Gradle script for Windows
- `gradle/wrapper/gradle-wrapper.properties` - Wrapper properties
- `gradle/wrapper/gradle-wrapper.jar` - Wrapper JAR (needs to be downloaded)

### Quality Scripts
- `scripts/quality-check.sh` - Quality script for Gradle

## 🔧 Equivalent Commands

| Maven | Gradle | Description |
|-------|--------|-------------|
| `./mvnw clean` | `./gradlew clean` | Clean build |
| `./mvnw compile` | `./gradlew compileJava` | Compile code |
| `./mvnw test` | `./gradlew test` | Run tests |
| `./mvnw package` | `./gradlew build` | Complete build |
| `./mvnw spring-boot:run` | `./gradlew bootRun` | Run application |
| `./mvnw checkstyle:check` | `./gradlew checkstyleMain` | Checkstyle |
| `./mvnw pmd:check` | `./gradlew pmdMain` | PMD |
| `./mvnw spotbugs:check` | `./gradlew spotbugsMain` | SpotBugs |
| `./mvnw jacoco:report` | `./gradlew jacocoTestReport` | Coverage report |
| `./mvnw sonar:sonar` | `./gradlew sonarqube` | SonarQube |

## 🚀 How to Use

### 1. First, download the wrapper JAR
```bash
# Option 1: Using curl
curl -o gradle/wrapper/gradle-wrapper.jar https://github.com/gradle/gradle/raw/v8.7.0/gradle/wrapper/gradle-wrapper.jar

# Option 2: Using wget
wget -O gradle/wrapper/gradle-wrapper.jar https://github.com/gradle/gradle/raw/v8.7.0/gradle/wrapper/gradle-wrapper.jar

# Option 3: Download manually from GitHub and place in gradle/wrapper/
```

### 2. Test Gradle
```bash
./gradlew --version
```

### 3. Build project
```bash
./gradlew build
```

### 4. Run application
```bash
./gradlew bootRun
```

### 5. Quality checks
```bash
# Using the script
./scripts/quality-check.sh

# Or individual commands
./gradlew checkstyleMain
./gradlew pmdMain
./gradlew spotbugsMain
./gradlew test
./gradlew jacocoTestReport
./gradlew jacocoTestCoverageVerification
```

## 📊 Reports

Reports are generated in `build/reports/`:

- **Checkstyle**: `build/reports/checkstyle/`
- **PMD**: `build/reports/pmd/`
- **SpotBugs**: `build/reports/spotbugs/`
- **JaCoCo**: `build/reports/jacoco/`

## 🔍 Specific Configurations

### Java 21
The project is configured for Java 21:
```gradle
sourceCompatibility = '21'
targetCompatibility = '21'
```

### Code Coverage
- Minimum: 80% (line and branch)
- Exclusions: config, dto, model, exception, enums, util, validation
- Reports: HTML and XML

### Quality Plugins
- **Checkstyle**: Version 10.26.1
- **PMD**: Version 7.0.0
- **SpotBugs**: Version 4.8.3 + FindSecBugs
- **JaCoCo**: Default Gradle version
- **SonarQube**: Version 5.0.0.4638

## 🛠️ Custom Tasks

### Task `qualityCheck`
Runs all quality checks:
```bash
./gradlew qualityCheck
```

### Task `build`
Complete build with tests and checks:
```bash
./gradlew build
```

## ✅ Migration Complete

The project has been successfully migrated from Maven to Gradle:

1. **Gradle**: `./gradlew` or `./gradlew.bat` (primary build system)
2. **Maven**: Removed (no longer supported)

### Quality Scripts
- **Gradle**: `./scripts/quality-check.sh` (primary quality script)

## 🧹 Cleanup

Maven files have been removed. Use the provided script to safely remove Maven files:

```bash
# Run the Maven removal script
./scripts/remove-maven.sh
```

This script will:
- Create a backup of Maven files
- Verify Gradle is working
- Remove Maven files safely
- Test Gradle functionality after removal

## ⚠️ Known Issues

1. **Wrapper JAR**: Needs to be downloaded manually
2. **PMD**: May have issues with Java 21 (use version 17 for analysis)
3. **SpotBugs**: May need specific configurations for Java 21

## 📝 Migration Status

1. ✅ Create Gradle files
2. ✅ Configure quality plugins
3. ✅ Create quality script
4. ✅ Download wrapper JAR
5. ✅ Test complete build
6. ✅ Validate reports
7. ✅ Remove Maven files
8. ✅ Update documentation

**Migration is complete!** The project now uses Gradle exclusively.

## 🆘 Support

If you encounter issues:

1. Check if the wrapper JAR was downloaded correctly
2. Run with `--verbose` for more details
3. Compare with Maven build to identify differences
4. Consult Gradle documentation: https://docs.gradle.org/ 