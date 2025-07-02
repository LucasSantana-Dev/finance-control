#!/bin/bash

# Fix Environment Script
# This script fixes common environment issues and provides diagnostics

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

# Function to detect OS
detect_os() {
    case "$(uname -s)" in
        Linux*)     echo "linux";;
        Darwin*)    echo "macos";;
        CYGWIN*)    echo "windows";;
        MINGW*)     echo "windows";;
        MSYS*)      echo "windows";;
        *)          echo "unknown";;
    esac
}

# Function to find Java installations
find_java_installations() {
    local os=$(detect_os)
    local java_paths=()
    
    print_status "Searching for Java installations..."
    
    if [ "$os" = "windows" ]; then
        # Windows paths
        local windows_paths=(
            "/c/Program Files/Java"
            "/c/Program Files (x86)/Java"
            "/d/Program Files/Java"
            "/d/Program Files (x86)/Java"
            "/e/Program Files/Java"
            "/e/Program Files (x86)/Java"
        )
        
        for path in "${windows_paths[@]}"; do
            if [ -d "$path" ]; then
                for java_dir in "$path"/*; do
                    if [ -d "$java_dir" ] && [ -f "$java_dir/bin/java.exe" ]; then
                        java_paths+=("$java_dir")
                        print_status "Found Java: $java_dir"
                    fi
                done
            fi
        done
    else
        # Unix-like paths
        local unix_paths=(
            "/usr/lib/jvm"
            "/usr/java"
            "/opt/java"
            "/Library/Java/JavaVirtualMachines"
        )
        
        for path in "${unix_paths[@]}"; do
            if [ -d "$path" ]; then
                for java_dir in "$path"/*; do
                    if [ -d "$java_dir" ] && [ -f "$java_dir/bin/java" ]; then
                        java_paths+=("$java_dir")
                        print_status "Found Java: $java_dir"
                    fi
                done
            fi
        done
    fi
    
    # Check PATH for java
    if command_exists java; then
        local java_path=$(which java)
        local java_home=$(dirname "$(dirname "$java_path")")
        if [ -d "$java_home" ]; then
            java_paths+=("$java_home")
            print_status "Found Java in PATH: $java_home"
        fi
    fi
    
    echo "${java_paths[@]}"
}

# Function to check Java version
check_java_version() {
    local java_home="$1"
    local java_exe="java"
    
    if [ "$(detect_os)" = "windows" ]; then
        java_exe="java.exe"
    fi
    
    if [ -f "$java_home/bin/$java_exe" ]; then
        local version=$("$java_home/bin/$java_exe" -version 2>&1 | head -n 1 | cut -d'"' -f2)
        echo "$version"
    else
        echo "unknown"
    fi
}

# Function to fix JAVA_HOME
fix_java_home() {
    print_status "Fixing JAVA_HOME configuration..."
    
    local java_installations=($(find_java_installations))
    local java_21_found=""
    local java_17_found=""
    local java_11_found=""
    local java_8_found=""
    
    # Check each installation for Java 21
    for java_home in "${java_installations[@]}"; do
        local version=$(check_java_version "$java_home")
        print_status "Java installation: $java_home (version: $version)"
        
        if [[ $version == 21* ]]; then
            java_21_found="$java_home"
            print_success "✅ Found Java 21: $java_home"
        elif [[ $version == 17* ]]; then
            java_17_found="$java_home"
            print_warning "⚠️  Found Java 17: $java_home"
        elif [[ $version == 11* ]]; then
            java_11_found="$java_home"
            print_warning "⚠️  Found Java 11: $java_home"
        elif [[ $version == 1.8* ]] || [[ $version == 8* ]]; then
            java_8_found="$java_home"
            print_warning "⚠️  Found Java 8: $java_home"
        fi
    done
    
    # Recommend the best Java version
    if [ -n "$java_21_found" ]; then
        print_success "🎯 RECOMMENDED: Use Java 21"
        echo "export JAVA_HOME=\"$java_21_found\""
        echo "export PATH=\"\$JAVA_HOME/bin:\$PATH\""
        echo ""
        print_status "Add these lines to your shell profile (.bashrc, .zshrc, etc.)"
    elif [ -n "$java_17_found" ]; then
        print_warning "⚠️  FALLBACK: Use Java 17 (project requires Java 21)"
        echo "export JAVA_HOME=\"$java_17_found\""
        echo "export PATH=\"\$JAVA_HOME/bin:\$PATH\""
        echo ""
        print_status "Add these lines to your shell profile (.bashrc, .zshrc, etc.)"
    else
        print_error "❌ No suitable Java installation found"
        print_status "Please install Java 21 or Java 17"
    fi
}

# Function to test Gradle build
test_gradle_build() {
    print_status "Testing Gradle build..."
    
    # Create log directory
    mkdir -p build/logs
    
    # Create timestamp for log files
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local log_file="build/logs/gradle-test_${timestamp}.log"
    
    print_status "Log will be saved to: $log_file"
    
    # Make gradlew executable
    if [ -f "gradlew" ]; then
        chmod +x gradlew
    fi
    
    # Test Gradle
    if [ -f /.dockerenv ] || grep -q docker /proc/1/cgroup 2>/dev/null; then
        # Inside Docker
        if ./gradlew --version 2>&1 | tee "$log_file"; then
            print_success "✅ Gradle working in Docker"
            return 0
        else
            print_error "❌ Gradle failed in Docker"
            return 1
        fi
    elif command_exists docker; then
        # Use Docker
        print_status "Testing Gradle in Docker container..."
        if docker build -t finance-control-test --target base . 2>&1 | tee "$log_file"; then
            print_success "✅ Docker build successful"
            return 0
        else
            print_error "❌ Docker build failed"
            return 1
        fi
    else
        # Local execution
        if ./gradlew --version 2>&1 | tee "$log_file"; then
            print_success "✅ Gradle working locally"
            return 0
        else
            print_error "❌ Gradle failed locally"
            return 1
        fi
    fi
}

# Function to provide recommendations
provide_recommendations() {
    echo ""
    print_status "🔧 ENVIRONMENT FIXES"
    echo "${'='*50}"
    
    # Fix JAVA_HOME
    fix_java_home
    
    echo ""
    print_status "🧪 TESTING BUILD"
    echo "${'='*50}"
    
    if test_gradle_build; then
        print_success "✅ Build environment is working!"
    else
        print_error "❌ Build environment has issues"
    fi
    
    echo ""
    print_status "🚀 NEXT STEPS"
    echo "${'='*50}"
    echo "1. Set JAVA_HOME as recommended above"
    echo "2. Restart your terminal"
    echo "3. Run: ./scripts/check-environment.sh"
    echo "4. Run: ./scripts/quality-check.sh"
    echo "5. Run: ./scripts/test-fixes.sh"
}

# Main script logic
main() {
    echo "🔧 Fix Environment Script"
    echo "========================"
    echo ""
    
    # Check if we're in the right directory
    if [ ! -f "build.gradle" ]; then
        print_error "build.gradle not found. Please run this script from the project root directory."
        exit 1
    fi
    
    # Run all fixes
    provide_recommendations
}

# Run main function
main "$@" 