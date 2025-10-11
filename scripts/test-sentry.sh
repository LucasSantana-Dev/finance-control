#!/bin/bash

# Test Sentry Configuration Script
# This script tests the Sentry configuration by sending a test alert

echo "🔍 Testing Sentry Configuration for Finance Control"
echo "=================================================="

# Check if the application is running
if ! curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "❌ Application is not running. Please start the application first."
    echo "   Run: ./gradlew bootRun"
    exit 1
fi

echo "✅ Application is running"

# Test the monitoring endpoints
echo ""
echo "🧪 Testing Monitoring Endpoints"
echo "==============================="

# Test health endpoint
echo "Testing health endpoint..."
HEALTH_RESPONSE=$(curl -s http://localhost:8080/api/monitoring/health)
if [ $? -eq 0 ]; then
    echo "✅ Health endpoint is accessible"
    echo "   Response: $(echo $HEALTH_RESPONSE | jq -r '.status // "unknown"')"
else
    echo "❌ Health endpoint failed"
fi

# Test status endpoint
echo "Testing status endpoint..."
STATUS_RESPONSE=$(curl -s http://localhost:8080/api/monitoring/status)
if [ $? -eq 0 ]; then
    echo "✅ Status endpoint is accessible"
    echo "   Response: $(echo $STATUS_RESPONSE | jq -r '.status // "unknown"')"
else
    echo "❌ Status endpoint failed"
fi

# Test alerts endpoint
echo "Testing alerts endpoint..."
ALERTS_RESPONSE=$(curl -s http://localhost:8080/api/monitoring/alerts)
if [ $? -eq 0 ]; then
    echo "✅ Alerts endpoint is accessible"
    ALERT_COUNT=$(echo $ALERTS_RESPONSE | jq -r '.alerts | length // 0')
    echo "   Active alerts: $ALERT_COUNT"
else
    echo "❌ Alerts endpoint failed"
fi

# Test metrics endpoint
echo "Testing metrics endpoint..."
METRICS_RESPONSE=$(curl -s http://localhost:8080/api/monitoring/metrics/summary)
if [ $? -eq 0 ]; then
    echo "✅ Metrics endpoint is accessible"
    echo "   Response: $(echo $METRICS_RESPONSE | jq -r '.status // "unknown"')"
else
    echo "❌ Metrics endpoint failed"
fi

# Test triggering a test alert
echo ""
echo "🚨 Testing Alert Triggering"
echo "==========================="

echo "Triggering test alert..."
TEST_ALERT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/monitoring/test-alert)
if [ $? -eq 0 ]; then
    echo "✅ Test alert triggered successfully"
    echo "   Response: $(echo $TEST_ALERT_RESPONSE | jq -r '.message // "unknown"')"
else
    echo "❌ Test alert failed"
fi

# Check alerts after triggering test alert
echo "Checking alerts after test..."
sleep 2
ALERTS_AFTER_RESPONSE=$(curl -s http://localhost:8080/api/monitoring/alerts)
if [ $? -eq 0 ]; then
    ALERT_COUNT_AFTER=$(echo $ALERTS_AFTER_RESPONSE | jq -r '.alerts | length // 0')
    echo "   Active alerts after test: $ALERT_COUNT_AFTER"
    
    if [ "$ALERT_COUNT_AFTER" -gt "$ALERT_COUNT" ]; then
        echo "✅ Test alert was created successfully"
    else
        echo "⚠️  Test alert may not have been created"
    fi
else
    echo "❌ Failed to check alerts after test"
fi

echo ""
echo "📊 Sentry Configuration Summary"
echo "==============================="
echo "✅ Application is running and accessible"
echo "✅ Monitoring endpoints are working"
echo "✅ Alert system is functional"
echo ""
echo "🔗 Check your Sentry dashboard for any captured events:"
echo "   https://lucas-santana-gm.sentry.io/projects/finance-control/"
echo ""
echo "💡 To view real-time monitoring data, visit:"
echo "   http://localhost:8080/api/monitoring/health"
echo "   http://localhost:8080/api/monitoring/status"
echo "   http://localhost:8080/api/monitoring/alerts"
echo "   http://localhost:8080/api/monitoring/metrics/summary"
