#!/bin/bash

# Development shell and environment functions for Finance Control

start_dev() {
    local max_retries=2
    local retry_count=0
    print_status "Starting development shell..."
    while [ $retry_count -lt $max_retries ]; do
        if docker compose --profile dev up -d dev; then
            print_success "Development container started!"
            print_status "Connect with: docker compose exec dev bash"
            print_status "Or run: $0 dev"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "Failed to start development container, retrying... (attempt $retry_count/$max_retries)"
                sleep 3
            else
                print_error "Failed to start development container after $max_retries attempts"
                return 1
            fi
        fi
    done
}

open_dev_shell() {
    local max_retries=2
    local retry_count=0
    print_status "Opening development shell..."
    while [ $retry_count -lt $max_retries ]; do
        if docker compose run --rm dev bash; then
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "Failed to open development shell, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                print_error "Failed to open development shell after $max_retries attempts"
                print_status "Make sure the development container is properly configured"
                return 1
            fi
        fi
    done
}

check_environment() {
    local max_retries=2
    local retry_count=0
    print_status "Checking environment..."
    while [ $retry_count -lt $max_retries ]; do
        if docker compose run --rm check-env; then
            print_success "Environment check completed!"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "Environment check failed, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                print_error "Environment check failed after $max_retries attempts"
                return 1
            fi
        fi
    done
}
