#!/bin/bash

# SonarQube management functions for Finance Control

start_sonarqube() {
    local max_retries=2
    local retry_count=0
    print_status "🚀 Starting SonarQube service..."
    while [ $retry_count -lt $max_retries ]; do
        if docker compose --profile sonarqube up -d sonarqube; then
            print_success "SonarQube service started!"
            print_status "Access SonarQube at: http://localhost:9000"
            print_status "Default credentials: ${SONAR_DB_USER}/${SONAR_DB_PASSWORD}"
            print_status "Wait a few minutes for SonarQube to fully start..."
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "Failed to start SonarQube, retrying... (attempt $retry_count/$max_retries)"
                sleep 3
            else
                print_error "Failed to start SonarQube after $max_retries attempts"
                return 1
            fi
        fi
    done
}

stop_sonarqube() {
    local max_retries=2
    local retry_count=0
    print_status "🛑 Stopping SonarQube service..."
    while [ $retry_count -lt $max_retries ]; do
        if docker compose --profile sonarqube down sonarqube; then
            print_success "SonarQube service stopped!"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "Failed to stop SonarQube, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                print_error "Failed to stop SonarQube after $max_retries attempts"
                return 1
            fi
        fi
    done
}

show_sonarqube_logs() {
    local max_retries=2
    local retry_count=0
    print_status "📋 Showing SonarQube logs..."
    while [ $retry_count -lt $max_retries ]; do
        if docker compose --profile sonarqube logs -f sonarqube; then
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "Failed to show SonarQube logs, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                print_error "Failed to show SonarQube logs after $max_retries attempts"
                print_status "Checking if SonarQube is running..."
                if ! docker compose --profile sonarqube ps sonarqube | grep -q "Up"; then
                    print_warning "SonarQube is not running. Start it first with: $0 sonarqube-start"
                fi
                return 1
            fi
        fi
    done
}

run_sonarqube_scan() {
    local skip_tests=false
    local sonar_timeout=900  # 15 minutes timeout for SonarQube analysis
    local build_timeout=900  # 15 minutes timeout for build
    local max_retries=2
    local retry_count=0
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    print_status "🔍 Running SonarQube analysis..."
    if ! docker compose --profile sonarqube ps sonarqube | grep -q "Up"; then
        print_warning "SonarQube is not running. Starting it first..."
        start_sonarqube
        print_status "Waiting for SonarQube to be ready..."
        sleep 30
    fi
    if [ ! -f "build.gradle" ]; then
        print_error "Error: This script must be run from the project root directory"
        exit 1
    fi
    if ! command -v java >/dev/null 2>&1; then
        print_error "Java not found. Please install Java 21 or use Docker commands."
        exit 1
    fi
    if ! command -v ./gradlew >/dev/null 2>&1; then
        print_error "Gradle wrapper not found. Please use Docker commands."
        exit 1
    fi
    if [ "$skip_tests" = true ]; then
        print_status "🔨 Building project for SonarQube analysis (skipping tests)..."
        print_status "⏱️  Build timeout set to ${build_timeout}s (15 minutes)"
        if timeout $build_timeout ./gradlew clean build -x test; then
            print_success "✅ Build completed successfully (tests skipped)"
        else
            local exit_code=$?
            if [ $exit_code -eq 124 ]; then
                print_error "❌ Build timed out after ${build_timeout}s (15 minutes)"
                print_status "💡 Try building with --no-test flag: $0 sonarqube-scan --no-test"
            else
                print_error "❌ Build failed with exit code $exit_code"
            fi
            exit 1
        fi
    else
        print_status "🔨 Building project for SonarQube analysis..."
        print_status "⏱️  Build timeout set to ${build_timeout}s (15 minutes)"
        if timeout $build_timeout ./gradlew clean build; then
            print_success "✅ Build completed successfully"
        else
            local exit_code=$?
            if [ $exit_code -eq 124 ]; then
                print_error "❌ Build timed out after ${build_timeout}s (15 minutes)"
                print_status "💡 Try building with --no-test flag: $0 sonarqube-scan --no-test"
            else
                print_error "❌ Build failed with exit code $exit_code"
            fi
            exit 1
        fi
    fi
    print_status "🔍 Running SonarQube analysis..."
    print_status "⏱️  SonarQube analysis timeout set to ${sonar_timeout}s (15 minutes)"
    while [ $retry_count -lt $max_retries ]; do
        if timeout $sonar_timeout ./gradlew sonarqube; then
            print_success "✅ SonarQube analysis completed successfully"
            print_status "📊 View results at: http://localhost:9000"
            return 0
        else
            local exit_code=$?
            if [ $exit_code -eq 124 ]; then
                print_error "❌ SonarQube analysis timed out after ${sonar_timeout}s (15 minutes)"
                print_status "💡 Try running with --no-test flag: $0 sonarqube-scan --no-test"
            else
                print_error "❌ SonarQube analysis failed with exit code $exit_code"
            fi
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "SonarQube analysis failed, retrying... (attempt $retry_count/$max_retries)"
                sleep 5
            else
                print_error "SonarQube analysis failed after $max_retries attempts"
                print_status "💡 Troubleshooting tips:"
                print_status "   - Check SonarQube is running: $0 sonarqube-logs"
                print_status "   - Restart SonarQube: $0 sonarqube-stop && $0 sonarqube-start"
                print_status "   - Try with --no-test: $0 sonarqube-scan --no-test"
                return 1
            fi
        fi
    done
}
