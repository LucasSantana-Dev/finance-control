#!/bin/bash

# Test Fixes Script
# This script tests the fixes for build and application issues

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to test Gradle build
test_gradle_build() {
    print_status "Testing Gradle build..."
    
    # Create log directory
    mkdir -p build/logs
    
    # Create timestamp for log files
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local log_file="build/logs/gradle-build-test_${timestamp}.log"
    
    print_status "Log will be saved to: $log_file"
    
    # If inside Docker, run as usual
    if [ -f /.dockerenv ] || grep -q docker /proc/1/cgroup 2>/dev/null; then
        if ./gradlew build --no-daemon 2>&1 | tee "$log_file"; then
            print_success "✅ Gradle build successful!"
            return 0
        else
            print_error "❌ Gradle build failed"
            print_status "Check the log file for details: $log_file"
            return 1
        fi
    # If Docker is available, run Gradle in Docker
    elif command -v docker >/dev/null 2>&1; then
        print_status "Running Gradle build inside Docker container (base target)"
        docker build -t finance-control-build --target base .
        if docker run --rm \
            -v "$PWD":/app \
            -v "$PWD/build":/app/build \
            -w /app \
            finance-control-build \
            ./gradlew build --no-daemon 2>&1 | tee "$log_file"; then
            print_success "✅ Gradle build successful (Docker)!"
            return 0
        else
            print_error "❌ Gradle build failed (Docker)"
            print_status "Check the log file for details: $log_file"
            return 1
        fi
    else
        # Fallback to local execution
        if ./gradlew build --no-daemon 2>&1 | tee "$log_file"; then
            print_success "✅ Gradle build successful!"
            return 0
        else
            print_error "❌ Gradle build failed"
            print_status "Check the log file for details: $log_file"
            return 1
        fi
    fi
}

# Function to test Docker build
test_docker_build() {
    print_status "Testing Docker build..."
    
    # Create timestamp for log files
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local log_file="build/logs/docker-build-test_${timestamp}.log"
    
    print_status "Log will be saved to: $log_file"
    
    if docker build --target base . 2>&1 | tee "$log_file"; then
        print_success "✅ Docker build successful!"
        return 0
    else
        print_error "❌ Docker build failed"
        print_status "Check the log file for details: $log_file"
        return 1
    fi
}

# Function to test application startup
test_application_startup() {
    print_status "Testing application startup..."
    
    # Create timestamp for log files
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local log_file="build/logs/app-startup-test_${timestamp}.log"
    
    print_status "Log will be saved to: $log_file"
    
    # Start the application in background
    docker-compose up -d db
    
    # Wait for database to be ready
    sleep 10
    
    # Try to start the application
    if docker-compose up app --abort-on-container-exit 2>&1 | tee "$log_file"; then
        print_success "✅ Application startup successful!"
        docker-compose down
        return 0
    else
        print_error "❌ Application startup failed"
        print_status "Check the log file for details: $log_file"
        docker-compose down
        return 1
    fi
}

# Main script logic
main() {
    echo "🧪 Test Fixes Script"
    echo "==================="
    echo ""
    
    # Check if we're in the right directory
    if [ ! -f "build.gradle" ]; then
        print_error "build.gradle not found. Please run this script from the project root directory."
        exit 1
    fi
    
    local all_tests_passed=true
    
    # Test 1: Gradle build
    if test_gradle_build; then
        print_success "✅ Test 1 PASSED: Gradle build"
    else
        print_error "❌ Test 1 FAILED: Gradle build"
        all_tests_passed=false
    fi
    
    echo ""
    
    # Test 2: Docker build
    if test_docker_build; then
        print_success "✅ Test 2 PASSED: Docker build"
    else
        print_error "❌ Test 2 FAILED: Docker build"
        all_tests_passed=false
    fi
    
    echo ""
    
    # Test 3: Application startup (optional)
    read -p "Test application startup? (y/N): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        if test_application_startup; then
            print_success "✅ Test 3 PASSED: Application startup"
        else
            print_error "❌ Test 3 FAILED: Application startup"
            all_tests_passed=false
        fi
    else
        print_status "Skipping application startup test"
    fi
    
    echo ""
    print_status "📊 TEST RESULTS SUMMARY"
    echo "${'='*40}"
    
    if [ "$all_tests_passed" = true ]; then
        print_success "🎉 ALL TESTS PASSED!"
        print_success "✅ Build issues have been fixed"
        print_success "✅ Application should start correctly"
        echo ""
        print_status "You can now:"
        echo "  - Run: ./gradlew build"
        echo "  - Run: docker-compose up"
        echo "  - Remove Maven files: ./scripts/remove-maven.sh"
    else
        print_error "❌ SOME TESTS FAILED"
        print_warning "Please check the errors above and fix them"
    fi
}

# Run main function
main "$@" 