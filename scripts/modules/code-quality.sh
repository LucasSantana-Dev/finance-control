#!/bin/bash

# Code Quality Checker Module
# Checks file size limits and complexity violations using Gradle quality tools
# Adapted from React Native template patterns for Java/Spring Boot

# Source core utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/core.sh"

# Configuration
MAX_FILE_LINES=2000  # Java classes max lines (from Checkstyle config)
MAX_COMPLEXITY=10    # Cyclomatic complexity limit (from Checkstyle/PMD config)
SOURCE_EXTENSIONS=".java"
EXCLUDE_DIRS="node_modules|.git|build|target|.gradle|bin|out|.idea|.vscode|.settings|logs|coverage"

# Count lines in a file
count_lines() {
    local file_path="$1"
    if [ -f "$file_path" ]; then
        wc -l < "$file_path" | tr -d ' '
    else
        echo "0"
    fi
}

# Get all Java source files recursively
get_source_files() {
    local dir="${1:-src/main/java}"
    local files=()

    if [ ! -d "$dir" ]; then
        return
    fi

    while IFS= read -r -d '' file; do
        files+=("$file")
    done < <(find "$dir" -type f -name "*.java" -print0 2>/dev/null)

    printf '%s\n' "${files[@]}"
}

# Check file size limits
check_file_sizes() {
    print_status "📏 Checking file sizes..."
    echo ""

    local violations=0
    local source_files
    readarray -t source_files < <(get_source_files)

    if [ ${#source_files[@]} -eq 0 ]; then
        print_warning "No Java source files found in src/main/java"
        echo "0"
        return
    fi

    for file in "${source_files[@]}"; do
        # Skip excluded directories
        if echo "$file" | grep -qE "$EXCLUDE_DIRS"; then
            continue
        fi

        local line_count=$(count_lines "$file")

        if [ "$line_count" -gt "$MAX_FILE_LINES" ]; then
            if [ $violations -eq 0 ]; then
                echo ""
            fi
            local over=$((line_count - MAX_FILE_LINES))
            echo "  ❌ $file"
            echo "     Lines: $line_count ($over over limit)"
            echo "     Suggestion: Consider splitting this file into smaller modules"
            echo ""
            violations=$((violations + 1))
        fi
    done

    if [ $violations -eq 0 ]; then
        print_success "✅ All files are within the ${MAX_FILE_LINES}-line limit"
    else
        print_warning "⚠️  $violations files exceed the ${MAX_FILE_LINES}-line limit"
    fi

    echo "$violations"
}

# Parse Checkstyle XML report for complexity violations
parse_checkstyle_complexity() {
    local report_file="$1"
    local violations=0

    if [ ! -f "$report_file" ]; then
        echo "0"
        return
    fi

    # Check if xmlstarlet is available, otherwise use grep/sed
    if command -v xmlstarlet >/dev/null 2>&1; then
        violations=$(xmlstarlet sel -t -c "count(//error[@source='com.puppycrawl.tools.checkstyle.checks.metrics.CyclomaticComplexityCheck'])" "$report_file" 2>/dev/null || echo "0")
    else
        violations=$(grep -c "CyclomaticComplexityCheck" "$report_file" 2>/dev/null || echo "0")
    fi

    echo "$violations"
}

# Parse PMD XML report for complexity violations
parse_pmd_complexity() {
    local report_file="$1"
    local violations=0

    if [ ! -f "$report_file" ]; then
        echo "0"
        return
    fi

    # Check if xmlstarlet is available
    if command -v xmlstarlet >/dev/null 2>&1; then
        violations=$(xmlstarlet sel -t -c "count(//violation[@rule='CyclomaticComplexity'])" "$report_file" 2>/dev/null || echo "0")
    else
        violations=$(grep -c "CyclomaticComplexity" "$report_file" 2>/dev/null || echo "0")
    fi

    echo "$violations"
}

# Check complexity using Checkstyle and PMD reports
check_complexity() {
    print_status "🧠 Checking function complexity..."
    echo ""

    local checkstyle_report="build/reports/checkstyle/main.xml"
    local pmd_report="build/reports/pmd/main.xml"

    local checkstyle_violations=0
    local pmd_violations=0

    # Parse Checkstyle report
    if [ -f "$checkstyle_report" ]; then
        checkstyle_violations=$(parse_checkstyle_complexity "$checkstyle_report")
    fi

    # Parse PMD report
    if [ -f "$pmd_report" ]; then
        pmd_violations=$(parse_pmd_complexity "$pmd_report")
    fi

    local total_violations=$((checkstyle_violations + pmd_violations))

    if [ $total_violations -eq 0 ]; then
        print_success "✅ All functions are within the complexity limit of ${MAX_COMPLEXITY}"
    else
        print_warning "⚠️  Found $total_violations complexity violations"
        if [ $checkstyle_violations -gt 0 ]; then
            echo "   - Checkstyle: $checkstyle_violations violations"
            echo "     Report: $checkstyle_report"
        fi
        if [ $pmd_violations -gt 0 ]; then
            echo "   - PMD: $pmd_violations violations"
            echo "     Report: $pmd_report"
        fi
        echo ""
        echo "   💡 Suggestion: Consider breaking complex functions into smaller, more focused functions"
    fi

    echo ""
    echo "$total_violations"
}

# Generate summary report
generate_report() {
    local file_violations="$1"
    local complexity_violations="$2"

    echo ""
    echo "📊 Code Quality Summary"
    echo "$(printf '=%.0s' {1..50})"
    echo "File Size Violations: $file_violations"
    echo "Complexity Violations: $complexity_violations"
    echo "$(printf '=%.0s' {1..50})"
    echo ""

    if [ "$file_violations" -eq 0 ] && [ "$complexity_violations" -eq 0 ]; then
        print_success "🎉 All code quality checks passed!"
        echo "Your codebase maintains excellent quality standards."
    else
        print_warning "⚠️  Code quality issues detected."
        echo "Please review the suggestions above and refactor as needed."
        echo ""
        echo "📊 Detailed reports available at:"
        echo "   - Checkstyle: build/reports/checkstyle/"
        echo "   - PMD: build/reports/pmd/"
        echo "   - SpotBugs: build/reports/spotbugs/"
    fi
}

# Main function
run_code_quality_check() {
    local skip_quality_tools=false

    # Check for --skip-tools parameter
    for arg in "$@"; do
        if [ "$arg" = "--skip-tools" ]; then
            skip_quality_tools=true
            break
        fi
    done

    print_status "🔍 Running Code Quality Checks"
    echo ""
    echo "File size limit: $MAX_FILE_LINES lines"
    echo "Complexity limit: $MAX_COMPLEXITY"
    echo "$(printf '=%.0s' {1..50})"
    echo ""

    # Check if we're in the project root
    if [ ! -f "build.gradle" ]; then
        print_error "Error: This script must be run from the project root directory"
        exit 1
    fi

    # Run quality tools if not skipped
    if [ "$skip_quality_tools" = false ]; then
        print_status "🔍 Running quality analysis tools..."
        echo ""

        # Run Checkstyle
        if ! ./gradlew checkstyleMain --no-daemon >/dev/null 2>&1; then
            print_warning "Checkstyle found violations (check build/reports/checkstyle/)"
        fi

        # Run PMD
        if ! ./gradlew pmdMain --no-daemon >/dev/null 2>&1; then
            print_warning "PMD found violations (check build/reports/pmd/)"
        fi
    else
        print_status "⏭️  Skipping quality tools execution (using existing reports)"
    fi

    echo ""

    # Check file sizes
    local file_violations
    file_violations=$(check_file_sizes)

    # Check complexity
    echo ""
    local complexity_violations
    complexity_violations=$(check_complexity)

    # Generate report
    generate_report "$file_violations" "$complexity_violations"

    # Exit with error code if violations found
    if [ "$file_violations" -gt 0 ] || [ "$complexity_violations" -gt 0 ]; then
        return 1
    fi

    return 0
}

# Run if executed directly
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    run_code_quality_check "$@"
    exit $?
fi
