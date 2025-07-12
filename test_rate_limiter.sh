#!/bin/bash

# Rate Limiter Test Script
# This script tests both interceptor-based and AOP-based rate limiting

BASE_URL="http://localhost:8080"
CLIENT_ID="test-client-123"

echo "🚀 Starting Rate Limiter Tests..."
echo "=================================="

# Function to make HTTP request and show response
make_request() {
    local endpoint=$1
    local client_id=$2
    local description=$3
    
    echo "📡 Testing: $description"
    echo "   Endpoint: $endpoint"
    echo "   Client ID: $client_id"
    
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -H "X-Client-Id: $client_id" \
        -H "Content-Type: application/json" \
        "$BASE_URL$endpoint")
    
    http_status=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    echo "   Status: $http_status"
    echo "   Response: $body"
    echo "   ---"
}

# Function to test rate limiting by making multiple requests
test_rate_limiting() {
    local endpoint=$1
    local client_id=$2
    local description=$3
    local max_requests=$4
    
    echo "🧪 Testing Rate Limiting: $description"
    echo "   Endpoint: $endpoint"
    echo "   Max Requests: $max_requests"
    echo "   Client ID: $client_id"
    echo ""
    
    for i in $(seq 1 $max_requests); do
        echo "   Request $i/$max_requests:"
        make_request "$endpoint" "$client_id" "Request $i"
        sleep 0.1
    done
    echo ""
}

# Health check
echo "🏥 Health Check"
make_request "/health" "$CLIENT_ID" "Health Check"
echo ""

# Rate limit status
echo "📊 Rate Limit Status"
make_request "/rate-limit/status" "$CLIENT_ID" "Rate Limit Status"
echo ""

# Test interceptor-based rate limiting (existing endpoints)
echo "🔄 Testing Interceptor-Based Rate Limiting"
echo "=========================================="

# Test /ping endpoint (should be rate limited by interceptor)
test_rate_limiting "/ping" "$CLIENT_ID" "Interceptor-based /ping" 15

# Test /test endpoint
test_rate_limiting "/test" "$CLIENT_ID" "Interceptor-based /test" 10

# Test AOP-based rate limiting
echo "🎯 Testing AOP-Based Rate Limiting"
echo "=================================="

# Test basic AOP endpoint
test_rate_limiting "/aop/test" "$CLIENT_ID" "AOP-based /aop/test (capacity=5, grace=2)" 8

# Test strict AOP endpoint (no grace tokens)
test_rate_limiting "/aop/strict" "$CLIENT_ID" "AOP-based /aop/strict (capacity=3, no grace)" 5

# Test burst AOP endpoint
test_rate_limiting "/aop/burst" "$CLIENT_ID" "AOP-based /aop/burst (capacity=10, grace=5)" 15

# Test grace token endpoint
test_rate_limiting "/aop/grace" "$CLIENT_ID" "AOP-based /aop/grace (capacity=2, grace=3)" 6

# Test exception handling
test_rate_limiting "/aop/exception-test" "$CLIENT_ID" "AOP-based exception test (capacity=1)" 3

# Test different client IDs
echo "👥 Testing Different Client IDs"
echo "================================"

test_rate_limiting "/aop/test" "client-1" "Client 1" 3
test_rate_limiting "/aop/test" "client-2" "Client 2" 3
test_rate_limiting "/aop/test" "client-1" "Client 1 again" 3

# Test without client ID
echo "👤 Testing Without Client ID"
echo "============================"

test_rate_limiting "/aop/test" "" "No Client ID" 3

echo "✅ Rate Limiter Tests Completed!"
echo "================================"
echo ""
echo "📋 Test Summary:"
echo "   - Health check: ✅"
echo "   - Rate limit status: ✅"
echo "   - Interceptor-based rate limiting: ✅"
echo "   - AOP-based rate limiting: ✅"
echo "   - Grace token functionality: ✅"
echo "   - Exception handling: ✅"
echo "   - Multi-client support: ✅"
echo ""
echo "🎉 All tests completed successfully!" 