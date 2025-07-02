#!/bin/bash

# Help Script for Finance Control Project
# This script provides an overview of all available scripts and their usage

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print colored output
print_header() {
    echo -e "${PURPLE}$1${NC}"
}

print_section() {
    echo -e "${CYAN}$1${NC}"
}

print_command() {
    echo -e "${GREEN}$1${NC}"
}

print_description() {
    echo -e "${BLUE}$1${NC}"
}

print_warning() {
    echo -e "${YELLOW}$1${NC}"
}

print_error() {
    echo -e "${RED}$1${NC}"
}

show_script_help() {
    local script_name="$1"
    local description="$2"
    local usage="$3"
    local examples="$4"
    
    echo ""
    print_section "📋 $script_name"
    echo "${'='*60}"
    print_description "$description"
    echo ""
    print_command "Usage: $usage"
    echo ""
    if [ -n "$examples" ]; then
        print_command "Examples:"
        echo "$examples"
        echo ""
    fi
}

show_main_help() {
    echo ""
    print_header "🚀 Finance Control Project - Scripts Help"
    echo "${'='*60}"
    echo ""
    print_description "This project provides several bash scripts to help with development,"
    print_description "testing, and deployment. Each script is designed to work in both"
    print_description "local and Docker environments."
    echo ""
    
    print_section "📁 Available Scripts"
    echo "${'='*60}"
    
    show_script_help \
        "check-environment.sh" \
        "Validates your development environment and provides recommendations" \
        "./scripts/check-environment.sh" \
        "  ./scripts/check-environment.sh"
    
    show_script_help \
        "fix-environment.sh" \
        "Fixes common environment issues (JAVA_HOME, Java version, etc.)" \
        "./scripts/fix-environment.sh" \
        "  ./scripts/fix-environment.sh"
    
    show_script_help \
        "quality-check.sh" \
        "Runs comprehensive code quality checks (Checkstyle, PMD, SpotBugs)" \
        "./scripts/quality-check.sh [--verbose]" \
        "  ./scripts/quality-check.sh\n  ./scripts/quality-check.sh --verbose"
    
    show_script_help \
        "test-fixes.sh" \
        "Tests build fixes and application startup" \
        "./scripts/test-fixes.sh" \
        "  ./scripts/test-fixes.sh"
    
    show_script_help \
        "docker-compose-run.sh" \
        "Runs Docker Compose services for development" \
        "./scripts/docker-compose-run.sh <service>" \
        "  ./scripts/docker-compose-run.sh dev\n  ./scripts/docker-compose-run.sh quality\n  ./scripts/docker-compose-run.sh app"
    
    show_script_help \
        "gradle-with-logs.sh" \
        "Runs Gradle commands with comprehensive logging" \
        "./scripts/gradle-with-logs.sh <gradle-task> [options]" \
        "  ./scripts/gradle-with-logs.sh build\n  ./scripts/gradle-with-logs.sh qualityCheck\n  ./scripts/gradle-with-logs.sh test --info"
    
    show_script_help \
        "remove-maven.sh" \
        "Safely removes Maven files after Gradle migration" \
        "./scripts/remove-maven.sh" \
        "  ./scripts/remove-maven.sh"
    
    echo ""
    print_section "🎯 Quick Start Guide"
    echo "${'='*60}"
    echo ""
    print_command "1. Check your environment:"
    echo "   ./scripts/check-environment.sh"
    echo ""
    print_command "2. Fix any issues:"
    echo "   ./scripts/fix-environment.sh"
    echo ""
    print_command "3. Run quality checks:"
    echo "   ./scripts/quality-check.sh"
    echo ""
    print_command "4. Test the build:"
    echo "   ./scripts/test-fixes.sh"
    echo ""
    print_command "5. Start development:"
    echo "   ./scripts/docker-compose-run.sh dev"
    echo ""
    
    print_section "🔧 Common Workflows"
    echo "${'='*60}"
    echo ""
    print_command "Development Workflow:"
    echo "  1. ./scripts/check-environment.sh"
    echo "  2. ./scripts/docker-compose-run.sh dev"
    echo "  3. docker-compose exec dev ./gradlew build"
    echo "  4. docker-compose exec dev ./gradlew test"
    echo ""
    print_command "Quality Assurance Workflow:"
    echo "  1. ./scripts/quality-check.sh"
    echo "  2. ./scripts/test-fixes.sh"
    echo "  3. ./scripts/gradle-with-logs.sh build"
    echo ""
    print_command "Docker Development Workflow:"
    echo "  1. ./scripts/docker-compose-run.sh app"
    echo "  2. Access at: http://localhost:8080"
    echo "  3. docker-compose logs -f app"
    echo ""
    
    print_section "⚠️  Troubleshooting"
    echo "${'='*60}"
    echo ""
    print_warning "If you encounter issues:"
    echo "  1. Run: ./scripts/fix-environment.sh"
    echo "  2. Check logs in: build/logs/"
    echo "  3. Verify Docker is running"
    echo "  4. Ensure JAVA_HOME is set correctly"
    echo "  5. Try running in Docker: ./scripts/docker-compose-run.sh dev"
    echo ""
    
    print_section "📚 Additional Resources"
    echo "${'='*60}"
    echo ""
    print_description "• Project Documentation: docs/"
    print_description "• API Documentation: http://localhost:8080/swagger-ui.html"
    print_description "• Gradle Tasks: ./gradlew tasks"
    print_description "• Docker Services: docker-compose ps"
    echo ""
}

show_script_specific_help() {
    local script_name="$1"
    
    case "$script_name" in
        "check-environment")
            echo ""
            print_header "🔍 Environment Check Script Help"
            echo "${'='*60}"
            echo ""
            print_description "This script validates your development environment and provides"
            print_description "recommendations for setting up the project correctly."
            echo ""
            print_command "What it checks:"
            echo "  • Java installation and version"
            echo "  • JAVA_HOME configuration"
            echo "  • Gradle wrapper functionality"
            echo "  • Docker availability and functionality"
            echo "  • Build environment compatibility"
            echo ""
            print_command "Usage:"
            echo "  ./scripts/check-environment.sh"
            echo ""
            print_command "Output:"
            echo "  • Environment status summary"
            echo "  • Recommendations for fixes"
            echo "  • Quick start commands"
            ;;
        "fix-environment")
            echo ""
            print_header "🔧 Fix Environment Script Help"
            echo "${'='*60}"
            echo ""
            print_description "This script automatically detects and fixes common environment"
            print_description "issues, particularly Java-related problems."
            echo ""
            print_command "What it fixes:"
            echo "  • JAVA_HOME configuration issues"
            echo "  • Java version compatibility"
            echo "  • Path configuration problems"
            echo "  • Build environment setup"
            echo ""
            print_command "Usage:"
            echo "  ./scripts/fix-environment.sh"
            echo ""
            print_command "Output:"
            echo "  • Java installation detection"
            echo "  • Recommended JAVA_HOME settings"
            echo "  • Build environment testing"
            echo "  • Next steps for setup"
            ;;
        "quality-check")
            echo ""
            print_header "🔍 Quality Check Script Help"
            echo "${'='*60}"
            echo ""
            print_description "This script runs comprehensive code quality checks using"
            print_description "Checkstyle, PMD, and SpotBugs."
            echo ""
            print_command "What it runs:"
            echo "  • Checkstyle (code style and conventions)"
            echo "  • PMD (static code analysis)"
            echo "  • SpotBugs (bug detection)"
            echo "  • Tests (unit and integration)"
            echo "  • Test coverage (JaCoCo)"
            echo ""
            print_command "Usage:"
            echo "  ./scripts/quality-check.sh [--verbose]"
            echo ""
            print_command "Options:"
            echo "  --verbose    Enable detailed output"
            echo ""
            print_command "Output:"
            echo "  • Quality check results"
            echo "  • Violation reports"
            echo "  • Test results summary"
            echo "  • Coverage reports"
            ;;
        "test-fixes")
            echo ""
            print_header "🧪 Test Fixes Script Help"
            echo "${'='*60}"
            echo ""
            print_description "This script tests the fixes for build and application issues"
            print_description "to ensure everything is working correctly."
            echo ""
            print_command "What it tests:"
            echo "  • Gradle build functionality"
            echo "  • Docker build process"
            echo "  • Application startup (optional)"
            echo "  • Database connectivity"
            echo ""
            print_command "Usage:"
            echo "  ./scripts/test-fixes.sh"
            echo ""
            print_command "Output:"
            echo "  • Build test results"
            echo "  • Docker test results"
            echo "  • Application startup test (if requested)"
            echo "  • Overall test summary"
            ;;
        "docker-compose-run")
            echo ""
            print_header "🐳 Docker Compose Run Script Help"
            echo "${'='*60}"
            echo ""
            print_description "This script provides easy access to Docker Compose services"
            print_description "for different development scenarios."
            echo ""
            print_command "Available Services:"
            echo "  dev        - Development container (interactive)"
            echo "  quality    - Run quality checks in Docker"
            echo "  test       - Run tests in Docker"
            echo "  build      - Run build in Docker"
            echo "  app        - Start full application"
            echo "  db         - Start database only"
            echo ""
            print_command "Usage:"
            echo "  ./scripts/docker-compose-run.sh <service>"
            echo ""
            print_command "Examples:"
            echo "  ./scripts/docker-compose-run.sh dev"
            echo "  ./scripts/docker-compose-run.sh quality"
            echo "  ./scripts/docker-compose-run.sh app"
            echo ""
            print_command "Interactive Commands (after starting dev service):"
            echo "  docker-compose exec dev ./gradlew build"
            echo "  docker-compose exec dev ./gradlew qualityCheck"
            echo "  docker-compose exec dev ./scripts/quality-check.sh"
            ;;
        "gradle-with-logs")
            echo ""
            print_header "📝 Gradle with Logs Script Help"
            echo "${'='*60}"
            echo ""
            print_description "This script runs Gradle commands with comprehensive logging"
            print_description "and automatic log file generation."
            echo ""
            print_command "Features:"
            echo "  • Automatic log file creation"
            echo "  • Timestamped log files"
            echo "  • Docker support"
            echo "  • Colored output"
            echo "  • Execution summary"
            echo ""
            print_command "Usage:"
            echo "  ./scripts/gradle-with-logs.sh <gradle-task> [options]"
            echo ""
            print_command "Examples:"
            echo "  ./scripts/gradle-with-logs.sh build"
            echo "  ./scripts/gradle-with-logs.sh qualityCheck"
            echo "  ./scripts/gradle-with-logs.sh test --info"
            echo "  ./scripts/gradle-with-logs.sh clean build --no-daemon"
            echo ""
            print_command "Options:"
            echo "  --verbose    Enable verbose output"
            echo "  --no-daemon  Disable Gradle daemon"
            echo "  --info       Enable info logging"
            echo "  --debug      Enable debug logging"
            ;;
        "remove-maven")
            echo ""
            print_header "🗑️  Remove Maven Script Help"
            echo "${'='*60}"
            echo ""
            print_description "This script safely removes all Maven-related files after"
            print_description "successfully migrating to Gradle."
            echo ""
            print_command "What it removes:"
            echo "  • pom.xml"
            echo "  • mvnw and mvnw.cmd"
            echo "  • .mvn directory"
            echo "  • target directory"
            echo ""
            print_command "Safety features:"
            echo "  • Automatic backup creation"
            echo "  • Gradle verification before removal"
            echo "  • Build testing after removal"
            echo "  • Rollback instructions"
            echo ""
            print_command "Usage:"
            echo "  ./scripts/remove-maven.sh"
            echo ""
            print_command "Output:"
            echo "  • Backup creation confirmation"
            echo "  • Gradle verification results"
            echo "  • File removal summary"
            echo "  • Build test results"
            ;;
        *)
            print_error "Unknown script: $script_name"
            echo ""
            print_command "Available scripts:"
            echo "  check-environment"
            echo "  fix-environment"
            echo "  quality-check"
            echo "  test-fixes"
            echo "  docker-compose-run"
            echo "  gradle-with-logs"
            echo "  remove-maven"
            ;;
    esac
}

# Main script logic
main() {
    if [ $# -eq 0 ]; then
        show_main_help
    elif [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_main_help
    else
        show_script_specific_help "$1"
    fi
}

# Run main function
main "$@" 