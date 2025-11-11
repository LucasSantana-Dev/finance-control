# CI/CD Setup Guide

This document provides detailed instructions for setting up the CI/CD pipeline with quality gates for the Finance Control project.

## Overview

The project uses GitHub Actions for automated CI/CD with comprehensive quality assurance:
- **CI Pipeline**: Automated build, tests, and quality checks on every push/PR
- **SonarQube Analysis**: Code quality analysis with self-hosted SonarQube instance
- **Quality Gates**: Enforced standards for code quality, security, and coverage

## GitHub Actions Workflows

### CI Pipeline (`ci.yml`)
- **Triggers**: Push to main/develop, Pull Requests
- **Jobs**: Build, test, quality checks, security scan
- **Duration**: ~5-10 minutes
- **Artifacts**: Quality reports, test results, coverage reports

### SonarQube Analysis (`sonarqube.yml`)
- **Triggers**: Push to main, manual dispatch
- **Jobs**: Self-hosted SonarQube with PostgreSQL, full analysis
- **Duration**: ~10-15 minutes
- **Artifacts**: SonarQube reports, coverage data

## Required GitHub Secrets

### SONAR_TOKEN
**Required for SonarQube analysis workflow**

#### How to Create SonarQube Token

1. **Access SonarQube UI**:
   - If using self-hosted: `http://localhost:9000` (after running `docker-compose --profile sonarqube up`)
   - If using SonarCloud: `https://sonarcloud.io`

2. **Login as Administrator**:
   - Default credentials: `admin` / `admin`
   - Change password on first login

3. **Create Project Token**:
   - Navigate to: **Administration** → **Security** → **Users**
   - Click on your user account
   - Scroll to **Tokens** section
   - Click **Generate Token**
   - Enter token name: `finance-control-ci`
   - Copy the generated token immediately

4. **Add to GitHub Secrets**:
   - Go to your GitHub repository
   - Navigate to: **Settings** → **Secrets and variables** → **Actions**
   - Click **New repository secret**
   - Name: `SONAR_TOKEN`
   - Value: Paste the token from step 3
   - Click **Add secret**

#### Token Permissions
The token needs the following permissions in SonarQube:
- Execute Analysis
- Create Projects (if project doesn't exist yet)

## Local Development Setup

### Running Quality Checks Locally

```bash
# Run all quality checks
./gradlew qualityCheck

# Run tests with coverage
./gradlew test jacocoTestReport

# Run SonarQube analysis (requires Docker)
docker-compose --profile sonarqube up -d
./gradlew sonarqube

# Run security scan
./gradlew dependencyCheckAnalyze
```

### Docker Compose Profiles

The project includes multiple Docker Compose profiles:

```bash
# Full development environment
docker-compose up

# SonarQube analysis only
docker-compose --profile sonarqube up

# Database only
docker-compose --profile postgres up
```

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

#### SonarQube Connection Failed
- Verify `SONAR_TOKEN` secret is set correctly
- Check if SonarQube container is running: `docker-compose ps`
- Ensure SonarQube is accessible: `curl http://localhost:9000/api/system/status`

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
- `.github/workflows/ci.yml` - CI pipeline
- `.github/workflows/sonarqube.yml` - SonarQube analysis
- `sonar-project.properties` - SonarQube configuration
- `build.gradle` - Quality check configuration
- `checkstyle.xml` - Checkstyle rules
- `pmd-ruleset.xml` - PMD rules
- `spotbugs-exclude.xml` - SpotBugs exclusions

### Environment Variables
- `SONAR_HOST_URL` - SonarQube server URL (default: http://localhost:9000)
- `SONAR_TOKEN` - Project analysis token (GitHub secret)

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
- Track coverage trends in SonarQube
- Monitor build times and failure rates
- Review security vulnerabilities regularly
- Update dependencies for security patches

### Notifications
- GitHub Actions notifications on failures
- SonarQube quality gate alerts
- Security scan failure alerts

## Support

For issues with CI/CD setup:
1. Check workflow logs in GitHub Actions
2. Verify local environment matches CI
3. Review configuration files
4. Check SonarQube server status
5. Review this documentation

## Related Documentation

- [README.md](../README.md) - Project overview and setup
- [TESTING_STRATEGY.md](TESTING_STRATEGY.md) - Testing guidelines
- [CODE_QUALITY.md](CODE_QUALITY.md) - Code quality standards
- [DOCKER.md](DOCKER.md) - Docker setup and usage
