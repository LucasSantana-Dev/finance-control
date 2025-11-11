# Finance Control – Project‑Specific Development Guidelines

This document captures the practical, project‑specific knowledge needed to build, test, and extend this codebase efficiently. It is intended for experienced developers; generic Java/Spring/Gradle details are omitted.

## Build and Configuration

- Tooling
  - Build: Gradle (wrapper committed). Java 21 (see `gradle.properties` and `sonar-project.properties`).
  - Spring Boot application, modules under `src/main/java` and tests under `src/test/java`.
  - CI/Local Dev rely on Docker for a reproducible environment. Prefer the scripts in `scripts/` over ad‑hoc commands.

- Primary entry points
  - Docker compose services: Postgres (`db`), Redis (`redis`), optional SonarQube (`sonarqube`), the app (`app`), and a dev container (`dev`). See `docker-compose.yml` and `docker.env`.
  - Scripted workflows: `./scripts/dev.sh` orchestrates Docker + Gradle. See `scripts/README.md` for the full matrix. Common flows:
    - `./scripts/dev.sh start` – brings up infra and app (with tests).
    - `./scripts/dev.sh start --no-test` – faster bootstrap; skips tests.
    - `./scripts/dev.sh dev` – drops you into the dev container with Gradle available.
    - `./scripts/dev.sh test` / `build` / `quality` – convenience wrappers for Gradle.

- Application configuration
  - Default Docker profile: `SPRING_PROFILES_ACTIVE=docker` (via `docker.env`).
  - Test profile: `test` (integration tests use `@ActiveProfiles("test")`). Integration tests disable Redis autoconfiguration to keep the context slimmer:
    - See `src/test/java/com/finance_control/integration/BaseIntegrationTest.java` where 
      `spring.autoconfigure.exclude` is set for Redis auto‑configs.
  - Database: Postgres 17 via `db` service. Migrations: Flyway at `classpath:db/migration`.
  - Redis is available for caching/rate‑limiting. In tests, Redis auto‑config is excluded unless a test opts in explicitly.

- Local (non‑Docker) build
  - You can build outside Docker if you have JDK 21:
    - `./gradlew build` (warnings from MapStruct/Spring may appear; they don’t fail the build).
    - Skip tests if needed: `./gradlew build -x test`.

## Testing

This repository contains three broad layers of tests:
- Unit tests: lean, JUnit 5 + Mockito, run with `./gradlew test` quickly.
- Integration tests (Spring context + DB): extend `BaseIntegrationTest`, use profile `test` and transactional rollback.
- E2E/Selenium façade: `src/test/java/com/finance_control/e2e/*` uses a base that currently falls back to HTTP client checks via `RestTemplate` on a random port. No real browser is required by default.

### How to run tests
- In Docker dev shell (recommended for parity):
  - `./scripts/dev.sh dev` then inside container: `./gradlew test`.
- Directly on host:
  - `./gradlew test`.
- Selective execution (JUnit 5):
  - Run a single class: `./gradlew test --tests com.finance_control.unit.shared.config.SentryConfigTest`
  - By package (glob): `./gradlew test --tests 'com.finance_control.unit.*'`
  - By method: `./gradlew test --tests 'com.finance_control.unit.users.service.UserServiceTest.someMethod'`

Notes
- Integration/E2E tests that spin up a Spring context are slower; prefer targeting unit packages during TDD cycles.
- The project does not use JUnit `@Tag` filters at the moment; rely on `--tests` globs or Gradle test tasks.

### Adding a new test (demo)
The simplest way to verify the toolchain is to add a tiny JUnit 5 test under the project package and run only that test. Example:

File: `src/test/java/com/finance_control/demo/DemoSanityTest.java`
```java
package com.finance_control.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoSanityTest {
    @Test
    void sanity() {
        assertTrue(true, "JUnit platform is wired correctly");
    }
}
```
Run the test only:
```bash
./gradlew test --tests com.finance_control.demo.DemoSanityTest
```
This isolates feedback from the rest of the suite and confirms the Gradle/JUnit integration is sound.

### Integration test specifics
- Base class: `com.finance_control.integration.BaseIntegrationTest`
  - `@SpringBootTest` with profile `test`.
  - Transactions are rolled back after each test (`@Transactional`).
  - Redis auto‑config is excluded via `@TestPropertySource` to avoid requiring Redis for most integration tests.
- Database schema is provided by Flyway migrations under `src/main/resources/db/migration`.

### E2E/Selenium layer
- Base: `com.finance_control.e2e.BaseSeleniumTest`
  - Uses random port Spring Boot test server and `RestTemplate` for HTTP‑level assertions.
  - Designed to be upgraded to real WebDriver when/if the environment provides a browser; until then, it operates in a headless HTTP mode.

## Quality Gates and Static Analysis
- Code style: `checkstyle.xml` at repo root. Run:
  - `./gradlew checkstyleMain checkstyleTest`
- PMD rules: `pmd-ruleset.xml`. Run:
  - `./gradlew pmdMain pmdTest`
- SpotBugs: see `spotbugs-exclude.xml`. Run:
  - `./gradlew spotbugsMain spotbugsTest`
- SonarQube (optional, via Docker profile `sonarqube`):
  - Start Sonar: `./scripts/dev.sh sonarqube-start`
  - Analyze: `./scripts/dev.sh sonarqube-scan` (coverage from `build/reports/jacoco/test/jacocoTestReport.xml`)

## Project Conventions and Tips
- Java 21 baseline; prefer records and `var` prudently, but keep mappings DTO<->entity explicit (MapStruct is in use; watch for unmapped warnings in mappers like `TransactionMapper`, `FinancialGoalMapper`).
- Spring profiles:
  - `docker` for containerized runtime; `test` for integration tests.
- Test naming: `*Test.java` suffix. Keep unit tests under `src/test/java/com/finance_control/unit/...` when applicable.
- Redis in tests: disabled by default in integration tests. If a test needs Redis, override the `spring.autoconfigure.exclude` property accordingly and provide a test container or a stub.
- Useful scripts
  - `./scripts/dev.sh quality` – aggregated quality checks.
  - `./scripts/dev.sh check-env` – validates local environment prior to builds.
  - `./scripts/dev.sh checkstyle-clean` – resets and runs Checkstyle with stacktraces.
- Troubleshooting
  - If Gradle complains about non‑resolvable configurations, stick to the default `test` task and limit scope via `--tests` rather than custom source sets.
  - Health endpoint for local sanity checks: `curl http://localhost:8080/actuator/health` (after `start`).

## Verified Commands (executed locally during documentation)
- Build and minimal sanity test were executed successfully via Gradle using a small dedicated test class and selective execution.
- Dockerized workflows are exercised through `./scripts/dev.sh`; see `scripts/README.md` for the authoritative list and parameters.
