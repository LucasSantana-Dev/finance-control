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
- **Jobs**: Build, test, quality checks, security scan
- **Duration**: ~5-10 minutes
- **Artifacts**: Quality reports, test results, coverage reports

### SonarQube Cloud Analysis (`sonarqube-cloud.yml`)
- **Triggers**: Push to main, Pull Requests (opened, synchronize, reopened)
- **Jobs**: Build and analyze with SonarQube Cloud
- **Duration**: ~5-10 minutes
- **Artifacts**: Analysis results available in SonarQube Cloud dashboard

## Required GitHub Secrets

### SONAR_TOKEN
**Required for SonarQube analysis workflow**

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
- `.github/workflows/ci.yml` - CI pipeline
- `.github/workflows/sonarqube-cloud.yml` - SonarQube Cloud analysis
- `.github/workflows/sonarqube.yml` - Legacy self-hosted SonarQube (optional)
- `sonar-project.properties` - SonarQube configuration (legacy, optional)
- `build.gradle` - Quality check and SonarQube Cloud configuration
- `checkstyle.xml` - Checkstyle rules
- `pmd-ruleset.xml` - PMD rules
- `spotbugs-exclude.xml` - SpotBugs exclusions

### Environment Variables
- `SONAR_TOKEN` - SonarQube Cloud project analysis token (GitHub secret, required)
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
