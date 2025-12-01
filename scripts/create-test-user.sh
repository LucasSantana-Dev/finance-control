#!/bin/bash

# Finance Control - Create Test User Script

API_URL="http://localhost:8080/auth/register"
EMAIL="test@example.com"
PASSWORD="password123"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "Creating test user..."
echo "Email: $EMAIL"
echo "Password: $PASSWORD"

# Check if curl is installed
if ! command -v curl &> /dev/null; then
    echo -e "${RED}Error: curl is not installed.${NC}"
    exit 1
fi

# Send request
response=$(curl -s -w "\n%{http_code}" -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"$EMAIL\", \"password\": \"$PASSWORD\"}")

# Parse response
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
  echo -e "${GREEN}User created successfully!${NC}"
  echo "Response: $body"
else
  echo -e "${RED}Failed to create user. HTTP Code: $http_code${NC}"
  echo "Response: $body"
  echo ""
  echo "Troubleshooting:"
  echo "1. Make sure the application is running (./scripts/dev.sh start)"
  echo "2. Check if the user already exists"
  echo "3. Check application logs (./scripts/dev.sh logs)"
fi
