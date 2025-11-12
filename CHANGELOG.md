# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Security Audit**: Comprehensive repository security scan completed - zero hardcoded secrets found
- **Environment Configuration**: Complete `docker.env` file with all configurable environment variables

### Fixed
- **AppProperties Architecture**: Refactored to immutable Java records with constructor binding for better security and performance
- **Code Quality**: Resolved all SpotBugs (104 warnings), Checkstyle (45 warnings), and PMD (34 warnings) issues
- **JWT Security**: Fixed WeakKeyException by upgrading test keys to minimum 256-bit strength
- **Test Configuration**: Fixed @ConfigurationProperties binding issues with record-based configuration
- **Test Suite**: All 1246 tests passing after comprehensive refactoring

## [0.1.0] - 2024-12-19

### Added
- **Transaction Categories Management**: Complete CRUD operations for transaction categories and subcategories

## [0.0.1-SNAPSHOT] - 2024-12-18

### Added
- Initial project setup with Spring Boot 3.x
