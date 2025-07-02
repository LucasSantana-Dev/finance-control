#!/bin/bash

# Gradle with Logs Script
# This script runs Gradle commands with comprehensive logging

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

# Function to show usage
show_usage() {
    echo "Gradle with Logs Script"
    echo "======================"
    echo ""
    echo "Usage: $0 <gradle-task> [options]"
    echo ""
    echo "Examples:"
    echo "  $0 build                    # Run build with logging"
    echo "  $0 qualityCheck             # Run quality checks with logging"
    echo "  $0 test                     # Run tests with logging"
    echo "  $0 clean build              # Run clean and build with logging"
    echo "  $0 --version                # Show Gradle version with logging"
    echo ""
    echo "Options:"
    echo "  --verbose                   # Enable verbose output"
    echo "  --no-daemon                 # Disable Gradle daemon"
    echo "  --info                      # Enable info logging"
    echo "  --debug                     # Enable debug logging"
    echo ""
    echo "Log files will be saved to: build/logs/"
}

# Function to determine Gradle command
get_gradle_cmd() {
    if [ -f /.dockerenv ] || grep -q docker /proc/1/cgroup 2>/dev/null; then
        echo "./gradlew"
    elif command -v docker >/dev/null 2>&1; then
        echo "DOCKER"
    elif command -v gradle >/dev/null 2>&1; then
        echo "gradle"
    else
        echo "./gradlew"
    fi
}

# Function to create log directory and files
setup_logging() {
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local task_name=$(echo "$*" | tr ' ' '_' | tr -d '--')
    
    # Create log directory
    mkdir -p build/logs
    
    # Create log file names
    local log_file="build/logs/gradle_${task_name}_${timestamp}.log"
    local summary_log="build/logs/gradle_${task_name}_summary_${timestamp}.log"
    
    echo "$log_file"
}

# Function to run Gradle command with logging
run_gradle_with_logs() {
    local gradle_cmd=$(get_gradle_cmd)
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local task_name=$(echo "$*" | tr ' ' '_' | tr -d '--')
    mkdir -p build/logs
    local log_file="build/logs/gradle_${task_name}_${timestamp}.log"
    local summary_log="build/logs/gradle_${task_name}_summary_${timestamp}.log"
    print_status "Running: $gradle_cmd $*"
    print_status "Log: $log_file"
    if [ "$gradle_cmd" = "DOCKER" ]; then
        print_status "Running Gradle command inside Docker container (base target)"
        docker build -t finance-control-build --target base .
        docker run --rm \
            -v "$PWD":/app \
            -v "$PWD/build":/app/build \
            -w /app \
            finance-control-build \
            ./gradlew "$@" 2>&1 | tee "$log_file"
        local exit_code=${PIPESTATUS[0]}
        {
            echo "GRADLE EXECUTION SUMMARY"
            echo "Timestamp: $(date)"
            echo "Command: ./gradlew $* (Docker)"
            echo "Log: $log_file"
            echo "Exit code: $exit_code"
        } > "$summary_log"
        return $exit_code
    else
        # Local execution
        local start_time=$(date +%s)
        if $gradle_cmd "$@" 2>&1 | tee "$log_file"; then
            local duration=$(($(date +%s) - start_time))
            print_success "✅ Success! Duration: ${duration}s"
            echo "Exit code: 0 (SUCCESS)" >> "$summary_log"
            echo "Duration: ${duration}s" >> "$summary_log"
        else
            local duration=$(($(date +%s) - start_time))
            local exit_code=${PIPESTATUS[0]}
            print_error "❌ Failed! Exit code: $exit_code"
            echo "Exit code: $exit_code (FAILED)" >> "$summary_log"
            echo "Duration: ${duration}s" >> "$summary_log"
            return $exit_code
        fi
    fi
}

# Main script logic
main() {
    # Check if we're in the right directory
    if [ ! -f "build.gradle" ] && [ ! -f "gradlew" ]; then
        print_error "build.gradle or gradlew not found. Please run this script from the project root directory."
        exit 1
    fi
    
    # Check if arguments are provided
    if [ $# -eq 0 ]; then
        show_usage
        exit 1
    fi
    
    # Check for help flag
    if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_usage
        exit 0
    fi
    
    # Run Gradle command with logging
    run_gradle_with_logs "$@"
}

# Run main function
main "$@" 