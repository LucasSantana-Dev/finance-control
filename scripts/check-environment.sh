#!/bin/bash

# Environment Check Script
# This script checks the current environment and provides guidance

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

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check Java
check_java() {
    print_status "Checking Java installation..."
    
    if command_exists java; then
        java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        print_success "Java found: $java_version"
        
        if [[ $java_version == 21* ]]; then
            print_success "✅ Java 21 detected (compatible with project)"
        else
            print_warning "⚠️  Java version $java_version detected (project requires Java 21)"
        fi
    else
        print_error "❌ Java not found in PATH"
        return 1
    fi
}

# Function to check JAVA_HOME
check_java_home() {
    print_status "Checking JAVA_HOME..."
    
    if [ -n "$JAVA_HOME" ]; then
        print_status "JAVA_HOME is set to: $JAVA_HOME"
        
        if [ -d "$JAVA_HOME" ]; then
            print_success "✅ JAVA_HOME directory exists"
            
            if [ -f "$JAVA_HOME/bin/java" ]; then
                print_success "✅ Java executable found in JAVA_HOME"
            else
                print_warning "⚠️  Java executable not found in JAVA_HOME/bin/"
            fi
        else
            print_error "❌ JAVA_HOME directory does not exist"
            return 1
        fi
    else
        print_warning "⚠️  JAVA_HOME is not set"
    fi
}

# Function to check Gradle
check_gradle() {
    print_status "Checking Gradle..."
    
    # Check if gradlew exists
    if [ -f "gradlew" ]; then
        print_success "✅ Gradle wrapper found"
        chmod +x gradlew
        
        # If inside Docker, run as usual
        if [ -f /.dockerenv ] || grep -q docker /proc/1/cgroup 2>/dev/null; then
            if ./gradlew --version > /dev/null 2>&1; then
                gradle_version=$(./gradlew --version | grep "Gradle" | head -n 1)
                print_success "✅ Gradle working: $gradle_version"
                return 0
            else
                print_warning "⚠️  Gradle wrapper exists but command failed"
                return 1
            fi
        # If Docker is available, run Gradle in Docker
        elif command -v docker >/dev/null 2>&1; then
            print_status "Testing Gradle wrapper inside Docker container (base target)"
            docker build -t finance-control-build --target base .
            if docker run --rm \
                -v "$PWD":/app \
                -v "$PWD/build":/app/build \
                -w /app \
                finance-control-build \
                ./gradlew --version > /dev/null 2>&1; then
                gradle_version=$(docker run --rm \
                    -v "$PWD":/app \
                    -v "$PWD/build":/app/build \
                    -w /app \
                    finance-control-build \
                    ./gradlew --version | grep "Gradle" | head -n 1)
                print_success "✅ Gradle working in Docker: $gradle_version"
                return 0
            else
                print_warning "⚠️  Gradle wrapper exists but Docker command failed"
                return 1
            fi
        else
            # Fallback to local execution
            if ./gradlew --version > /dev/null 2>&1; then
                gradle_version=$(./gradlew --version | grep "Gradle" | head -n 1)
                print_success "✅ Gradle working: $gradle_version"
                return 0
            else
                print_warning "⚠️  Gradle wrapper exists but command failed"
                return 1
            fi
        fi
    else
        print_error "❌ Gradle wrapper not found"
        return 1
    fi
}

# Function to check Docker
check_docker() {
    print_status "Checking Docker..."
    
    if command_exists docker; then
        docker_version=$(docker --version)
        print_success "✅ Docker found: $docker_version"
        
        # Test Docker
        if docker info > /dev/null 2>&1; then
            print_success "✅ Docker is running"
            return 0
        else
            print_warning "⚠️  Docker found but not running"
            return 1
        fi
    else
        print_error "❌ Docker not found"
        return 1
    fi
}

# Function to test Docker build
test_docker_build() {
    print_status "Testing Docker build..."
    
    if docker build --target base . > /dev/null 2>&1; then
        print_success "✅ Docker build works!"
        return 0
    else
        print_warning "⚠️  Docker build failed"
        return 1
    fi
}

# Function to provide recommendations
provide_recommendations() {
    echo ""
    print_status "📋 ENVIRONMENT SUMMARY"
    echo "${'='*50}"
    
    local java_ok=false
    local gradle_ok=false
    local docker_ok=false
    
    # Check Java
    if check_java && check_java_home; then
        java_ok=true
    fi
    
    # Check Gradle
    if check_gradle; then
        gradle_ok=true
    fi
    
    # Check Docker
    if check_docker && test_docker_build; then
        docker_ok=true
    fi
    
    echo ""
    print_status "🎯 RECOMMENDATIONS"
    echo "${'='*50}"
    
    if [ "$java_ok" = true ] && [ "$gradle_ok" = true ]; then
        print_success "✅ Local environment is ready for Gradle development"
        echo "  - Use: ./gradlew build"
        echo "  - Use: ./gradlew test"
        echo "  - Use: ./scripts/quality-check.sh"
    elif [ "$docker_ok" = true ]; then
        print_success "✅ Docker environment is ready for development"
        echo "  - Use: docker-compose build"
        echo "  - Use: docker-compose up app"
        echo "  - Use: docker-compose exec app ./gradlew test"
        
        if [ "$java_ok" = false ]; then
            print_warning "⚠️  Consider installing Java 21 for local development"
        fi
        if [ "$gradle_ok" = false ]; then
            print_warning "⚠️  Gradle wrapper issues - use Docker for consistent builds"
        fi
    else
        print_error "❌ No working environment found"
        echo "  - Install Java 21"
        echo "  - Install Docker"
        echo "  - Check JAVA_HOME configuration"
    fi
    
    echo ""
    print_status "🚀 QUICK START COMMANDS"
    echo "${'='*50}"
    
    if [ "$docker_ok" = true ]; then
        echo "  # Build and run with Docker (recommended)"
        echo "  docker-compose build"
        echo "  docker-compose up app"
        echo ""
        echo "  # Run quality checks in Docker"
        echo "  docker-compose exec app ./gradlew qualityCheck"
        echo ""
    fi
    
    if [ "$gradle_ok" = true ]; then
        echo "  # Build locally"
        echo "  ./gradlew build"
        echo ""
        echo "  # Run quality checks locally"
        echo "  ./scripts/quality-check.sh"
        echo ""
    fi
    
    echo "  # Remove Maven files (if ready)"
    echo "  ./scripts/remove-maven.sh"
}

# Main script logic
main() {
    echo "🔍 Environment Check Script"
    echo "=========================="
    echo ""
    
    # Check if we're in the right directory
    if [ ! -f "build.gradle" ]; then
        print_error "build.gradle not found. Please run this script from the project root directory."
        exit 1
    fi
    
    # Run all checks
    provide_recommendations
}

# Run main function
main "$@" 