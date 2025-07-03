#!/bin/bash

# Finance Control - Core Utilities Module
# Common functions, colors, and configuration

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Print functions
print_status() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Check if Docker is available and running
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

# Show usage information
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