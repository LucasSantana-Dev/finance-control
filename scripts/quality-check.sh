#!/bin/bash

# Quality Check Script for Gradle
# This script runs all code quality checks for the Finance Control project

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

# Function to check if we're in a Docker environment
is_docker() {
    [ -f /.dockerenv ] || grep -q docker /proc/1/cgroup 2>/dev/null
}

# Function to make gradlew executable
make_gradlew_executable() {
    if [ -f "gradlew" ]; then
        chmod +x gradlew
        print_status "Made gradlew executable"
    else
        print_error "gradlew not found in current directory"
        exit 1
    fi
}

# Function to run quality checks
run_quality_checks() {
    local verbose=$1
    
    print_status "Starting quality checks..."
    
    # Make gradlew executable
    make_gradlew_executable
    
    # Check if we're in Docker
    if is_docker; then
        print_status "Running in Docker environment"
        GRADLE_CMD="./gradlew"
        # Run quality checks with logging and colored output
        print_status "Running comprehensive quality checks with logging..."
        
        # Create log directory
        mkdir -p build/logs
        
        # Enable colored output for Gradle
        export GRADLE_OPTS="-Dorg.gradle.console=rich"
        
        # Create timestamp for log files
        local timestamp=$(date +"%Y%m%d_%H%M%S")
        local log_file="build/logs/quality-check_${timestamp}.log"
        local summary_log="build/logs/quality-check-summary_${timestamp}.log"
        
        print_status "Log files will be saved to:"
        print_status "  - Detailed log: ${log_file}"
        print_status "  - Summary log: ${summary_log}"
        
        if [ "$verbose" = "true" ]; then
            $GRADLE_CMD qualityCheck --info --console=rich 2>&1 | tee "$log_file"
        else
            $GRADLE_CMD qualityCheck --console=rich 2>&1 | tee "$log_file"
        fi
        
        # Create a summary log with key information
        {
            echo "QUALITY CHECK SUMMARY - $(date)"
            echo "=================================="
            echo "Timestamp: $(date)"
            echo "Command: $GRADLE_CMD qualityCheck"
            echo "Verbose: $verbose"
            echo "Log file: $log_file"
            echo ""
            echo "EXIT CODE: ${PIPESTATUS[0]}"
            echo ""
            echo "For detailed output, see: $log_file"
        } > "$summary_log"
        
        # Check if quality check was successful
        if [ ${PIPESTATUS[0]} -eq 0 ]; then
            print_success "All quality checks completed successfully!"
        else
            print_warning "Quality checks completed with some issues. Check the logs for details."
        fi
        
        # Show summary of violations
        print_status "Generating quality report..."
        $GRADLE_CMD generateQualityReport
        
        # Display summary with colors
        if [ -f "build/quality-report.txt" ]; then
            print_status "Quality report generated. Summary of violations:"
            echo ""
            echo "🔍 VIOLATIONS SUMMARY:"
            echo "${'='*50}"
            
            local violation_count=0
            while IFS= read -r line; do
                if [[ $line == ✗* ]]; then
                    echo "❌ ${line#✗ }"
                    ((violation_count++))
                fi
            done < build/quality-report.txt
            
            echo "${'='*50}"
            echo "📊 Total violations found: $violation_count"
            
            if [ $violation_count -eq 0 ]; then
                print_success "🎉 No violations found!"
            else
                print_warning "⚠️  Please fix these violations to improve code quality"
            fi
            
            echo ""
            print_status "Full report available at: build/quality-report.txt"
        fi
        
        # Show test results summary
        if [ -f "build/test-results/test-results.txt" ]; then
            print_status "Test results summary:"
            cat build/test-results/test-results.txt
            echo ""
        fi
    else
        # Not in Docker, check if Docker is available
        if command_exists docker; then
            print_status "Running Gradle quality checks inside Docker container (base target)"
            mkdir -p build/logs
            local timestamp=$(date +"%Y%m%d_%H%M%S")
            local log_file="build/logs/quality-check_${timestamp}.log"
            local summary_log="build/logs/quality-check-summary_${timestamp}.log"
            print_status "Log files will be saved to:"
            print_status "  - Detailed log: ${log_file}"
            print_status "  - Summary log: ${summary_log}"
            # Compose the Docker run command
            local gradle_args="qualityCheck --console=rich"
            if [ "$verbose" = "true" ]; then
                gradle_args="qualityCheck --info --console=rich"
            fi
            docker build -t finance-control-build --target base .
            docker run --rm \
                -v "$PWD":/app \
                -v "$PWD/build":/app/build \
                -w /app \
                finance-control-build \
                ./gradlew $gradle_args 2>&1 | tee "$log_file"
            # Generate quality report
            docker run --rm \
                -v "$PWD":/app \
                -v "$PWD/build":/app/build \
                -w /app \
                finance-control-build \
                ./gradlew generateQualityReport
            # Create a summary log with key information
            {
                echo "QUALITY CHECK SUMMARY - $(date)"
                echo "=================================="
                echo "Timestamp: $(date)"
                echo "Command: ./gradlew $gradle_args (Docker)"
                echo "Verbose: $verbose"
                echo "Log file: $log_file"
                echo ""
                echo "For detailed output, see: $log_file"
            } > "$summary_log"
            # Show summary of violations
            if [ -f "build/quality-report.txt" ]; then
                print_status "Quality report generated. Summary of violations:"
                echo ""
                echo "🔍 VIOLATIONS SUMMARY:"
                echo "$(printf '=%.0s' {1..50})"
                local violation_count=0
                while IFS= read -r line; do
                    if [[ $line == ✗* ]]; then
                        echo "❌ ${line#✗ }"
                        ((violation_count++))
                    fi
                done < build/quality-report.txt
                echo "$(printf '=%.0s' {1..50})"
                echo "📊 Total violations found: $violation_count"
                if [ $violation_count -eq 0 ]; then
                    print_success "🎉 No violations found!"
                else
                    print_warning "⚠️  Please fix these violations to improve code quality"
                fi
                echo ""
                print_status "Full report available at: build/quality-report.txt"
            fi
            # Show test results summary
            if [ -f "build/test-results/test-results.txt" ]; then
                print_status "Test results summary:"
                cat build/test-results/test-results.txt
                echo ""
            fi
            return
        else
            print_warning "Docker not available, falling back to local Gradle execution."
            if command_exists gradle; then
                GRADLE_CMD="gradle"
            else
                GRADLE_CMD="./gradlew"
            fi
            # Run quality checks with logging and colored output
            print_status "Running comprehensive quality checks with logging..."
            
            # Create log directory
            mkdir -p build/logs
            
            # Enable colored output for Gradle
            export GRADLE_OPTS="-Dorg.gradle.console=rich"
            
            # Create timestamp for log files
            local timestamp=$(date +"%Y%m%d_%H%M%S")
            local log_file="build/logs/quality-check_${timestamp}.log"
            local summary_log="build/logs/quality-check-summary_${timestamp}.log"
            
            print_status "Log files will be saved to:"
            print_status "  - Detailed log: ${log_file}"
            print_status "  - Summary log: ${summary_log}"
            
            if [ "$verbose" = "true" ]; then
                $GRADLE_CMD qualityCheck --info --console=rich 2>&1 | tee "$log_file"
            else
                $GRADLE_CMD qualityCheck --console=rich 2>&1 | tee "$log_file"
            fi
            
            # Create a summary log with key information
            {
                echo "QUALITY CHECK SUMMARY - $(date)"
                echo "=================================="
                echo "Timestamp: $(date)"
                echo "Command: $GRADLE_CMD qualityCheck"
                echo "Verbose: $verbose"
                echo "Log file: $log_file"
                echo ""
                echo "EXIT CODE: ${PIPESTATUS[0]}"
                echo ""
                echo "For detailed output, see: $log_file"
            } > "$summary_log"
            
            # Check if quality check was successful
            if [ ${PIPESTATUS[0]} -eq 0 ]; then
                print_success "All quality checks completed successfully!"
            else
                print_warning "Quality checks completed with some issues. Check the logs for details."
            fi
            
            # Show summary of violations
            print_status "Generating quality report..."
            $GRADLE_CMD generateQualityReport
            
            # Display summary with colors
            if [ -f "build/quality-report.txt" ]; then
                print_status "Quality report generated. Summary of violations:"
                echo ""
                echo "🔍 VIOLATIONS SUMMARY:"
                echo "${'='*50}"
                
                local violation_count=0
                while IFS= read -r line; do
                    if [[ $line == ✗* ]]; then
                        echo "❌ ${line#✗ }"
                        ((violation_count++))
                    fi
                done < build/quality-report.txt
                
                echo "${'='*50}"
                echo "📊 Total violations found: $violation_count"
                
                if [ $violation_count -eq 0 ]; then
                    print_success "🎉 No violations found!"
                else
                    print_warning "⚠️  Please fix these violations to improve code quality"
                fi
                
                echo ""
                print_status "Full report available at: build/quality-report.txt"
            fi
            
            # Show test results summary
            if [ -f "build/test-results/test-results.txt" ]; then
                print_status "Test results summary:"
                cat build/test-results/test-results.txt
                echo ""
            fi
        fi
    fi
}

# Function to show help
show_help() {
    echo "Quality Check Script for Finance Control Project"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -h, --help     Show this help message"
    echo "  -v, --verbose  Run with verbose output"
    echo "  --docker       Force Docker environment detection"
    echo ""
    echo "This script runs the following quality checks:"
    echo "  - Checkstyle (code style)"
    echo "  - PMD (static analysis)"
    echo "  - SpotBugs (bug detection)"
    echo "  - Unit tests"
    echo "  - JaCoCo coverage report"
    echo "  - Coverage threshold verification"
    echo ""
    echo "Reports will be generated in:"
    echo "  - build/reports/checkstyle/"
    echo "  - build/reports/pmd/"
    echo "  - build/reports/spotbugs/"
    echo "  - build/reports/jacoco/"
}

# Main script logic
main() {
    local verbose=false
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -v|--verbose)
                verbose=true
                shift
                ;;
            --docker)
                # Force Docker environment
                export DOCKER_ENV=true
                shift
                ;;
            *)
                print_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # Check if we're in the right directory
    if [ ! -f "build.gradle" ]; then
        print_error "build.gradle not found. Please run this script from the project root directory."
        exit 1
    fi
    
    # Run quality checks
    run_quality_checks "$verbose"
}

# Run main function with all arguments
main "$@" 