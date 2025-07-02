# Gradle Script Tasks

This document describes the custom Gradle tasks that allow executing bash scripts directly, similar to how `package.json` works in Node.js projects.

## Overview

Tasks have been added to `build.gradle` to facilitate the execution of common project bash scripts, organized in groups for better organization.

## Task Groups

### `scripts` Group - Quality Checks

#### `qualityCheckScript`
Executes quality checks using the Gradle-optimized bash script.

```bash
./gradlew qualityCheckScript
```

#### `qualityCheckScriptVerbose`
Executes quality checks with detailed output.

```bash
./gradlew qualityCheckScriptVerbose
```

#### `qualityCheckMaven` (Legacy)
Executes quality checks using the original Maven script (if it exists).

```bash
./gradlew qualityCheckMaven
```

### `docker` Group - Docker Operations

#### `dockerBuild`
Builds the production Docker image.

```bash
./gradlew dockerBuild
```

#### `dockerDev`
Starts the Docker development environment.

```bash
./gradlew dockerDev
```

#### `dockerProd`
Starts the Docker production environment.

```bash
./gradlew dockerProd
```

#### `dockerClean`
Cleans up unused Docker resources.

```bash
./gradlew dockerClean
```

#### `dockerLogs`
Shows Docker container logs.

```bash
./gradlew dockerLogs
```

#### `dockerShell`
Opens a shell in the application container.

```bash
./gradlew dockerShell
```

#### `dockerTest`
Runs tests in Docker.

```bash
./gradlew dockerTest
```

## Listing Available Tasks

### View all tasks
```bash
./gradlew tasks
```

### View tasks from a specific group
```bash
./gradlew tasks --group scripts
./gradlew tasks --group docker
```

### View tasks with descriptions
```bash
./gradlew tasks --all
```

## Usage Examples

### Typical Development Workflow

1. **Start development environment:**
   ```bash
   ./gradlew dockerDev
   ```

2. **Run quality checks:**
   ```bash
   ./gradlew qualityCheckScript
   ```

3. **View application logs:**
   ```bash
   ./gradlew dockerLogs
   ```

4. **Open shell in container:**
   ```bash
   ./gradlew dockerShell
   ```

### Production Workflow

1. **Build production image:**
   ```bash
   ./gradlew dockerBuild
   ```

2. **Start production environment:**
   ```bash
   ./gradlew dockerProd
   ```

3. **Run tests:**
   ```bash
   ./gradlew dockerTest
   ```

## Advantages

### 1. **Consistency**
- Standardized commands across all environments
- Same interface for all developers

### 2. **Ease of Use**
- Simple and intuitive commands
- No need to remember script paths
- IDE integration (IntelliJ, Eclipse, VS Code)

### 3. **Organization**
- Tasks grouped by functionality
- Clear descriptions for each task
- Easy discovery of available features

### 4. **Automation**
- Can be integrated into CI/CD pipelines
- Programmatic script execution
- Dependencies between tasks

## Comparison with npm scripts

| npm scripts | Gradle tasks | Description |
|-------------|--------------|-------------|
| `npm run dev` | `./gradlew dockerDev` | Start development environment |
| `npm run build` | `./gradlew dockerBuild` | Build application |
| `npm run test` | `./gradlew dockerTest` | Run tests |
| `npm run lint` | `./gradlew qualityCheckScript` | Quality checks |
| `npm run clean` | `./gradlew dockerClean` | Clean resources |

## Configuration

Tasks are configured in `build.gradle` with the following characteristics:

- **Type**: `Exec` - Executes external commands
- **Working Directory**: Project root
- **Executability**: Automatically checks and sets permissions
- **Groups**: Organized by functionality
- **Descriptions**: Inline documentation

## Extensibility

To add new tasks:

1. **Add to `build.gradle`:**
   ```groovy
   task newTask(type: Exec) {
       group = 'scripts'
       description = 'Description of the new task'
       workingDir = projectDir
       commandLine = ['bash', 'scripts/new-script.sh']
       
       doFirst {
           def scriptFile = file('scripts/new-script.sh')
           if (!scriptFile.canExecute()) {
               scriptFile.setExecutable(true)
           }
       }
   }
   ```

2. **Create the corresponding bash script**
3. **Document the new functionality**

## Troubleshooting

### Script not found
If a script is not found, check:
- The file exists in `scripts/`
- The path is correct in `build.gradle`
- Permissions are correct

### Permission error
Tasks automatically set execution permissions, but if there are issues:
```bash
chmod +x scripts/*.sh
```

### Gradle wrapper not found
If the wrapper JAR is missing:
```bash
# Download the wrapper JAR manually
curl -o gradle/wrapper/gradle-wrapper.jar https://github.com/gradle/gradle/raw/v8.5/distributions/gradle-8.5-bin/gradle/wrapper/gradle-wrapper.jar
```

## IDE Integration

### IntelliJ IDEA
- Tasks appear automatically in the Gradle tab
- Can be executed with double-click
- Integration with run configurations

### Eclipse
- Install the Gradle plugin
- Tasks appear in the Gradle Tasks view
- Execution via project context

### VS Code
- Install the Gradle for Java extension
- Tasks appear in the Gradle tab
- Execution via command palette 