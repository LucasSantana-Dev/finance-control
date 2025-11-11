# CI/CD Setup Guide

This document provides detailed instructions for setting up the CI/CD pipeline with quality gates for the Finance Control project.

## Overview

The project uses GitHub Actions for automated CI/CD with comprehensive quality assurance:
- **CI Pipeline**: Automated build, tests, and quality checks on every push/PR
- **SonarQube Cloud Analysis**: Code quality analysis with SonarQube Cloud
- **Quality Gates**: Enforced standards for code quality, security, and coverage

## GitHub Actions Workflows

### CI Pipeline (`ci.yml`)
- **Triggers**: Push to main/develop, Pull Requests
- **Jobs**:
  - `build-and-test`: Compiles code, runs tests, generates coverage (parallel)
  - `quality-checks`: Runs Checkstyle, PMD, SpotBugs (parallel)
  - `security-scan`: Runs OWASP Dependency Check (parallel)
  - `ci-summary`: Aggregates results
- **Duration**: ~3-8 minutes (optimized with caching and parallelization)
- **Artifacts**: Quality reports, test results, coverage reports
- **Optimizations**:
  - Advanced Gradle dependency caching
  - Build cache enabled for faster rebuilds
  - Parallel job execution
  - Test result caching
  - Artifact sharing between jobs

### SonarQube Cloud Analysis (`sonarqube-cloud.yml`)
- **Triggers**: Push to main, Pull Requests (opened, synchronize, reopened)
- **Jobs**: Build and analyze with SonarQube Cloud
- **Duration**: ~3-6 minutes (optimized with caching)
- **Artifacts**: Analysis results available in SonarQube Cloud dashboard
- **Optimizations**:
  - Automatic Gradle dependency caching via setup-java
  - Build cache enabled
  - SonarQube package caching
  - Full git history for better analysis

### Qodana Code Quality Analysis (`qodana_code_quality.yml`)
- **Triggers**: Push to main, Pull Requests, Manual trigger
- **Jobs**: Runs JetBrains Qodana static analysis
- **Duration**: ~2-5 minutes
- **Features**: Code quality analysis, PR comments, GitHub annotations
- **Configuration**: Optional workflow (`continue-on-error: true`) - runs when `QODANA_TOKEN` is available
- **Note**: Requires `QODANA_TOKEN` secret for full functionality (PR comments, annotations)

## Required GitHub Secrets

### SONAR_TOKEN
**Required for SonarQube analysis workflow**

#### How to Create SonarQube Cloud Token

### QODANA_TOKEN (Optional)
**Optional for Qodana code quality analysis workflow**

The Qodana workflow is configured to run optionally (`continue-on-error: true`). If you want to use Qodana for advanced features like PR comments and GitHub annotations:

#### How to Create Qodana Token

1. **Access Qodana Cloud**:
   - Go to `https://qodana.cloud`
   - Login with your GitHub account

2. **Create Organization/Project**:
   - Create or join an organization
   - Add the `finance-control` project

3. **Generate Token**:
   - Go to organization settings
   - Navigate to **Tokens**
   - Create a new token for the project
   - Copy the token

4. **Add to GitHub Secrets**:
   - Repository: `LucasSantana-Dev/finance-control`
   - Settings → Secrets and variables → Actions
   - New secret: `QODANA_TOKEN`
   - Paste the token value

**Note**: Without this token, Qodana will still run basic analysis but won't post PR comments or GitHub annotations.

#### How to Create SonarQube Cloud Token

1. **Access SonarQube Cloud**:
   - Go to `https://sonarcloud.io`
   - Login with your GitHub account (or create an account)

2. **Create Project** (if not already created):
   - Navigate to your organization: `lucassantana-dev`
   - Click **Add Project** → **From GitHub**
   - Select the `finance-control` repository
   - Follow the setup wizard

3. **Generate Project Token**:
   - Go to your project: **LucasSantana-Dev_finance-control**
   - Navigate to: **Project Settings** → **Analysis Method** → **GitHub Actions**
   - Or go to: **My Account** → **Security** → **Generate Token**
   - Enter token name: `finance-control-ci`
   - Select project: `LucasSantana-Dev_finance-control`
   - Click **Generate Token**
   - **Important**: Copy the token immediately (it won't be shown again)

4. **Add to GitHub Secrets**:
   - Go to your GitHub repository: `LucasSantana-Dev/finance-control`
   - Navigate to: **Settings** → **Secrets and variables** → **Actions**
   - Click **New repository secret**
   - Name: `SONAR_TOKEN`
   - Value: Paste the token from step 3
   - Click **Add secret**

#### Token Permissions
The token automatically has the following permissions in SonarQube Cloud:
- Execute Analysis
- Create Projects (if project doesn't exist yet)

## NVD API Key Setup (Optional but Recommended)

The OWASP Dependency Check tool can run significantly faster with an NVD (National Vulnerability Database) API key. Without an API key, requests are limited to 5 per 30 seconds; with an API key, this increases to 50 requests per 30 seconds.

### 1. Request NVD API Key

1. **Visit NVD API Key Request Page**:
   - Go to: https://nvd.nist.gov/developers/request-an-api-key

2. **Fill out the Request Form**:
   - Provide your organization name
   - Enter a valid email address
   - Select your organization type
   - Scroll to the end and check "I agree to the Terms of Use"
   - Click **Submit Request**

3. **Activate Your Key**:
   - Check your email for an activation link
   - Click the link to activate and view your API key
   - **Note**: The activation link is valid for 7 days

### 2. Add to GitHub Secrets

1. **Navigate to Repository Secrets**:
   - Go to your GitHub repository: `LucasSantana-Dev/finance-control`
   - Navigate to: **Settings** → **Secrets and variables** → **Actions**
   - Click **New repository secret**

2. **Configure the Secret**:
   - Name: `NVD_API_KEY`
   - Value: Paste your NVD API key from the activation email
   - Click **Add secret**

### 3. Benefits

- **10x faster scans**: Rate limit increases from 5 to 50 requests per 30 seconds
- **Reduced CI time**: Security scans complete faster
- **Better developer experience**: Faster local development scans

### 4. Notes

- The API key is optional - scans work without it, just slower
- The CI/CD pipeline is configured to use the API key automatically if the secret is set
- For local development, set the `NVD_API_KEY` environment variable

## Performance Optimizations

### Caching Strategy

The CI/CD pipeline implements comprehensive caching to reduce build times:

1. **Gradle Dependency Caching**:
   - Automatic via `setup-java@v4` with `cache: 'gradle'`
   - Caches `~/.gradle/caches` and `~/.gradle/wrapper`
   - Cache key based on Gradle files hash

2. **Build Cache**:
   - Enabled in `gradle.properties` with `org.gradle.caching=true`
   - Caches compiled classes and build outputs
   - Significantly speeds up incremental builds

3. **Test Result Caching**:
   - Caches test results to enable smart test skipping
   - Only re-runs tests for changed code

4. **SonarQube Package Caching**:
   - Caches SonarQube analysis packages
   - Reduces analysis setup time

5. **OWASP Dependency Check Caching**:
   - Caches vulnerability database
   - Avoids re-downloading on every run

### Parallel Job Execution

The CI pipeline runs jobs in parallel for maximum efficiency:

- **build-and-test**: Runs first (required for other jobs)
- **quality-checks**: Runs in parallel after build completes
- **security-scan**: Runs in parallel after build completes
- **ci-summary**: Aggregates all results

This parallelization reduces total pipeline time by ~40-50%.

### Concurrency Control

Workflows use concurrency groups to:
- Cancel in-progress runs when new commits are pushed
- Prevent resource waste from multiple simultaneous runs
- Ensure only the latest commit is analyzed

## Local Development Setup

### Running Quality Checks Locally

```bash
# Run all quality checks
./gradlew qualityCheck

# Run tests with coverage
./gradlew test jacocoTestReport

# Run SonarQube Cloud analysis (requires SONAR_TOKEN environment variable)
export SONAR_TOKEN=your-token-here
./gradlew build sonar

# Run security scan
./gradlew dependencyCheckAnalyze
```

### SonarQube Cloud Setup

The project uses SonarQube Cloud for code quality analysis. No local Docker setup is required.

**Project Configuration:**
- **Organization**: `lucassantana-dev`
- **Project Key**: `LucasSantana-Dev_finance-control`
- **Analysis**: Automatic on every push to main and Pull Requests

**Local Analysis:**
```bash
# Set your SonarQube Cloud token
export SONAR_TOKEN=your-token-here

# Run analysis
./gradlew build sonar
```

### SonarCloud Analysis Configuration

**Coverage and Test Reports:**
- JaCoCo XML report: `build/reports/jacoco/test/jacocoTestReport.xml`
- JUnit reports: `build/test-results/test`
- Coverage exclusions: config, dto, model, exception, enums, util, validation packages

**Analysis Exclusions:**
- Generated code: `**/generated/**`, `**/build/**`
- Test code: `**/target/**`

**Quality Gate Requirements:**
- Overall coverage: ≥80%
- New code coverage: ≥80%
- No new critical/blocker issues allowed

### SonarCloud Issue Triage

**Retrieve Issues via API:**
```bash
# Get all critical and major issues
curl -s "https://sonarcloud.io/api/issues/search?organization=lucassantana-dev&componentKeys=LucasSantana-Dev_finance_control&severities=BLOCKER,CRITICAL,MAJOR&ps=500"

# Get all issues (including minor)
curl -s "https://sonarcloud.io/api/issues/search?organization=lucassantana-dev&componentKeys=LucasSantana-Dev_finance_control&ps=500"

# Filter by issue type (VULNERABILITY, BUG, CODE_SMELL)
curl -s "https://sonarcloud.io/api/issues/search?organization=lucassantana-dev&componentKeys=LucasSantana-Dev_finance_control&types=VULNERABILITY&ps=500"
```

**Issue Prioritization:**
1. **Security Issues (VULNERABILITY)**: Highest priority
2. **Bugs (BUG)**: Reliability issues
3. **Code Smells (CODE_SMELL)**: Maintainability issues

**Common Resolution Actions:**
- **Won't Fix**: For false positives with justification
- **False Positive**: When issue doesn't apply to codebase
- **Fix**: Address the underlying issue
- **Review**: Requires manual review

## Quality Standards

### Code Quality Gates
- **Checkstyle**: No violations (coding standards)
- **PMD**: No violations (code quality rules)
- **SpotBugs**: No high/critical violations (bug detection)
- **Test Coverage**: Minimum 80% required
- **SonarQube**: Quality gate must pass

### Security Requirements
- **OWASP Dependency Check**: No high/critical vulnerabilities
- **Input Validation**: All user inputs validated
- **Authentication**: Proper JWT implementation
- **Secrets Management**: No hardcoded secrets

## Troubleshooting

### Common Issues

#### SonarQube Cloud Connection Failed
- Verify `SONAR_TOKEN` secret is set correctly in GitHub repository settings
- Check token is valid: Go to SonarQube Cloud → My Account → Security
- Ensure project exists: Verify `LucasSantana-Dev_finance-control` exists in organization `lucassantana-dev`
- Check workflow logs for detailed error messages

#### Quality Checks Failing
- Run locally: `./gradlew qualityCheck`
- Check specific tool output: `./gradlew checkstyleMain pmdMain spotbugsMain`
- Fix violations and commit

#### OWASP Scan Issues
- Clear cache: `rm -rf ~/.gradle/caches`
- Update database: `./gradlew dependencyCheckUpdate`
- Check for false positives in `dependency-check-suppression.xml`

### Workflow Logs
- Access workflow runs in GitHub Actions tab
- Download artifacts for detailed reports
- Check SonarQube UI for analysis results

## Configuration Files

### Key Configuration Files
- `.github/workflows/ci.yml` - Optimized CI pipeline with parallel jobs
- `.github/workflows/sonarqube-cloud.yml` - Optimized SonarQube Cloud analysis
- `.github/workflows/sonarqube.yml.disabled` - Legacy self-hosted SonarQube (disabled)
- `.github/workflows/qodana_code_quality.yml` - Qodana code quality analysis (optional)
- `sonar-project.properties` - SonarQube configuration (legacy, optional)
- `build.gradle` - Quality check and SonarQube Cloud configuration
- `gradle.properties` - Gradle performance optimizations and build cache
- `checkstyle.xml` - Checkstyle rules
- `pmd-ruleset.xml` - PMD rules
- `spotbugs-exclude.xml` - SpotBugs exclusions

### Environment Variables
- `SONAR_TOKEN` - SonarQube Cloud project analysis token (GitHub secret, required)
- `QODANA_TOKEN` - Qodana Cloud project analysis token (GitHub secret, optional)
- `SONAR_HOST_URL` - Only needed for self-hosted SonarQube (not used with SonarQube Cloud)

## Best Practices

### Commit Messages
Follow Angular conventional commits:
```
feat: add transaction reconciliation
fix: resolve null pointer in market data
docs: update CI/CD setup guide
```

### Pull Requests
- Use the PR template with quality checklist
- Ensure all quality gates pass
- Include test coverage for new features
- Update documentation and CHANGELOG

### Branch Strategy
- `main`: Production-ready code
- `develop`: Integration branch
- Feature branches: `feat/feature-name`
- Bug fixes: `fix/issue-description`

## Monitoring & Alerts

### Quality Metrics
- Track coverage trends in SonarQube Cloud dashboard
- Monitor build times and failure rates in GitHub Actions
- Review security vulnerabilities regularly in SonarQube Cloud
- Update dependencies for security patches
- View analysis results at: `https://sonarcloud.io/project/overview?id=LucasSantana-Dev_finance-control`

### Notifications
- GitHub Actions notifications on failures
- SonarQube Cloud quality gate alerts (configured in project settings)
- Security scan failure alerts
- Pull Request comments with analysis results (automatic)

## Support

For issues with CI/CD setup:
1. Check workflow logs in GitHub Actions
2. Verify local environment matches CI
3. Review configuration files (`build.gradle`, workflow files)
4. Verify `SONAR_TOKEN` secret is set correctly in GitHub
5. Check SonarQube Cloud project status: `https://sonarcloud.io/project/overview?id=LucasSantana-Dev_finance-control`
6. Review this documentation

## Related Documentation

- [README.md](../README.md) - Project overview and setup
- [TESTING_STRATEGY.md](TESTING_STRATEGY.md) - Testing guidelines
- [CODE_QUALITY.md](CODE_QUALITY.md) - Code quality standards
- [DOCKER.md](DOCKER.md) - Docker setup and usage
