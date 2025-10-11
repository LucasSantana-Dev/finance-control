#!/bin/bash

# Finance Control - Docker Run Script
# ===================================

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

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker Desktop and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Function to stop existing containers
stop_containers() {
    print_status "Stopping existing containers..."
    docker compose --env-file docker.env down --remove-orphans || true
    print_success "Existing containers stopped"
}

# Function to build and start the application
start_application() {
    print_status "Building and starting the application..."
    docker compose --env-file docker.env up --build -d app db
    print_success "Application started"
}

# Function to wait for the application to be ready
wait_for_application() {
    print_status "Waiting for application to be ready..."
    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
            print_success "Application is ready!"
            return 0
        fi

        print_status "Attempt $attempt/$max_attempts - Application not ready yet, waiting 5 seconds..."
        sleep 5
        attempt=$((attempt + 1))
    done

    print_error "Application failed to start within expected time"
    return 1
}

# Function to show application status
show_status() {
    print_status "Application Status:"
    echo "=================="
    echo "Health Check: http://localhost:8080/actuator/health"
    echo "API Docs: http://localhost:8080/swagger-ui.html"
    echo "Database: localhost:5432"
    echo ""
    print_status "Container Status:"
    docker compose --env-file docker.env ps
}

# Function to show logs
show_logs() {
    print_status "Showing application logs..."
    docker compose --env-file docker.env logs -f app
}

# Function to test the auth endpoint
test_auth() {
    print_status "Testing authentication endpoint..."

    local response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -X POST http://localhost:8080/auth/register \
        -H "Content-Type: application/json" \
        -d '{
            "email": "test.user@example.com",
            "password": "SecurePassword123!"
        }' 2>/dev/null)

    local http_status=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    local body=$(echo "$response" | sed '/HTTP_STATUS:/d')

    if [ "$http_status" = "200" ] || [ "$http_status" = "201" ]; then
        print_success "Auth endpoint is working! Status: $http_status"
        echo "Response: $body"
    else
        print_warning "Auth endpoint returned status: $http_status"
        echo "Response: $body"
    fi
}

# Function to run tests
run_tests() {
    print_status "Running tests in Docker..."
    docker compose --env-file docker.env run --rm test
}

# Function to run quality checks
run_quality() {
    print_status "Running quality checks in Docker..."
    docker compose --env-file docker.env run --rm quality
}

# Function to show help
show_help() {
    echo "Finance Control - Docker Run Script"
    echo "===================================="
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  start     Start the application (default)"
    echo "  stop      Stop all containers"
    echo "  restart   Restart the application"
    echo "  status    Show application status"
    echo "  logs      Show application logs"
    echo "  test      Test the auth endpoint"
    echo "  tests     Run all tests"
    echo "  quality   Run quality checks"
    echo "  shell     Open a shell in the app container"
    echo "  help      Show this help message"
    echo ""
}

# Main script logic
case "${1:-start}" in
    "start")
        check_docker
        stop_containers
        start_application
        wait_for_application
        show_status
        test_auth
        ;;
    "stop")
        check_docker
        stop_containers
        print_success "Application stopped"
        ;;
    "restart")
        check_docker
        stop_containers
        start_application
        wait_for_application
        show_status
        test_auth
        ;;
    "status")
        show_status
        ;;
    "logs")
        show_logs
        ;;
    "test")
        test_auth
        ;;
    "tests")
        check_docker
        run_tests
        ;;
    "quality")
        check_docker
        run_quality
        ;;
    "shell")
        print_status "Opening shell in app container..."
        docker compose --env-file docker.env exec app bash
        ;;
    "help")
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
