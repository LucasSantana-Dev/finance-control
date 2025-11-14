#!/bin/bash

# Script to verify that the Finance Control application is running and healthy
# This script should be run before executing the Postman collection

echo "🔍 Verifying Finance Control application is running..."

# Check if the application is responding on port 8080
echo "📡 Checking if application responds on port 8080..."
if curl -s -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Application is responding on port 8080"

    # Check the health status
    echo "🏥 Checking application health status..."
    HEALTH_RESPONSE=$(curl -s http://localhost:8080/actuator/health)

    if echo "$HEALTH_RESPONSE" | grep -q '"status":"UP"'; then
        echo "✅ Application health status: UP"
        echo "🎉 Application is ready for testing!"
        exit 0
    else
        echo "❌ Application health status is not UP. Response: $HEALTH_RESPONSE"
        exit 1
    fi
else
    echo "❌ Application is not responding on port 8080"
    echo "💡 Make sure to start the application first:"
    echo "   ./gradlew bootRun"
    echo "   or"
    echo "   ./scripts/dev.sh start"
    exit 1
fi
