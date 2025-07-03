#!/bin/bash

# Build, test, and quality functions for Finance Control

run_build() {
    local skip_tests=false
    local build_timeout=900  # 15 minutes timeout
    local max_retries=2
    local retry_count=0
    
    # Check for --no-test parameter
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    
    while [ $retry_count -lt $max_retries ]; do
        if [ "$skip_tests" = true ]; then
            print_status "Building application (skipping tests)..."
            print_status "⏱️  Build timeout set to ${build_timeout}s (15 minutes)"
            if timeout $build_timeout SKIP_TESTS=true docker-compose --profile build up build --abort-on-container-exit; then
                print_success "Application built successfully (tests skipped)!"
                return 0
            else
                local exit_code=$?
                if [ $exit_code -eq 124 ]; then
                    print_error "❌ Build timed out after ${build_timeout}s (15 minutes)"
                    print_status "💡 Try building with --no-test flag to skip tests: $0 build --no-test"
                else
                    print_error "❌ Build failed with exit code $exit_code"
                fi
            fi
        else
            print_status "Building application..."
            print_status "⏱️  Build timeout set to ${build_timeout}s (15 minutes)"
            if timeout $build_timeout docker-compose --profile build up build --abort-on-container-exit; then
                print_success "Application built successfully!"
                return 0
            else
                local exit_code=$?
                if [ $exit_code -eq 124 ]; then
                    print_error "❌ Build timed out after ${build_timeout}s (15 minutes)"
                    print_status "💡 Try building with --no-test flag to skip tests: $0 build --no-test"
                else
                    print_error "❌ Build failed with exit code $exit_code"
                fi
            fi
        fi
        retry_count=$((retry_count + 1))
        if [ $retry_count -lt $max_retries ]; then
            print_warning "Build failed, retrying... (attempt $retry_count/$max_retries)"
            sleep 5
        else
            print_error "Build failed after $max_retries attempts"
            print_status "💡 Troubleshooting tips:"
            print_status "   - Check Docker is running: docker info"
            print_status "   - Clean and rebuild: $0 clean && $0 build"
            print_status "   - Try with --no-test: $0 build --no-test"
            return 1
        fi
    done
}

run_tests() {
    local skip_tests=false
    local test_timeout=600  # 10 minutes timeout
    local max_retries=2
    local retry_count=0
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    while [ $retry_count -lt $max_retries ]; do
        if [ "$skip_tests" = true ]; then
            print_status "Running tests (skipping compilation)..."
            print_status "⏱️  Test timeout set to ${test_timeout}s (10 minutes)"
            if timeout $test_timeout SKIP_TESTS=true docker-compose --profile test up test --abort-on-container-exit; then
                print_success "Tests completed (compilation skipped)!"
                return 0
            else
                local exit_code=$?
                if [ $exit_code -eq 124 ]; then
                    print_error "❌ Tests timed out after ${test_timeout}s (10 minutes)"
                    print_status "💡 Try running tests with --no-test flag: $0 test --no-test"
                else
                    print_error "❌ Tests failed with exit code $exit_code"
                fi
            fi
        else
            print_status "Running tests..."
            print_status "⏱️  Test timeout set to ${test_timeout}s (10 minutes)"
            if timeout $test_timeout docker-compose --profile test up test --abort-on-container-exit; then
                print_success "Tests completed successfully!"
                return 0
            else
                local exit_code=$?
                if [ $exit_code -eq 124 ]; then
                    print_error "❌ Tests timed out after ${test_timeout}s (10 minutes)"
                    print_status "💡 Try running tests with --no-test flag: $0 test --no-test"
                else
                    print_error "❌ Tests failed with exit code $exit_code"
                fi
            fi
        fi
        retry_count=$((retry_count + 1))
        if [ $retry_count -lt $max_retries ]; then
            print_warning "Tests failed, retrying... (attempt $retry_count/$max_retries)"
            sleep 3
        else
            print_error "Tests failed after $max_retries attempts"
            print_status "💡 Troubleshooting tips:"
            print_status "   - Check Docker is running: docker info"
            print_status "   - Clean and retry: $0 clean && $0 test"
            print_status "   - Try with --no-test: $0 test --no-test"
            return 1
        fi
    done
}

run_quality() {
    local skip_tests=false
    local quality_timeout=900  # 15 minutes timeout
    local max_retries=2
    local retry_count=0
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    while [ $retry_count -lt $max_retries ]; do
        if [ "$skip_tests" = true ]; then
            print_status "Running quality checks in Docker (skipping tests)..."
            print_status "⏱️  Quality check timeout set to ${quality_timeout}s (15 minutes)"
            if timeout $quality_timeout SKIP_TESTS=true docker-compose --profile quality up quality --abort-on-container-exit; then
                print_success "Quality checks completed (tests skipped)!"
                return 0
            else
                local exit_code=$?
                if [ $exit_code -eq 124 ]; then
                    print_error "❌ Quality checks timed out after ${quality_timeout}s (15 minutes)"
                    print_status "💡 Try running quality checks with --no-test flag: $0 quality --no-test"
                else
                    print_error "❌ Quality checks failed with exit code $exit_code"
                fi
            fi
        else
            print_status "Running quality checks in Docker..."
            print_status "⏱️  Quality check timeout set to ${quality_timeout}s (15 minutes)"
            if timeout $quality_timeout docker-compose --profile quality up quality --abort-on-container-exit; then
                print_success "Quality checks completed successfully!"
                return 0
            else
                local exit_code=$?
                if [ $exit_code -eq 124 ]; then
                    print_error "❌ Quality checks timed out after ${quality_timeout}s (15 minutes)"
                    print_status "💡 Try running quality checks with --no-test flag: $0 quality --no-test"
                else
                    print_error "❌ Quality checks failed with exit code $exit_code"
                fi
            fi
        fi
        retry_count=$((retry_count + 1))
        if [ $retry_count -lt $max_retries ]; then
            print_warning "Quality checks failed, retrying... (attempt $retry_count/$max_retries)"
            sleep 3
        else
            print_error "Quality checks failed after $max_retries attempts"
            print_status "💡 Troubleshooting tips:"
            print_status "   - Check Docker is running: docker info"
            print_status "   - Clean and retry: $0 clean && $0 quality"
            print_status "   - Try with --no-test: $0 quality --no-test"
            return 1
        fi
    done
}

run_quality_local() {
    local skip_tests=false
    for arg in "$@"; do
        if [ "$arg" = "--no-test" ]; then
            skip_tests=true
            break
        fi
    done
    print_status "Running quality checks locally..."
    if [ ! -f "build.gradle" ]; then
        print_error "Error: This script must be run from the project root directory"
        exit 1
    fi
    if ! command -v java >/dev/null 2>&1; then
        print_error "Java not found. Please install Java 21 or use 'quality' command for Docker."
        exit 1
    fi
    if ! command -v ./gradlew >/dev/null 2>&1; then
        print_error "Gradle wrapper not found. Please use 'quality' command for Docker."
        exit 1
    fi
    print_status "🧹 Cleaning previous reports..."
    ./gradlew clean
    print_status "🔍 Running Checkstyle..."
    if ./gradlew checkstyleMain; then
        print_success "✅ Checkstyle completed successfully"
    else
        print_warning "⚠️  Checkstyle found violations"
    fi
    print_status "🔍 Running PMD..."
    if ./gradlew pmdMain; then
        print_success "✅ PMD completed successfully"
    else
        print_warning "⚠️  PMD found violations"
    fi
    print_status "🔍 Running SpotBugs..."
    if ./gradlew spotbugsMain; then
        print_success "✅ SpotBugs completed successfully"
    else
        print_warning "⚠️  SpotBugs found violations"
    fi
    if [ "$skip_tests" = false ]; then
        print_status "🧪 Running tests with coverage..."
        if ./gradlew test jacocoTestReport; then
            print_success "✅ Tests completed successfully"
        else
            print_error "❌ Tests failed"
            exit 1
        fi
    else
        print_status "🧪 Skipping tests (--no-test flag used)"
    fi
    print_status "📊 Generating quality report..."
    if [ "$skip_tests" = true ]; then
        ./gradlew qualityCheckNoTests
    else
        ./gradlew qualityCheck
    fi
    echo ""; echo "🎯 QUALITY ANALYSIS SUMMARY"; echo "=========================="; echo "📁 Reports location: build/reports/"; echo "📊 Checkstyle: build/reports/checkstyle/"; echo "📊 PMD: build/reports/pmd/"; echo "📊 SpotBugs: build/reports/spotbugs/"; if [ "$skip_tests" = false ]; then echo "📊 Tests: build/reports/tests/"; echo "📊 Coverage: build/reports/jacoco/"; echo "📄 Quality Report: build/quality-report.txt"; else echo "📊 Tests: SKIPPED (--no-test flag used)"; echo "📊 Coverage: SKIPPED (--no-test flag used)"; echo "📄 Quality Report: build/quality-report-no-tests.txt"; fi; if docker-compose --profile sonarqube ps sonarqube | grep -q "Up"; then echo ""; print_status "🚀 SonarQube is running!"; print_status "Access SonarQube at: http://localhost:9000"; print_status "To run analysis: $0 sonarqube-scan"; else echo ""; print_warning "⚠️  SonarQube is not running."; echo ""; print_status "📋 To start SonarQube:"; echo "   $0 sonarqube-start"; echo ""; print_status "📋 To run SonarQube analysis:"; echo "   $0 sonarqube-scan"; echo ""; print_status "📊 For now, you can view local reports at:"; echo "   - Quality Report: build/quality-report-no-tests.txt"; echo "   - Checkstyle: build/reports/checkstyle/"; echo "   - PMD: build/reports/pmd/"; echo "   - SpotBugs: build/reports/spotbugs/"; fi; echo ""; if [ "$skip_tests" = true ]; then print_success "🎉 Quality analysis completed (tests skipped)!"; else print_success "🎉 Quality analysis completed!"; fi; echo "📋 Review the reports above to see detailed results."
}

run_checkstyle_clean() {
    local max_retries=2
    local retry_count=0
    print_status "🧹 Cleaning project..."
    while [ $retry_count -lt $max_retries ]; do
        if ./gradlew clean; then
            print_success "✅ Clean completed successfully"
            break
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "Clean failed, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                print_error "❌ Clean failed after $max_retries attempts"
                exit 1
            fi
        fi
    done
    retry_count=0
    print_status "🔍 Running Checkstyle with stacktrace..."
    while [ $retry_count -lt $max_retries ]; do
        if ./gradlew checkstyleMain --stacktrace; then
            print_success "✅ Checkstyle completed successfully"
            return 0
        else
            retry_count=$((retry_count + 1))
            if [ $retry_count -lt $max_retries ]; then
                print_warning "Checkstyle failed, retrying... (attempt $retry_count/$max_retries)"
                sleep 2
            else
                print_warning "⚠️  Checkstyle found violations after $max_retries attempts"
                print_status "📊 Check Checkstyle report at: build/reports/checkstyle/"
                return 1
            fi
        fi
    done
} 