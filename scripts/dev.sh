#!/bin/bash

# Finance Control - Development Script
# Everything runs in Docker - no local environment setup needed

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

show_usage() {
    echo "Finance Control - Development Script"
    echo "==================================="
    echo ""
    echo "Usage: $0 <command> [options]"
    echo ""
    echo "Commands:"
    echo "  start [--no-test]     # Start application (app + database)"
    echo "  dev                   # Start development shell"
    echo "  build [--no-test]     # Build the application"
    echo "  test [--no-test]      # Run tests"
    echo "  quality [--no-test]   # Run quality checks (Docker)"
    echo "  quality-local [--no-test] # Run quality checks (local)"
    echo "  checkstyle-clean      # Clean and run Checkstyle with stacktrace"
    echo "  logs                  # Show application logs"
    echo "  stop                  # Stop all services"
    echo "  clean [--no-test]     # Clean up containers and volumes"
    echo ""
    echo "Options:"
    echo "  --no-test             # Skip test compilation and execution"
    echo ""
    echo "Examples:"
    echo "  $0 start              # Start the app with tests"
    echo "  $0 start --no-test    # Start the app without tests"
    echo "  $0 dev                # Open development shell"
    echo "  $0 build              # Build the project with tests"
    echo "  $0 build --no-test    # Build the project without tests"
    echo "  $0 test               # Run tests"
    echo "  $0 test --no-test     # Run tests without compilation"
    echo "  $0 quality --no-test  # Run quality checks without tests"
    echo "  $0 checkstyle-clean   # Clean and run Checkstyle with stacktrace"
    echo ""
    echo "Development workflow:"
    echo "  1. $0 start           # Start app and database"
    echo "  2. $0 dev             # Open shell for development"
    echo "  3. Inside dev: ./gradlew build test"
    echo "  4. Access app at: http://localhost:8080"
}

check_docker() {
    if ! command -v docker >/dev/null 2>&1; then
        print_error "Docker not found. Please install Docker first."
        exit 1
    fi
    
    if ! docker info >/dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker."
        exit 1
    fi
}

start_app() {
    local skip_tests=false
    
    # Check for --no-test parameter
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    
    if [ "$skip_tests" = true ]; then
        print_status "Starting application (skipping tests)..."
        SKIP_TESTS=true docker-compose up -d
        print_success "Application started (tests skipped)!"
    else
        print_status "Starting application..."
        docker-compose up -d
        print_success "Application started!"
    fi
    
    print_status "Access at: http://localhost:8080"
    print_status "Database at: localhost:5432"
}

start_dev() {
    print_status "Starting development shell..."
    docker-compose --profile dev up -d dev
    print_success "Development container started!"
    print_status "Connect with: docker-compose exec dev bash"
    print_status "Or run: $0 dev"
}

open_dev_shell() {
    print_status "Opening development shell..."
    docker-compose exec dev bash
}

run_build() {
    local skip_tests=false
    
    # Check for --no-test parameter
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    
    if [ "$skip_tests" = true ]; then
        print_status "Building application (skipping tests)..."
        SKIP_TESTS=true docker-compose --profile build up build --abort-on-container-exit
        print_success "Application built successfully (tests skipped)!"
    else
        print_status "Building application..."
        docker-compose --profile build up build --abort-on-container-exit
        print_success "Application built successfully!"
    fi
}

run_tests() {
    local skip_tests=false
    
    # Check for --no-test parameter
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    
    if [ "$skip_tests" = true ]; then
        print_status "Running tests (skipping compilation)..."
        SKIP_TESTS=true docker-compose --profile test up test --abort-on-container-exit
        print_success "Tests completed (compilation skipped)!"
    else
        print_status "Running tests..."
        docker-compose --profile test up test --abort-on-container-exit
        print_success "Tests completed successfully!"
    fi
}

run_quality() {
    local skip_tests=false
    
    # Check for --no-test parameter
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    
    if [ "$skip_tests" = true ]; then
        print_status "Running quality checks in Docker (skipping tests)..."
        SKIP_TESTS=true docker-compose --profile quality up quality --abort-on-container-exit
    else
        print_status "Running quality checks in Docker..."
        docker-compose --profile quality up quality --abort-on-container-exit
    fi
}

run_quality_local() {
    local skip_tests=false
    
    # Check for --no-test parameter
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    
    print_status "Running quality checks locally..."
    
    # Check if we're in the project root
    if [ ! -f "build.gradle" ]; then
        print_error "Error: This script must be run from the project root directory"
        exit 1
    fi
    
    # Check if Java/Gradle are available
    if ! command -v java >/dev/null 2>&1; then
        print_error "Java not found. Please install Java 21 or use 'quality' command for Docker."
        exit 1
    fi
    
    if ! command -v ./gradlew >/dev/null 2>&1; then
        print_error "Gradle wrapper not found. Please use 'quality' command for Docker."
        exit 1
    fi
    
    # Clean previous reports
    print_status "🧹 Cleaning previous reports..."
    ./gradlew clean
    
    # Run Checkstyle
    print_status "🔍 Running Checkstyle..."
    if ./gradlew checkstyleMain; then
        print_success "✅ Checkstyle completed successfully"
    else
        print_warning "⚠️  Checkstyle found violations"
    fi
    
    # Run PMD
    print_status "🔍 Running PMD..."
    if ./gradlew pmdMain; then
        print_success "✅ PMD completed successfully"
    else
        print_warning "⚠️  PMD found violations"
    fi
    
    # Run SpotBugs
    print_status "🔍 Running SpotBugs..."
    if ./gradlew spotbugsMain; then
        print_success "✅ SpotBugs completed successfully"
    else
        print_warning "⚠️  SpotBugs found violations"
    fi
    
    # Run tests with coverage (only if not skipping)
    if [ "$skip_tests" = false ]; then
        print_status "🧪 Running tests with coverage..."
        if ./gradlew test jacocoTestReport; then
            print_success "✅ Tests completed successfully"
        else
            print_error "❌ Tests failed"
            exit 1
        fi
    else
        print_status "🧪 Skipping tests (--no-test flag used)"
    fi
    
    # Generate quality report
    print_status "📊 Generating quality report..."
    if [ "$skip_tests" = true ]; then
        ./gradlew qualityCheckNoTests
    else
        ./gradlew qualityCheck
    fi
    
    # Show summary
    echo ""
    echo "🎯 QUALITY ANALYSIS SUMMARY"
    echo "=========================="
    echo "📁 Reports location: build/reports/"
    echo "📊 Checkstyle: build/reports/checkstyle/"
    echo "📊 PMD: build/reports/pmd/"
    echo "📊 SpotBugs: build/reports/spotbugs/"
    if [ "$skip_tests" = false ]; then
        echo "📊 Tests: build/reports/tests/"
        echo "📊 Coverage: build/reports/jacoco/"
        echo "📄 Quality Report: build/quality-report.txt"
    else
        echo "📊 Tests: SKIPPED (--no-test flag used)"
        echo "📊 Coverage: SKIPPED (--no-test flag used)"
        echo "📄 Quality Report: build/quality-report-no-tests.txt"
    fi
    
    # Check if SonarQube is available
    if command -v sonar-scanner &> /dev/null; then
        echo ""
        print_status "🚀 To upload to SonarQube, run: ./gradlew sonarqube"
    else
        echo ""
        print_warning "⚠️  SonarQube scanner not found. Install it to upload results."
    fi
    
    echo ""
    if [ "$skip_tests" = true ]; then
        print_success "🎉 Quality analysis completed (tests skipped)!"
    else
        print_success "🎉 Quality analysis completed!"
    fi
    echo "📋 Review the reports above to see detailed results."
}

show_logs() {
    print_status "Showing application logs..."
    docker-compose logs -f app
}

stop_services() {
    print_status "Stopping all services..."
    docker-compose down
    print_success "All services stopped!"
}

clean_up() {
    local skip_tests=false
    
    # Check for --no-test parameter
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    
    if [ "$skip_tests" = true ]; then
        print_status "Cleaning up containers and volumes (skipping test cleanup)..."
        SKIP_TESTS=true docker-compose down -v --remove-orphans
        docker system prune -f
        print_success "Cleanup completed (test cleanup skipped)!"
    else
        print_status "Cleaning up containers and volumes..."
        docker-compose down -v --remove-orphans
        docker system prune -f
        print_success "Cleanup completed!"
    fi
}

run_checkstyle_clean() {
    print_status "🧹 Cleaning project..."
    if ./gradlew clean; then
        print_success "✅ Clean completed successfully"
    else
        print_error "❌ Clean failed"
        exit 1
    fi
    
    print_status "🔍 Running Checkstyle with stacktrace..."
    if ./gradlew checkstyleMain --stacktrace; then
        print_success "✅ Checkstyle completed successfully"
    else
        print_warning "⚠️  Checkstyle found violations"
        print_status "📊 Check Checkstyle report at: build/reports/checkstyle/"
    fi
}

main() {
    if [ $# -eq 0 ]; then
        show_usage
        exit 1
    fi
    
    case "$1" in
        start)
            check_docker
            start_app "$@"
            ;;
        dev)
            check_docker
            if [ "$2" = "shell" ]; then
                open_dev_shell
            else
                start_dev
            fi
            ;;
        build)
            check_docker
            run_build "$@"
            ;;
        test)
            check_docker
            run_tests "$@"
            ;;
        quality)
            check_docker
            run_quality "$@"
            ;;
        quality-local)
            run_quality_local "$@"
            ;;
        checkstyle-clean)
            run_checkstyle_clean
            ;;
        logs)
            check_docker
            show_logs
            ;;
        stop)
            check_docker
            stop_services
            ;;
        clean)
            check_docker
            clean_up "$@"
            ;;
        -h|--help)
            show_usage
            ;;
        *)
            print_error "Unknown command: $1"
            show_usage
            exit 1
            ;;
    esac
}

main "$@" 