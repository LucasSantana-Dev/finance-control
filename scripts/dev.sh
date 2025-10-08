#!/bin/bash

# Finance Control - Development Script
# Everything runs in Docker - no local environment setup needed

set -e

# Source modules
source "$(dirname "$0")/modules/core.sh"
source "$(dirname "$0")/modules/services.sh"
source "$(dirname "$0")/modules/build.sh"
source "$(dirname "$0")/modules/sonarqube.sh"
source "$(dirname "$0")/modules/devshell.sh"

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
    echo "  sonarqube-start       # Start SonarQube service"
    echo "  sonarqube-stop        # Stop SonarQube service"
    echo "  sonarqube-logs        # Show SonarQube logs"
    echo "  sonarqube-scan [--no-test] # Run SonarQube analysis"
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
    echo "  $0 sonarqube-start    # Start SonarQube service"
    echo "  $0 sonarqube-scan     # Run SonarQube analysis"
    echo "  $0 sonarqube-scan --no-test # Run SonarQube analysis without tests"
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

# Function to get the correct project path for Docker on Windows
get_docker_path() {
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
        # On Windows with Git Bash, convert the path to Docker-compatible format
        local current_dir=$(pwd)
        # Convert Windows path to Unix-style path for Docker
        echo "${current_dir}" | sed 's/^\([A-Za-z]\):/\/\1/' | sed 's/\\/\//g'
    else
        # On Linux/Mac, use current directory
        echo "$(pwd)"
    fi
}

# Function to start services with proper path resolution and retry limit
start_services() {
    local max_retries=2
    local retry_count=0
    
    echo -e "${BLUE}[INFO]${NC} Starting services..."
    
    # Set the correct path for Docker volume mounts on Windows
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
        export COMPOSE_CONVERT_WINDOWS_PATHS=1
        echo -e "${YELLOW}[INFO]${NC} Windows detected, enabling path conversion..."
    fi
    
    while [ $retry_count -lt $max_retries ]; do
        if docker-compose up -d; then
            echo -e "${GREEN}[SUCCESS]${NC} Services started successfully!"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                echo -e "${YELLOW}[WARNING]${NC} Failed to start services, retrying... (attempt $retry_count/$max_retries)"
                sleep 3
            else
                echo -e "${RED}[ERROR]${NC} Failed to start services after $max_retries attempts"
                return 1
            fi
        fi
    done
}

# Function to stop services with retry limit
stop_services() {
    local max_retries=2
    local retry_count=0
    
    echo -e "${BLUE}[INFO]${NC} Stopping services..."
    
    while [ $retry_count -lt $max_retries ]; do
        if docker-compose down; then
            echo -e "${GREEN}[SUCCESS]${NC} Services stopped successfully!"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                echo -e "${YELLOW}[WARNING]${NC} Failed to stop services, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                echo -e "${RED}[ERROR]${NC} Failed to stop services after $max_retries attempts"
                return 1
            fi
        fi
    done
}

# Function to clean everything
clean_services() {
    echo -e "${BLUE}[INFO]${NC} Cleaning all containers, networks, and volumes..."
    docker-compose down -v
    docker system prune -f
    echo -e "${GREEN}[SUCCESS]${NC} Cleanup completed!"
}

# Function to clean up with --no-test support
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

# Function to show logs with retry limit
show_logs() {
    local max_retries=2
    local retry_count=0
    
    echo -e "${BLUE}[INFO]${NC} Showing logs..."
    
    while [ $retry_count -lt $max_retries ]; do
        if docker-compose logs -f; then
            # If logs command succeeds, exit normally
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                echo -e "${YELLOW}[WARNING]${NC} Logs command failed, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                echo -e "${RED}[ERROR]${NC} Failed to show logs after $max_retries attempts"
                echo -e "${BLUE}[INFO]${NC} Checking if services are running..."
                if ! docker-compose ps | grep -q "Up"; then
                    echo -e "${YELLOW}[WARNING]${NC} No services are running. Start services first with: $0 start"
                fi
                return 1
            fi
        fi
    done
}

# Function to build the project with retry limit
build_project() {
    local max_retries=2
    local retry_count=0
    
    echo -e "${BLUE}[INFO]${NC} Building project..."
    
    while [ $retry_count -lt $max_retries ]; do
        if docker-compose run --rm build; then
            echo -e "${GREEN}[SUCCESS]${NC} Project built successfully!"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                echo -e "${YELLOW}[WARNING]${NC} Build failed, retrying... (attempt $retry_count/$max_retries)"
                sleep 3
            else
                echo -e "${RED}[ERROR]${NC} Build failed after $max_retries attempts"
                return 1
            fi
        fi
    done
}

# Function to run tests with timeout
run_tests() {
    local skip_tests=false
    local test_timeout=600  # 10 minutes timeout
    local max_retries=2
    local retry_count=0
    
    # Check for --no-test parameter
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    
    while [ $retry_count -lt $max_retries ]; do
        if [ "$skip_tests" = true ]; then
            print_status "Running tests (skipping compilation)..."
            print_status "⏱️  Test timeout set to ${test_timeout}s (10 minutes)"
            
            # Run tests with timeout
            if gtimeout $test_timeout SKIP_TESTS=true docker-compose --profile test up test --abort-on-container-exit 2>/dev/null || SKIP_TESTS=true docker-compose --profile test up test --abort-on-container-exit; then
                print_success "Tests completed (compilation skipped)!"
                return 0
            else
                local exit_code=$?
                if [ $exit_code -eq 124 ]; then
                    print_error "❌ Tests timed out after ${test_timeout}s (10 minutes)"
                    print_status "💡 Try running tests with --no-test flag: $0 test --no-test"
                else
                    print_error "❌ Tests failed with exit code $exit_code"
                fi
            fi
        else
            print_status "Running tests..."
            print_status "⏱️  Test timeout set to ${test_timeout}s (10 minutes)"
            
            # Run tests with timeout
            if gtimeout $test_timeout docker-compose --profile test up test --abort-on-container-exit 2>/dev/null || docker-compose --profile test up test --abort-on-container-exit; then
                print_success "Tests completed successfully!"
                return 0
            else
                local exit_code=$?
                if [ $exit_code -eq 124 ]; then
                    print_error "❌ Tests timed out after ${test_timeout}s (10 minutes)"
                    print_status "💡 Try running tests with --no-test flag: $0 test --no-test"
                else
                    print_error "❌ Tests failed with exit code $exit_code"
                fi
            fi
        fi
        
        retry_count=$((retry_count + 1))
        if [ $retry_count -lt $max_retries ]; then
            print_warning "Tests failed, retrying... (attempt $retry_count/$max_retries)"
            sleep 3
        else
            print_error "Tests failed after $max_retries attempts"
            print_status "💡 Troubleshooting tips:"
            print_status "   - Check Docker is running: docker info"
            print_status "   - Clean and retry: $0 clean && $0 test"
            print_status "   - Try with --no-test: $0 test --no-test"
            return 1
        fi
    done
}

# Function to run quality checks with timeout
run_quality() {
    local skip_tests=false
    local quality_timeout=900  # 15 minutes timeout
    local max_retries=2
    local retry_count=0
    
    # Check for --no-test parameter
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    
    while [ $retry_count -lt $max_retries ]; do
        if [ "$skip_tests" = true ]; then
            print_status "Running quality checks in Docker (skipping tests)..."
            print_status "⏱️  Quality check timeout set to ${quality_timeout}s (15 minutes)"
            
            # Run quality checks with timeout (macOS compatible)
            if gtimeout $quality_timeout SKIP_TESTS=true docker-compose --profile quality up quality --abort-on-container-exit 2>/dev/null || SKIP_TESTS=true docker-compose --profile quality up quality --abort-on-container-exit; then
                print_success "Quality checks completed (tests skipped)!"
                return 0
            else
                local exit_code=$?
                if [ $exit_code -eq 124 ]; then
                    print_error "❌ Quality checks timed out after ${quality_timeout}s (15 minutes)"
                    print_status "💡 Try running quality checks with --no-test flag: $0 quality --no-test"
                else
                    print_error "❌ Quality checks failed with exit code $exit_code"
                fi
            fi
        else
            print_status "Running quality checks in Docker..."
            print_status "⏱️  Quality check timeout set to ${quality_timeout}s (15 minutes)"
            
            # Run quality checks with timeout (macOS compatible)
            if gtimeout $quality_timeout docker-compose --profile quality up quality --abort-on-container-exit 2>/dev/null || docker-compose --profile quality up quality --abort-on-container-exit; then
                print_success "Quality checks completed successfully!"
                return 0
            else
                local exit_code=$?
                if [ $exit_code -eq 124 ]; then
                    print_error "❌ Quality checks timed out after ${quality_timeout}s (15 minutes)"
                    print_status "💡 Try running quality checks with --no-test flag: $0 quality --no-test"
                else
                    print_error "❌ Quality checks failed with exit code $exit_code"
                fi
            fi
        fi
        
        retry_count=$((retry_count + 1))
        if [ $retry_count -lt $max_retries ]; then
            print_warning "Quality checks failed, retrying... (attempt $retry_count/$max_retries)"
            sleep 3
        else
            print_error "Quality checks failed after $max_retries attempts"
            print_status "💡 Troubleshooting tips:"
            print_status "   - Check Docker is running: docker info"
            print_status "   - Clean and retry: $0 clean && $0 quality"
            print_status "   - Try with --no-test: $0 quality --no-test"
            return 1
        fi
    done
}

# Function to open development shell with retry limit
open_dev_shell() {
    local max_retries=2
    local retry_count=0
    
    echo -e "${BLUE}[INFO]${NC} Opening development shell..."
    
    while [ $retry_count -lt $max_retries ]; do
        if docker-compose run --rm dev bash; then
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                echo -e "${YELLOW}[WARNING]${NC} Failed to open development shell, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                echo -e "${RED}[ERROR]${NC} Failed to open development shell after $max_retries attempts"
                echo -e "${BLUE}[INFO]${NC} Make sure the development container is properly configured"
                return 1
            fi
        fi
    done
}

# Function to check environment with retry limit
check_environment() {
    local max_retries=2
    local retry_count=0
    
    echo -e "${BLUE}[INFO]${NC} Checking environment..."
    
    while [ $retry_count -lt $max_retries ]; do
        if docker-compose run --rm check-env; then
            echo -e "${GREEN}[SUCCESS]${NC} Environment check completed!"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                echo -e "${YELLOW}[WARNING]${NC} Environment check failed, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                echo -e "${RED}[ERROR]${NC} Environment check failed after $max_retries attempts"
                return 1
            fi
        fi
    done
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
    local max_retries=2
    local retry_count=0
    
    print_status "Starting development shell..."
    
    while [ $retry_count -lt $max_retries ]; do
        if docker-compose --profile dev up -d dev; then
            print_success "Development container started!"
            print_status "Connect with: docker-compose exec dev bash"
            print_status "Or run: $0 dev"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "Failed to start development container, retrying... (attempt $retry_count/$max_retries)"
                sleep 3
            else
                print_error "Failed to start development container after $max_retries attempts"
                return 1
            fi
        fi
    done
}

run_build() {
    local skip_tests=false
    local build_timeout=900  # 15 minutes timeout
    local max_retries=2
    local retry_count=0
    
    # Check for --no-test parameter
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    
    while [ $retry_count -lt $max_retries ]; do
        if [ "$skip_tests" = true ]; then
            print_status "Building application (skipping tests)..."
            print_status "⏱️  Build timeout set to ${build_timeout}s (15 minutes)"
            
            # Run build with timeout
            if gtimeout $build_timeout SKIP_TESTS=true docker-compose --profile build up build --abort-on-container-exit 2>/dev/null || SKIP_TESTS=true docker-compose --profile build up build --abort-on-container-exit; then
                print_success "Application built successfully (tests skipped)!"
                return 0
            else
                local exit_code=$?
                if [ $exit_code -eq 124 ]; then
                    print_error "❌ Build timed out after ${build_timeout}s (15 minutes)"
                    print_status "💡 Try building with --no-test flag to skip tests: $0 build --no-test"
                else
                    print_error "❌ Build failed with exit code $exit_code"
                fi
            fi
        else
            print_status "Building application..."
            print_status "⏱️  Build timeout set to ${build_timeout}s (15 minutes)"
            
            # Run build with timeout
            if gtimeout $build_timeout docker-compose --profile build up build --abort-on-container-exit 2>/dev/null || docker-compose --profile build up build --abort-on-container-exit; then
                print_success "Application built successfully!"
                return 0
            else
                local exit_code=$?
                if [ $exit_code -eq 124 ]; then
                    print_error "❌ Build timed out after ${build_timeout}s (15 minutes)"
                    print_status "💡 Try building with --no-test flag to skip tests: $0 build --no-test"
                else
                    print_error "❌ Build failed with exit code $exit_code"
                fi
            fi
        fi
        
        retry_count=$((retry_count + 1))
        if [ $retry_count -lt $max_retries ]; then
            print_warning "Build failed, retrying... (attempt $retry_count/$max_retries)"
            sleep 5
        else
            print_error "Build failed after $max_retries attempts"
            print_status "💡 Troubleshooting tips:"
            print_status "   - Check Docker is running: docker info"
            print_status "   - Clean and rebuild: $0 clean && $0 build"
            print_status "   - Try with --no-test: $0 build --no-test"
            return 1
        fi
    done
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
    if docker-compose --profile sonarqube ps sonarqube | grep -q "Up"; then
        echo ""
        print_status "🚀 SonarQube is running!"
        print_status "Access SonarQube at: http://localhost:9000"
        print_status "To run analysis: $0 sonarqube-scan"
    else
        echo ""
        print_warning "⚠️  SonarQube is not running."
        echo ""
        print_status "To start SonarQube:"
        echo "   $0 sonarqube-start"
        echo ""
        print_status "📋 To run SonarQube analysis:"
        echo "   $0 sonarqube-scan"
        echo ""
        print_status "📊 For now, you can view local reports at:"
        echo "   - Quality Report: build/quality-report-no-tests.txt"
        echo "   - Checkstyle: build/reports/checkstyle/"
        echo "   - PMD: build/reports/pmd/"
        echo "   - SpotBugs: build/reports/spotbugs/"
    fi
    
    echo ""
    if [ "$skip_tests" = true ]; then
        print_success "🎉 Quality analysis completed (tests skipped)!"
    else
        print_success "🎉 Quality analysis completed!"
    fi
    echo "📋 Review the reports above to see detailed results."
}

run_checkstyle_clean() {
    local max_retries=2
    local retry_count=0
    
    print_status "🧹 Cleaning project..."
    
    # Clean with retry
    while [ $retry_count -lt $max_retries ]; do
        if ./gradlew clean; then
            print_success "✅ Clean completed successfully"
            break
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "Clean failed, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                print_error "❌ Clean failed after $max_retries attempts"
                exit 1
            fi
        fi
    done
    
    # Reset retry counter for Checkstyle
    retry_count=0
    
    print_status "🔍 Running Checkstyle with stacktrace..."
    
    # Checkstyle with retry
    while [ $retry_count -lt $max_retries ]; do
        if ./gradlew checkstyleMain --stacktrace; then
            print_success "✅ Checkstyle completed successfully"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "Checkstyle failed, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                print_warning "⚠️  Checkstyle found violations after $max_retries attempts"
                print_status "📊 Check Checkstyle report at: build/reports/checkstyle/"
                return 1
            fi
        fi
    done
}

start_sonarqube() {
    local max_retries=2
    local retry_count=0
    
    print_status "🚀 Starting SonarQube service..."
    
    while [ $retry_count -lt $max_retries ]; do
        if docker-compose --profile sonarqube up -d sonarqube; then
            print_success "SonarQube service started!"
            print_status "Access SonarQube at: http://localhost:9000"
            print_status "Default credentials: ${SONAR_DB_USER}/${SONAR_DB_PASSWORD}"
            print_status "Wait a few minutes for SonarQube to fully start..."
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "Failed to start SonarQube, retrying... (attempt $retry_count/$max_retries)"
                sleep 3
            else
                print_error "Failed to start SonarQube after $max_retries attempts"
                return 1
            fi
        fi
    done
}

stop_sonarqube() {
    local max_retries=2
    local retry_count=0
    
    print_status "🛑 Stopping SonarQube service..."
    
    while [ $retry_count -lt $max_retries ]; do
        if docker-compose --profile sonarqube down sonarqube; then
            print_success "SonarQube service stopped!"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "Failed to stop SonarQube, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                print_error "Failed to stop SonarQube after $max_retries attempts"
                return 1
            fi
        fi
    done
}

show_sonarqube_logs() {
    local max_retries=2
    local retry_count=0
    
    print_status "📋 Showing SonarQube logs..."
    
    while [ $retry_count -lt $max_retries ]; do
        if docker-compose --profile sonarqube logs -f sonarqube; then
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "Failed to show SonarQube logs, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                print_error "Failed to show SonarQube logs after $max_retries attempts"
                print_status "Checking if SonarQube is running..."
                if ! docker-compose --profile sonarqube ps sonarqube | grep -q "Up"; then
                    print_warning "SonarQube is not running. Start it first with: $0 sonarqube-start"
                fi
                return 1
            fi
        fi
    done
}

run_sonarqube_scan() {
    local skip_tests=false
    local sonar_timeout=900  # 15 minutes timeout for SonarQube analysis
    local build_timeout=900  # 15 minutes timeout for build
    local max_retries=2
    local retry_count=0
    
    # Check for --no-test parameter
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    
    print_status "🔍 Running SonarQube analysis..."
    
    # Check if SonarQube is running
    if ! docker-compose --profile sonarqube ps sonarqube | grep -q "Up"; then
        print_warning "SonarQube is not running. Starting it first..."
        start_sonarqube
        print_status "Waiting for SonarQube to be ready..."
        sleep 30
    fi
    
    # Check if we're in the project root
    if [ ! -f "build.gradle" ]; then
        print_error "Error: This script must be run from the project root directory"
        exit 1
    fi
    
    # Check if Java/Gradle are available
    if ! command -v java >/dev/null 2>&1; then
        print_error "Java not found. Please install Java 21 or use Docker commands."
        exit 1
    fi
    
    if ! command -v ./gradlew >/dev/null 2>&1; then
        print_error "Gradle wrapper not found. Please use Docker commands."
        exit 1
    fi
    
    # Build the project first with timeout
    if [ "$skip_tests" = true ]; then
        print_status "🔨 Building project for SonarQube analysis (skipping tests)..."
        print_status "⏱️  Build timeout set to ${build_timeout}s (15 minutes)"
        
        if gtimeout $build_timeout ./gradlew clean build -x test 2>/dev/null || ./gradlew clean build -x test; then
            print_success "✅ Build completed successfully (tests skipped)"
        else
            local exit_code=$?
            if [ $exit_code -eq 124 ]; then
                print_error "❌ Build timed out after ${build_timeout}s (15 minutes)"
                print_status "💡 Try building with --no-test flag: $0 sonarqube-scan --no-test"
            else
                print_error "❌ Build failed with exit code $exit_code"
            fi
            exit 1
        fi
    else
        print_status "🔨 Building project for SonarQube analysis..."
        print_status "⏱️  Build timeout set to ${build_timeout}s (15 minutes)"
        
        if gtimeout $build_timeout ./gradlew clean build 2>/dev/null || ./gradlew clean build; then
            print_success "✅ Build completed successfully"
        else
            local exit_code=$?
            if [ $exit_code -eq 124 ]; then
                print_error "❌ Build timed out after ${build_timeout}s (15 minutes)"
                print_status "💡 Try building with --no-test flag: $0 sonarqube-scan --no-test"
            else
                print_error "❌ Build failed with exit code $exit_code"
            fi
            exit 1
        fi
    fi
    
    # Run SonarQube analysis with timeout
    print_status "🔍 Running SonarQube analysis..."
    print_status "⏱️  SonarQube analysis timeout set to ${sonar_timeout}s (15 minutes)"
    
    while [ $retry_count -lt $max_retries ]; do
        if gtimeout $sonar_timeout ./gradlew sonarqube 2>/dev/null || ./gradlew sonarqube; then
            print_success "✅ SonarQube analysis completed successfully"
            print_status "📊 View results at: http://localhost:9000"
            return 0
        else
            local exit_code=$?
            if [ $exit_code -eq 124 ]; then
                print_error "❌ SonarQube analysis timed out after ${sonar_timeout}s (15 minutes)"
                print_status "💡 Try running with --no-test flag: $0 sonarqube-scan --no-test"
            else
                print_error "❌ SonarQube analysis failed with exit code $exit_code"
            fi
            
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "SonarQube analysis failed, retrying... (attempt $retry_count/$max_retries)"
                sleep 5
            else
                print_error "SonarQube analysis failed after $max_retries attempts"
                print_status "💡 Troubleshooting tips:"
                print_status "   - Check SonarQube is running: $0 sonarqube-logs"
                print_status "   - Restart SonarQube: $0 sonarqube-stop && $0 sonarqube-start"
                print_status "   - Try with --no-test: $0 sonarqube-scan --no-test"
                return 1
            fi
        fi
    done
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
        sonarqube-start)
            check_docker
            start_sonarqube
            ;;
        sonarqube-stop)
            check_docker
            stop_sonarqube
            ;;
        sonarqube-logs)
            check_docker
            show_sonarqube_logs
            ;;
        sonarqube-scan)
            run_sonarqube_scan "$@"
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
        check-env)
            check_environment
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