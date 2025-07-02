#!/bin/bash

# Docker Run Script
# This script runs all commands inside Docker containers for consistent environments

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

print_header() {
    echo -e "${PURPLE}$1${NC}"
}

print_command() {
    echo -e "${CYAN}$1${NC}"
}

# Function to check if Docker is available
check_docker() {
    if ! command -v docker >/dev/null 2>&1; then
        print_error "Docker is not installed or not in PATH"
        exit 1
    fi
    
    if ! docker info >/dev/null 2>&1; then
        print_error "Docker is not running"
        exit 1
    fi
    
    print_success "✅ Docker is available and running"
}

# Function to check if docker-compose is available
check_docker_compose() {
    if ! command -v docker-compose >/dev/null 2>&1; then
        print_error "docker-compose is not installed or not in PATH"
        exit 1
    fi
    
    print_success "✅ docker-compose is available"
}

# Function to build the base image if needed
ensure_base_image() {
    print_status "Ensuring base Docker image is built..."
    
    if ! docker images | grep -q "finance-control.*base"; then
        print_status "Building base Docker image..."
        docker build -t finance-control:base --target base .
        print_success "✅ Base image built successfully"
    else
        print_success "✅ Base image already exists"
    fi
}

# Function to run a command in Docker
run_in_docker() {
    local command="$1"
    local service_name="${2:-app}"
    local interactive="${3:-false}"
    
    print_status "Running in Docker container: $command"
    
    # Create log directory
    mkdir -p build/logs
    
    # Create timestamp for log files
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local log_file="build/logs/docker-run_${timestamp}.log"
    
    print_status "Log will be saved to: $log_file"
    
    # Build the command
    local docker_cmd="docker run --rm"
    
    # Add interactive flag if needed
    if [ "$interactive" = "true" ]; then
        docker_cmd="$docker_cmd -it"
    fi
    
    # Add volumes and environment
    docker_cmd="$docker_cmd \
        -v \"$PWD\":/app \
        -v \"$PWD/build\":/app/build \
        -v \"$PWD/logs\":/app/logs \
        -v gradle-cache:/root/.gradle \
        -w /app \
        -e GRADLE_OPTS=\"-Dorg.gradle.console=rich -Dorg.gradle.daemon=false\" \
        finance-control:base \
        $command"
    
    # Execute the command
    if eval $docker_cmd 2>&1 | tee "$log_file"; then
        print_success "✅ Command completed successfully"
        return 0
    else
        print_error "❌ Command failed"
        print_status "Check the log file for details: $log_file"
        return 1
    fi
}

# Function to run a script in Docker
run_script_in_docker() {
    local script_name="$1"
    local script_args="${2:-}"
    
    print_status "Running script in Docker: $script_name"
    
    # Make sure the script is executable
    chmod +x "scripts/$script_name"
    
    # Run the script in Docker
    run_in_docker "./scripts/$script_name $script_args"
}

# Function to show usage
show_usage() {
    echo ""
    print_header "🐳 Docker Run Script"
    echo "${'='*60}"
    echo ""
    print_description "This script runs all commands inside Docker containers for"
    print_description "consistent environments across all platforms."
    echo ""
    print_command "Usage: $0 <command> [options]"
    echo ""
    print_section "Available Commands:"
    echo "${'='*60}"
    echo ""
    print_command "Scripts:"
    echo "  check-env          - Run environment check in Docker"
    echo "  fix-env            - Run environment fix in Docker"
    echo "  quality            - Run quality checks in Docker"
    echo "  test               - Run tests in Docker"
    echo "  build              - Run build in Docker"
    echo "  help               - Show help in Docker"
    echo ""
    print_command "Gradle Commands:"
    echo "  gradle <task>      - Run any Gradle task in Docker"
    echo "  build              - Run Gradle build in Docker"
    echo "  test               - Run Gradle tests in Docker"
    echo "  qualityCheck       - Run quality checks in Docker"
    echo "  clean              - Run Gradle clean in Docker"
    echo ""
    print_command "Development:"
    echo "  dev                - Start development container"
    echo "  app                - Start application with database"
    echo "  db                 - Start database only"
    echo "  shell              - Open shell in container"
    echo ""
    print_command "Examples:"
    echo "  $0 check-env"
    echo "  $0 quality"
    echo "  $0 gradle build"
    echo "  $0 gradle test --info"
    echo "  $0 dev"
    echo "  $0 shell"
    echo ""
}

# Function to handle different commands
handle_command() {
    local command="$1"
    shift
    
    case "$command" in
        "check-env")
            run_script_in_docker "check-environment.sh" "$@"
            ;;
        "fix-env")
            run_script_in_docker "fix-environment.sh" "$@"
            ;;
        "quality")
            run_script_in_docker "quality-check.sh" "$@"
            ;;
        "test")
            run_script_in_docker "test-fixes.sh" "$@"
            ;;
        "build")
            run_in_docker "./gradlew build --console=rich"
            ;;
        "gradle")
            run_in_docker "./gradlew $* --console=rich"
            ;;
        "dev")
            print_status "Starting development container..."
            docker-compose --profile dev up -d dev
            print_success "✅ Development container started"
            print_status "You can now run:"
            echo "  docker-compose exec dev ./gradlew build"
            echo "  docker-compose exec dev ./scripts/quality-check.sh"
            echo "  docker-compose exec dev bash"
            ;;
        "app")
            print_status "Starting application with database..."
            docker-compose up -d
            print_success "✅ Application started"
            print_status "Access at: http://localhost:8080"
            ;;
        "db")
            print_status "Starting database..."
            docker-compose up -d db
            print_success "✅ Database started"
            ;;
        "shell")
            print_status "Opening shell in container..."
            run_in_docker "bash" "app" "true"
            ;;
        "help"|"-h"|"--help")
            show_usage
            ;;
        *)
            print_error "Unknown command: $command"
            show_usage
            exit 1
            ;;
    esac
}

# Function to create a wrapper script
create_wrapper_script() {
    local script_name="$1"
    local wrapper_name="$2"
    
    cat > "scripts/$wrapper_name" << EOF
#!/bin/bash

# Wrapper script for $script_name
# This script runs $script_name inside Docker

set -e

# Get the directory of this script
SCRIPT_DIR="\$(cd "\$(dirname "\${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="\$(dirname "\$SCRIPT_DIR")"

# Change to project directory
cd "\$PROJECT_DIR"

# Run the docker-run script
"\$SCRIPT_DIR/docker-run.sh" $script_name "\$@"
EOF
    
    chmod +x "scripts/$wrapper_name"
    print_success "✅ Created wrapper script: scripts/$wrapper_name"
}

# Function to create all wrapper scripts
create_wrapper_scripts() {
    print_status "Creating wrapper scripts for all commands..."
    
    create_wrapper_script "check-env" "docker-check-env.sh"
    create_wrapper_script "fix-env" "docker-fix-env.sh"
    create_wrapper_script "quality" "docker-quality.sh"
    create_wrapper_script "test" "docker-test.sh"
    create_wrapper_script "build" "docker-build.sh"
    create_wrapper_script "gradle build" "docker-gradle-build.sh"
    create_wrapper_script "gradle test" "docker-gradle-test.sh"
    create_wrapper_script "gradle qualityCheck" "docker-quality-check.sh"
    
    print_success "✅ All wrapper scripts created"
    echo ""
    print_status "You can now use:"
    echo "  ./scripts/docker-check-env.sh"
    echo "  ./scripts/docker-quality.sh"
    echo "  ./scripts/docker-build.sh"
    echo "  ./scripts/docker-gradle-build.sh"
}

# Main script logic
main() {
    # Check if we're in the right directory
    if [ ! -f "docker-compose.yml" ]; then
        print_error "docker-compose.yml not found. Please run this script from the project root directory."
        exit 1
    fi
    
    # Check Docker and docker-compose
    check_docker
    check_docker_compose
    
    # Ensure base image is built
    ensure_base_image
    
    # Handle special commands
    if [ "$1" = "setup" ]; then
        create_wrapper_scripts
        exit 0
    fi
    
    # Handle commands
    if [ $# -eq 0 ]; then
        show_usage
        exit 1
    fi
    
    handle_command "$@"
}

# Run main function
main "$@" 