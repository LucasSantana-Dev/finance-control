#!/bin/bash

# Finance Control - Services Module
# Docker service management functions

# Function to start services with proper path resolution and retry limit
start_services() {
    local max_retries=2
    local retry_count=0
    
    echo -e "${BLUE}[INFO]${NC} Starting services..."
    
    # Set the correct path for Docker volume mounts on Windows
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
        export COMPOSE_CONVERT_WINDOWS_PATHS=1
        echo -e "${YELLOW}[INFO]${NC} Windows detected, enabling path conversion..."
    fi
    
    while [ $retry_count -lt $max_retries ]; do
        if docker-compose up -d; then
            echo -e "${GREEN}[SUCCESS]${NC} Services started successfully!"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                echo -e "${YELLOW}[WARNING]${NC} Failed to start services, retrying... (attempt $retry_count/$max_retries)"
                sleep 3
            else
                echo -e "${RED}[ERROR]${NC} Failed to start services after $max_retries attempts"
                return 1
            fi
        fi
    done
}

# Function to stop services with retry limit
stop_services() {
    local max_retries=2
    local retry_count=0
    
    echo -e "${BLUE}[INFO]${NC} Stopping services..."
    
    while [ $retry_count -lt $max_retries ]; do
        if docker-compose down; then
            echo -e "${GREEN}[SUCCESS]${NC} Services stopped successfully!"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                echo -e "${YELLOW}[WARNING]${NC} Failed to stop services, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                echo -e "${RED}[ERROR]${NC} Failed to stop services after $max_retries attempts"
                return 1
            fi
        fi
    done
}

# Function to clean everything
clean_services() {
    echo -e "${BLUE}[INFO]${NC} Cleaning all containers, networks, and volumes..."
    docker-compose down -v
    docker system prune -f
    echo -e "${GREEN}[SUCCESS]${NC} Cleanup completed!"
}

# Function to clean up with --no-test support
clean_up() {
    local skip_tests=false
    
    # Check for --no-test parameter
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    
    if [ "$skip_tests" = true ]; then
        print_status "Cleaning up containers and volumes (skipping test cleanup)..."
        SKIP_TESTS=true docker-compose down -v --remove-orphans
        docker system prune -f
        print_success "Cleanup completed (test cleanup skipped)!"
    else
        print_status "Cleaning up containers and volumes..."
        docker-compose down -v --remove-orphans
        docker system prune -f
        print_success "Cleanup completed!"
    fi
}

# Function to show logs with retry limit
show_logs() {
    local max_retries=2
    local retry_count=0
    
    echo -e "${BLUE}[INFO]${NC} Showing logs..."
    
    while [ $retry_count -lt $max_retries ]; do
        if docker-compose logs -f; then
            # If logs command succeeds, exit normally
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                echo -e "${YELLOW}[WARNING]${NC} Logs command failed, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                echo -e "${RED}[ERROR]${NC} Failed to show logs after $max_retries attempts"
                echo -e "${BLUE}[INFO]${NC} Checking if services are running..."
                if ! docker-compose ps | grep -q "Up"; then
                    echo -e "${YELLOW}[WARNING]${NC} No services are running. Start services first with: $0 start"
                fi
                return 1
            fi
        fi
    done
}

# Function to start application
start_app() {
    local skip_tests=false
    
    # Check for --no-test parameter
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    
    if [ "$skip_tests" = true ]; then
        print_status "Starting application (skipping tests)..."
        SKIP_TESTS=true docker-compose up -d
        print_success "Application started (tests skipped)!"
    else
        print_status "Starting application..."
        docker-compose up -d
        print_success "Application started!"
    fi
    
    print_status "Access at: http://localhost:8080"
    print_status "Database at: localhost:5432"
} 