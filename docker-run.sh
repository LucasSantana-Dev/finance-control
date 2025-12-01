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

# Function to stop containers using specific ports
stop_port_conflicts() {
    local ports=("54322" "54321" "54323" "54324" "54325" "54326" "54327" "8000" "8443")
    local found_conflict=false

    # Check for containers using required ports
    for port in "${ports[@]}"; do
        local container=$(docker ps -a --filter "publish=${port}" --format "{{.Names}}" | head -1)
        if [ -n "$container" ]; then
            if [ "$found_conflict" = false ]; then
                print_status "Checking for port conflicts..."
                found_conflict=true
            fi
            print_warning "Found container '$container' using port $port, stopping it..."
            docker stop "$container" > /dev/null 2>&1 || true
            docker rm "$container" > /dev/null 2>&1 || true
        fi
    done

    # Also check for old Supabase containers with different naming convention
    local supabase_containers=$(docker ps -a --filter "name=supabase_" --format "{{.Names}}" 2>/dev/null | head -5)
    if [ -n "$supabase_containers" ]; then
        if [ "$found_conflict" = false ]; then
            print_status "Checking for port conflicts..."
            found_conflict=true
        fi
        print_warning "Found old Supabase containers, stopping them..."
        echo "$supabase_containers" | while read -r container; do
            if [ -n "$container" ]; then
                docker stop "$container" > /dev/null 2>&1 || true
                docker rm "$container" > /dev/null 2>&1 || true
            fi
        done
    fi

    if [ "$found_conflict" = true ]; then
        print_success "Port conflicts resolved"
    fi
}

# Function to stop existing containers
stop_containers() {
    print_status "Stopping existing containers..."
    stop_port_conflicts

    # Stop and remove containers from all profiles
    docker compose --env-file docker.env down --remove-orphans || true
    docker compose --env-file docker.env --profile supabase down --remove-orphans || true

    # Remove any containers in Created/Exited state that might be blocking ports
    local created_containers=$(docker ps -a --filter "name=finance-control" --filter "status=created" --format "{{.ID}}")
    if [ -n "$created_containers" ]; then
        print_status "Removing containers in Created state..."
        echo "$created_containers" | xargs -r docker rm -f 2>/dev/null || true
    fi

    print_success "Existing containers stopped"
}

# Function to check if Supabase local is enabled
is_supabase_enabled() {
    # Check environment variable first (allows override via command line)
    if [ "${SUPABASE_LOCAL_ENABLED}" = "true" ]; then
        return 0
    fi
    # Then check docker.env file
    if [ -f docker.env ]; then
        grep -q "^SUPABASE_LOCAL_ENABLED=true" docker.env
    else
        return 1
    fi
}

# Function to build and start the application
start_application() {
    local services

    if is_supabase_enabled; then
        print_status "Supabase local is enabled, starting Supabase services..."
        # When using local Supabase, we still need the local db service for SonarQube
        services="app db redis"

        # Export local Supabase database connection variables to override docker.env values
        # These will be picked up by docker compose and override env_file values
        # Note: supabase-db is the service name, port 5432 is the internal container port
        export SUPABASE_DATABASE_HOST="supabase-db"
        export SUPABASE_DATABASE_PORT="5432"
        export SUPABASE_DATABASE_NAME="${SUPABASE_LOCAL_DB_NAME:-postgres}"
        export SUPABASE_DATABASE_USERNAME="${SUPABASE_LOCAL_DB_USER:-postgres}"
        export SUPABASE_DATABASE_PASSWORD="${SUPABASE_LOCAL_DB_PASSWORD:-postgres}"
        export SUPABASE_DATABASE_SSL_ENABLED="false"
        export SUPABASE_DATABASE_SSL_MODE="disable"

        print_status "Using local Supabase database: ${SUPABASE_DATABASE_HOST}:${SUPABASE_DATABASE_PORT}/${SUPABASE_DATABASE_NAME}"
        print_status "Building and starting the application..."
        docker compose --env-file docker.env --profile supabase up --build -d $services
    else
        print_status "Using remote Supabase (SUPABASE_LOCAL_ENABLED=false)"
        # When using remote Supabase, we don't need the local db service
        # Only start app and redis (db is only needed for SonarQube, which uses a separate profile)
        services="app redis"

        # Unset any previously exported local Supabase variables to ensure remote values from docker.env are used
        unset SUPABASE_DATABASE_HOST SUPABASE_DATABASE_PORT SUPABASE_DATABASE_NAME
        unset SUPABASE_DATABASE_USERNAME SUPABASE_DATABASE_PASSWORD
        unset SUPABASE_DATABASE_SSL_ENABLED SUPABASE_DATABASE_SSL_MODE
        print_status "Building and starting the application..."
        docker compose --env-file docker.env up --build -d $services
    fi

    print_success "Application started"
}

# Function to wait for the application to be ready
wait_for_application() {
    print_status "Waiting for application to be ready..."
    # Increased timeout to allow for long-running migrations (e.g., CREATE INDEX CONCURRENTLY)
    # 180 attempts * 5 seconds = 15 minutes total timeout
    local max_attempts=180
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
            print_success "Application is ready!"
            return 0
        fi

        # Show progress every 12 attempts (1 minute) to avoid spam
        if [ $((attempt % 12)) -eq 0 ] || [ $attempt -le 10 ]; then
            print_status "Attempt $attempt/$max_attempts - Application not ready yet, waiting 5 seconds..."
            if [ $attempt -gt 60 ]; then
                print_warning "Application is taking longer than expected. This may be due to database migrations running."
            fi
        fi
        sleep 5
        attempt=$((attempt + 1))
    done

    print_error "Application failed to start within expected time (15 minutes)"
    print_warning "Check application logs: docker logs finance-control-app-1"
    return 1
}

# Function to show application status
show_status() {
    print_status "Application Status:"
    echo "=================="
    echo "Health Check: http://localhost:8080/actuator/health"
    echo "API Docs: http://localhost:8080/swagger-ui.html"
    echo "Database: localhost:5432"

    if is_supabase_enabled; then
        echo ""
        print_status "Supabase Local Services:"
        echo "  API: http://localhost:54321"
        echo "  Studio: http://localhost:54323"
        echo "  Inbucket (Email): http://localhost:54324"
        echo "  Analytics: http://localhost:54327"
        echo "  Database: localhost:54322"
    fi

    echo ""
    print_status "Container Status:"
    if is_supabase_enabled; then
        docker compose --env-file docker.env --profile supabase ps
    else
        docker compose --env-file docker.env ps
    fi
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

# Function to start Supabase services
start_supabase() {
    print_status "Starting Supabase services..."
    docker compose --env-file docker.env --profile supabase up -d supabase-db supabase-rest supabase-auth supabase-storage supabase-realtime supabase-studio supabase-meta supabase-inbucket supabase-analytics supabase-kong
    print_success "Supabase services started"
    print_status "Supabase Studio: http://localhost:54323"
    print_status "Supabase API: http://localhost:54321"
    print_status "Inbucket (Email): http://localhost:54324"
}

# Function to stop Supabase services
stop_supabase() {
    print_status "Stopping Supabase services..."
    docker compose --env-file docker.env --profile supabase stop supabase-db supabase-rest supabase-auth supabase-storage supabase-realtime supabase-studio supabase-meta supabase-inbucket supabase-analytics supabase-kong
    print_success "Supabase services stopped"
}

# Function to show Supabase logs
show_supabase_logs() {
    print_status "Showing Supabase logs..."
    docker compose --env-file docker.env --profile supabase logs -f
}

# Function to show help
show_help() {
    echo "Finance Control - Docker Run Script"
    echo "===================================="
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  start          Start the application (default)"
    echo "  stop           Stop all containers"
    echo "  restart        Restart the application"
    echo "  status         Show application status"
    echo "  logs           Show application logs"
    echo "  test           Test the auth endpoint"
    echo "  tests          Run all tests"
    echo "  quality        Run quality checks"
    echo "  shell          Open a shell in the app container"
    echo "  supabase-start Start Supabase services only"
    echo "  supabase-stop  Stop Supabase services only"
    echo "  supabase-logs  Show Supabase services logs"
    echo "  help           Show this help message"
    echo ""
    echo "Note: Set SUPABASE_LOCAL_ENABLED=true in docker.env to use local Supabase"
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
    "supabase-start")
        check_docker
        start_supabase
        ;;
    "supabase-stop")
        check_docker
        stop_supabase
        ;;
    "supabase-logs")
        show_supabase_logs
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
