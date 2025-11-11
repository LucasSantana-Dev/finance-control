#!/bin/bash

# Security Check Script Module
# Runs comprehensive security checks for Java/Spring Boot projects
# Adapted from React Native template patterns

# Source core utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/core.sh"

# Configuration
SOURCE_DIRS="src/main/java"
EXCLUDE_DIRS="build|target|.gradle|bin|out|.idea|.vscode|.settings|logs|coverage|test"
ISSUES=()
WARNINGS=()

# Add security issue
add_issue() {
    local severity="$1"
    local message="$2"
    local details="${3:-}"

    if [ "$severity" = "error" ]; then
        ISSUES+=("$message|$details")
    else
        WARNINGS+=("$message|$details")
    fi
}

# Run dependency vulnerability check
run_dependency_check() {
    print_status "🔍 Running dependency vulnerability check..."
    echo ""

    # Check if OWASP Dependency Check plugin is available
    if grep -q "org.owasp.dependencycheck" build.gradle 2>/dev/null; then
        print_status "Using OWASP Dependency Check plugin..."
        if ./gradlew dependencyCheckAnalyze --no-daemon >/dev/null 2>&1; then
            local report_file="build/reports/dependency-check-report.html"
            if [ -f "$report_file" ]; then
                # Check if report contains vulnerabilities
                if grep -qi "high\|critical\|medium" "$report_file" 2>/dev/null; then
                    add_issue "error" "Dependency vulnerabilities found" "Check report at: $report_file"
                else
                    print_success "✅ No critical dependency vulnerabilities found"
                fi
            fi
        else
            add_issue "warning" "Dependency check analysis encountered issues" "Run manually: ./gradlew dependencyCheckAnalyze"
        fi
    else
        print_warning "⚠️  OWASP Dependency Check plugin not configured"
        echo "   Consider adding it to build.gradle:"
        echo "   plugins { id 'org.owasp.dependencycheck' version '9.0.9' }"
        echo ""
        print_status "Checking for Gradle dependency audit..."

        # Try Gradle's built-in dependency insight
        if ./gradlew dependencies --no-daemon 2>&1 | grep -qi "vulnerability\|CVE\|security"; then
            add_issue "warning" "Potential dependency issues detected" "Review dependencies manually"
        else
            print_success "✅ Basic dependency check passed (consider adding OWASP Dependency Check for comprehensive scan)"
        fi
    fi
}

# Check for critical vulnerabilities (if OWASP report exists)
check_critical_vulnerabilities() {
    print_status "🔍 Checking for critical vulnerabilities..."
    echo ""

    local report_file="build/reports/dependency-check-report.html"

    if [ ! -f "$report_file" ]; then
        print_warning "⚠️  Dependency check report not found. Run dependency check first."
        return
    fi

    if grep -qi "critical" "$report_file" 2>/dev/null; then
        add_issue "error" "Critical vulnerabilities found in dependencies" "Review report at: $report_file"
    else
        print_success "✅ No critical vulnerabilities found"
    fi
}

# Check for high severity vulnerabilities
check_high_vulnerabilities() {
    print_status "🔍 Checking for high severity vulnerabilities..."
    echo ""

    local report_file="build/reports/dependency-check-report.html"

    if [ ! -f "$report_file" ]; then
        print_warning "⚠️  Dependency check report not found. Run dependency check first."
        return
    fi

    if grep -qi "\"high\"" "$report_file" 2>/dev/null || grep -qi "severity.*high" "$report_file" 2>/dev/null; then
        add_issue "warning" "High severity vulnerabilities found" "Review report at: $report_file"
    else
        print_success "✅ No high severity vulnerabilities found"
    fi
}

# Check for outdated dependencies
check_outdated_dependencies() {
    print_status "🔍 Checking for outdated dependencies..."
    echo ""

    if ./gradlew dependencyUpdates --no-daemon 2>&1 | grep -qi "outdated\|newer"; then
        add_issue "warning" "Some dependencies may be outdated" "Run: ./gradlew dependencyUpdates to see details"
    else
        print_success "✅ All dependencies appear to be up to date"
    fi
}

# Check license compliance
check_license_compliance() {
    print_status "🔍 Checking license compliance..."
    echo ""

    # Simplified check - in production, use a proper license checker
    if command -v license-gradle-plugin >/dev/null 2>&1 || grep -q "com.github.hierynomus.license" build.gradle 2>/dev/null; then
        if ./gradlew licenseCheck --no-daemon >/dev/null 2>&1; then
            print_success "✅ License compliance check passed"
        else
            add_issue "warning" "License compliance issues detected" "Run: ./gradlew licenseCheck"
        fi
    else
        print_warning "⚠️  License plugin not configured"
        echo "   Consider adding license checking plugin to build.gradle"
        echo "   For now, manual review of dependencies is recommended"
    fi
}

# Get Java source files
get_java_source_files() {
    find $SOURCE_DIRS -type f -name "*.java" 2>/dev/null | grep -vE "$EXCLUDE_DIRS" || true
}

# Check security patterns in code
check_security_patterns() {
    print_status "🔍 Checking for security patterns in code..."
    echo ""

    local source_files
    readarray -t source_files < <(get_java_source_files)
    local found_issues=false

    if [ ${#source_files[@]} -eq 0 ]; then
        print_warning "No Java source files found"
        return
    fi

    # Security patterns to check
    declare -A patterns
    patterns[".*password.*=.*[\"'].*[\"'].*"]="error|Hardcoded password detected|Use environment variables or secure configuration"
    patterns[".*api[_-]?key.*=.*[\"'].*[\"'].*"]="error|Hardcoded API key detected|Use environment variables or secure storage"
    patterns[".*secret.*=.*[\"'].*[\"'].*"]="error|Hardcoded secret detected|Use secure configuration management"
    patterns["SELECT.*\\+.*FROM"]="error|SQL injection vulnerability - string concatenation in SQL|Use parameterized queries or JPA"
    patterns["Statement\\.executeQuery.*\\+"]="error|SQL injection vulnerability - dynamic SQL in Statement|Use PreparedStatement or JPA"
    patterns["new Random\\(\\)"]="warning|Insecure random number generation|Use SecureRandom for cryptographic operations"
    patterns["MD5|SHA1"]="warning|Weak cryptographic hash function|Use SHA-256 or stronger"
    patterns["DES|3DES"]="warning|Weak encryption algorithm|Use AES-256 or stronger"
    patterns["System\\.out\\.print.*password"]="error|Password logging detected|Remove or use proper logging levels"
    patterns["log\\.(info|debug).*password"]="error|Password logging detected|Remove sensitive data from logs"
    patterns["log\\.(info|debug).*token"]="error|Token logging detected|Remove sensitive data from logs"

    for file in "${source_files[@]}"; do
        local line_num=0
        while IFS= read -r line; do
            line_num=$((line_num + 1))

            for pattern in "${!patterns[@]}"; do
                if echo "$line" | grep -qiE "$pattern"; then
                    local severity_message="${patterns[$pattern]}"
                    local severity=$(echo "$severity_message" | cut -d'|' -f1)
                    local message=$(echo "$severity_message" | cut -d'|' -f2)
                    local suggestion=$(echo "$severity_message" | cut -d'|' -f3)

                    add_issue "$severity" "$message in $file (line $line_num)" "$suggestion"
                    found_issues=true
                fi
            done
        done < "$file"
    done

    if [ "$found_issues" = false ]; then
        print_success "✅ No obvious security patterns detected in code"
    fi
}

# Check for exposed sensitive data in application properties
check_exposed_secrets() {
    print_status "🔍 Checking for exposed secrets in configuration..."
    echo ""

    local config_files=("src/main/resources/application.properties" "src/main/resources/application.yml" ".env")
    local found_secrets=false

    for config_file in "${config_files[@]}"; do
        if [ ! -f "$config_file" ]; then
            continue
        fi

        # Check for potential secrets
        if grep -qiE "(password|secret|api[_-]?key|token|private[_-]?key).*=.*[a-zA-Z0-9]{16,}" "$config_file" 2>/dev/null; then
            # Check if it's using a variable reference (should be safe)
            if ! grep -qiE "(password|secret|api[_-]?key|token|private[_-]?key).*=.*\$\{" "$config_file" 2>/dev/null; then
                add_issue "error" "Potential hardcoded secret in $config_file" "Use environment variables or Spring Cloud Config"
                found_secrets=true
            fi
        fi
    done

    if [ "$found_secrets" = false ]; then
        print_success "✅ No obvious hardcoded secrets found in configuration files"
    fi
}

# Print summary
print_summary() {
    echo ""
    print_status "🔒 Security Check Summary"
    echo "$(printf '=%.0s' {1..50})"
    echo ""

    if [ ${#ISSUES[@]} -eq 0 ] && [ ${#WARNINGS[@]} -eq 0 ]; then
        print_success "🎉 All security checks passed!"
        return 0
    fi

    if [ ${#ISSUES[@]} -gt 0 ]; then
        print_error "Found ${#ISSUES[@]} security issues:"
        echo ""
        for issue in "${ISSUES[@]}"; do
            local message=$(echo "$issue" | cut -d'|' -f1)
            local details=$(echo "$issue" | cut -d'|' -f2)
            echo "  • $message"
            if [ -n "$details" ]; then
                echo "    $details"
            fi
        done
        echo ""
    fi

    if [ ${#WARNINGS[@]} -gt 0 ]; then
        print_warning "Found ${#WARNINGS[@]} security warnings:"
        echo ""
        for warning in "${WARNINGS[@]}"; do
            local message=$(echo "$warning" | cut -d'|' -f1)
            local details=$(echo "$warning" | cut -d'|' -f2)
            echo "  • $message"
            if [ -n "$details" ]; then
                echo "    $details"
            fi
        done
        echo ""
    fi

    if [ ${#ISSUES[@]} -gt 0 ]; then
        return 1
    fi

    return 0
}

# Main function
run_security_check() {
    local skip_dependency_check=false

    # Check for --skip-deps parameter
    for arg in "$@"; do
        if [ "$arg" = "--skip-deps" ]; then
            skip_dependency_check=true
            break
        fi
    done

    print_status "🔒 Security Check Starting..."
    echo ""

    # Check if we're in the project root
    if [ ! -f "build.gradle" ]; then
        print_error "Error: This script must be run from the project root directory"
        exit 1
    fi

    # Run checks
    if [ "$skip_dependency_check" = false ]; then
        run_dependency_check
        echo ""
        check_critical_vulnerabilities
        echo ""
        check_high_vulnerabilities
        echo ""
        check_outdated_dependencies
        echo ""
        check_license_compliance
        echo ""
    else
        print_status "⏭️  Skipping dependency checks (using existing reports)"
        echo ""
    fi

    check_security_patterns
    echo ""
    check_exposed_secrets

    # Print summary and exit
    if print_summary; then
        return 0
    else
        return 1
    fi
}

# Run if executed directly
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    run_security_check "$@"
    exit $?
fi
