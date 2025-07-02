#!/bin/bash

# Docker Manager Script
# This script manages all Docker operations for the Finance Control project

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

# Function to load environment variables
load_env() {
    if [ -f ".env" ]; then
        print_status "Loading environment from .env"
        # Source the environment file
        set -a
        source .env
        set +a
    else
        print_warning ".env not found, using default values"
    fi
}

# Function to build Docker images
build_images() {
    local target="${1:-base}"
    print_status "Building Docker image with target: $target"
    
    if docker build -t finance-control:$target --target $target .; then
        print_success "✅ Docker image built successfully: finance-control:$target"
    else
        print_error "❌ Docker build failed"
        exit 1
    fi
}

# Function to run a service
run_service() {
    local service="$1"
    local profile="${2:-}"
    local detached="${3:-true}"
    
    print_status "Running service: $service"
    
    local compose_cmd="docker-compose"
    if [ -n "$profile" ]; then
        compose_cmd="$compose_cmd --profile $profile"
    fi
    
    if [ "$detached" = "true" ]; then
        compose_cmd="$compose_cmd up -d"
    else
        compose_cmd="$compose_cmd up"
    fi
    
    if [ -n "$service" ]; then
        compose_cmd="$compose_cmd $service"
    fi
    
    print_command "Executing: $compose_cmd"
    
    if eval $compose_cmd; then
        print_success "✅ Service started successfully"
    else
        print_error "❌ Service failed to start"
        exit 1
    fi
}

# Function to stop services
stop_services() {
    local service="${1:-}"
    print_status "Stopping services${service:+: $service}"
    
    local compose_cmd="docker-compose down"
    if [ -n "$service" ]; then
        compose_cmd="docker-compose stop $service"
    fi
    
    if eval $compose_cmd; then
        print_success "✅ Services stopped successfully"
    else
        print_error "❌ Failed to stop services"
        exit 1
    fi
}

# Function to clean up Docker resources
cleanup() {
    print_status "Cleaning up Docker resources..."
    
    # Stop all services
    docker-compose down
    
    # Remove unused containers
    docker container prune -f
    
    # Remove unused images
    docker image prune -f
    
    # Remove unused volumes
    docker volume prune -f
    
    # Remove unused networks
    docker network prune -f
    
    print_success "✅ Cleanup completed"
}

# Function to show logs
show_logs() {
    local service="${1:-app}"
    local follow="${2:-false}"
    
    print_status "Showing logs for service: $service"
    
    local log_cmd="docker-compose logs"
    if [ "$follow" = "true" ]; then
        log_cmd="$log_cmd -f"
    fi
    log_cmd="$log_cmd $service"
    
    eval $log_cmd
}

# Function to execute command in container
exec_in_container() {
    local service="${1:-dev}"
    local command="${2:-bash}"
    
    print_status "Executing command in $service container: $command"
    
    if docker-compose exec $service $command; then
        print_success "✅ Command executed successfully"
    else
        print_error "❌ Command failed"
        exit 1
    fi
}

# Function to run script in container
run_script_in_container() {
    local script="$1"
    local service="${2:-dev}"
    
    print_status "Running script in $service container: $script"
    
    # Make script executable
    chmod +x "scripts/$script"
    
    if docker-compose exec $service "./scripts/$script"; then
        print_success "✅ Script executed successfully"
    else
        print_error "❌ Script failed"
        exit 1
    fi
}

# Function to show status
show_status() {
    print_status "Docker Compose Status:"
    docker-compose ps
    
    echo ""
    print_status "Docker Images:"
    docker images | grep finance-control || echo "No finance-control images found"
    
    echo ""
    print_status "Docker Volumes:"
    docker volume ls | grep finance-control || echo "No finance-control volumes found"
}

# Function to show usage
show_usage() {
    echo ""
    print_header "🐳 Docker Manager Script"
    echo "${'='*60}"
    echo ""
    print_description "This script manages all Docker operations for the Finance Control project."
    echo ""
    print_command "Usage: $0 <command> [options]"
    echo ""
    print_section "Available Commands:"
    echo "${'='*60}"
    echo ""
    print_command "Build Commands:"
    echo "  build [target]        - Build Docker image (default: base)"
    echo "  rebuild [target]      - Force rebuild Docker image"
    echo ""
    print_command "Service Management:"
    echo "  start [service]       - Start services (default: all)"
    echo "  stop [service]        - Stop services (default: all)"
    echo "  restart [service]     - Restart services"
    echo "  status                - Show service status"
    echo ""
    print_command "Development:"
    echo "  dev                   - Start development environment"
    echo "  shell                 - Open shell in dev container"
    echo "  app                   - Start application with database"
    echo "  db                    - Start database only"
    echo ""
    print_command "Scripts:"
    echo "  check-env             - Run environment check"
    echo "  fix-env               - Run environment fix"
    echo "  quality               - Run quality checks"
    echo "  test                  - Run tests"
    echo "  build-app             - Run build"
    echo ""
    print_command "Gradle Commands:"
    echo "  gradle <task>         - Run Gradle task"
    echo "  build                 - Run Gradle build"
    echo "  test                  - Run Gradle tests"
    echo "  qualityCheck          - Run quality checks"
    echo ""
    print_command "Maintenance:"
    echo "  logs [service]        - Show logs (default: app)"
    echo "  logs-follow [service] - Follow logs"
    echo "  cleanup               - Clean up Docker resources"
    echo "  help                  - Show this help"
    echo ""
    print_command "Examples:"
    echo "  $0 build"
    echo "  $0 dev"
    echo "  $0 shell"
    echo "  $0 app"
    echo "  $0 quality"
    echo "  $0 gradle build"
    echo "  $0 logs app"
    echo ""
}

# Function to handle different commands
handle_command() {
    local command="$1"
    shift
    
    case "$command" in
        "build")
            build_images "$1"
            ;;
        "rebuild")
            docker-compose build --no-cache "$1"
            print_success "✅ Image rebuilt successfully"
            ;;
        "start")
            run_service "$1" "" "true"
            ;;
        "stop")
            stop_services "$1"
            ;;
        "restart")
            stop_services "$1"
            run_service "$1" "" "true"
            ;;
        "status")
            show_status
            ;;
        "dev")
            run_service "dev" "dev" "true"
            print_status "Development environment started"
            print_status "You can now run:"
            echo "  $0 shell"
            echo "  $0 gradle build"
            echo "  $0 quality"
            ;;
        "shell")
            exec_in_container "dev" "bash"
            ;;
        "app")
            run_service "" "" "true"
            print_status "Application started"
            print_status "Access at: http://localhost:${APPLICATION_PORT:-8080}"
            ;;
        "db")
            run_service "db" "" "true"
            print_status "Database started"
            ;;
        "check-env")
            run_script_in_container "check-environment.sh" "dev"
            ;;
        "fix-env")
            run_script_in_container "fix-environment.sh" "dev"
            ;;
        "quality")
            run_service "quality" "quality" "false"
            ;;
        "test")
            run_service "test" "test" "false"
            ;;
        "build-app")
            run_service "build" "build" "false"
            ;;
        "gradle")
            exec_in_container "dev" "./gradlew $* --console=rich"
            ;;
        "logs")
            show_logs "$1" "false"
            ;;
        "logs-follow")
            show_logs "$1" "true"
            ;;
        "cleanup")
            cleanup
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
    
    # Load environment variables
    load_env
    
    # Handle commands
    if [ $# -eq 0 ]; then
        show_usage
        exit 1
    fi
    
    handle_command "$@"
}

# Run main function
main "$@" 