#!/bin/bash

# Docker Compose Run Script
# This script uses Docker Compose services for running Gradle commands

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

show_usage() {
    echo "Docker Compose Run Script"
    echo "========================"
    echo ""
    echo "Usage: $0 <service> [options]"
    echo ""
    echo "Services:"
    echo "  dev                    # Start development container (interactive)"
    echo "  quality                # Run quality checks"
    echo "  test                   # Run tests"
    echo "  build                  # Run build"
    echo "  app                    # Start application"
    echo "  db                     # Start database only"
    echo ""
    echo "Examples:"
    echo "  $0 dev                 # Start dev container"
    echo "  $0 quality             # Run quality checks"
    echo "  $0 test                # Run tests"
    echo "  $0 build               # Run build"
    echo "  $0 app                 # Start full application"
    echo ""
    echo "Interactive Commands (after starting dev service):"
    echo "  docker-compose exec dev ./gradlew build"
    echo "  docker-compose exec dev ./gradlew qualityCheck"
    echo "  docker-compose exec dev ./scripts/quality-check.sh"
}

check_docker_compose() {
    if ! command -v docker-compose >/dev/null 2>&1; then
        print_error "docker-compose not found"
        exit 1
    fi
}

check_project() {
    if [ ! -f "docker-compose.yml" ]; then
        print_error "docker-compose.yml not found. Please run from project root."
        exit 1
    fi
}

run_service() {
    local service=$1
    local profile=""
    
    case $service in
        dev)
            profile="--profile dev"
            print_status "Starting development container..."
            docker-compose $profile up -d dev
            print_success "Development container started!"
            print_status "You can now run:"
            echo "  docker-compose exec dev ./gradlew build"
            echo "  docker-compose exec dev ./gradlew qualityCheck"
            echo "  docker-compose exec dev ./scripts/quality-check.sh"
            ;;
        quality)
            profile="--profile quality"
            print_status "Running quality checks..."
            docker-compose $profile up quality --abort-on-container-exit
            ;;
        test)
            profile="--profile test"
            print_status "Running tests..."
            docker-compose $profile up test --abort-on-container-exit
            ;;
        build)
            profile="--profile build"
            print_status "Running build..."
            docker-compose $profile up build --abort-on-container-exit
            ;;
        app)
            print_status "Starting application..."
            docker-compose up -d
            print_success "Application started!"
            print_status "Access at: http://localhost:8080"
            ;;
        db)
            print_status "Starting database..."
            docker-compose up -d db
            print_success "Database started!"
            ;;
        *)
            print_error "Unknown service: $service"
            show_usage
            exit 1
            ;;
    esac
}

main() {
    if [ $# -eq 0 ]; then
        show_usage
        exit 1
    fi
    
    if [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
        show_usage
        exit 0
    fi
    
    check_docker_compose
    check_project
    
    run_service "$1"
}

main "$@" 