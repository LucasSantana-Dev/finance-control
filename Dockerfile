# syntax=docker/dockerfile:1

FROM openjdk:21-slim AS base

WORKDIR /app

# Install dev tools only if needed
ARG DEV_MODE=false
RUN if [ "$DEV_MODE" = "true" ]; then apt-get update && apt-get install -y curl; fi

# Install dos2unix for line ending conversion
RUN apt-get update && apt-get install -y dos2unix

# Copy all necessary files for Gradle
COPY gradlew ./
COPY gradle gradle/
COPY build.gradle settings.gradle ./

# Copy configuration files
COPY checkstyle.xml spotbugs-exclude.xml pmd-ruleset.xml ./

# Ensure gradlew is executable and has correct line endings
RUN chmod +x gradlew && dos2unix gradlew

# Diagnostic: list files and show gradlew content
RUN ls -la /app
RUN head -10 gradlew

# Try running gradlew directly, fallback to sh gradlew if not found
RUN ./gradlew --version || sh gradlew --version

# Download dependencies
RUN ./gradlew dependencies --no-daemon || sh gradlew dependencies --no-daemon

COPY src src

    # Build the app (for prod)
    ARG SKIP_TESTS=false
    RUN if [ "$SKIP_TESTS" = "true" ]; then ./gradlew compileJava processResources classes bootJar --no-daemon || sh gradlew compileJava processResources classes bootJar --no-daemon; else ./gradlew build --no-daemon || sh gradlew build --no-daemon; fi

# Final image
FROM openjdk:21-slim
WORKDIR /app
COPY --from=base /app/build/libs/finance-control-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080 5005
ENTRYPOINT ["java", "-jar", "app.jar"]
