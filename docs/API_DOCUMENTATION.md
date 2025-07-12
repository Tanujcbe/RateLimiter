# Rate Limiter - API Documentation

## 📋 Overview

Complete API reference for the Rate Limiter application with examples and usage patterns.

## 🔗 Base URL

```
http://localhost:8080
```

## 📊 API Endpoints

### Health & Status

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Application health check |
| `/rate-limit/status` | GET | Rate limiter status and features |

### Rate Limiting Endpoints

| Endpoint | Method | Rate Limit Configuration |
|----------|--------|-------------------------|
| `/aop/test` | GET | 5 tokens, 1/sec, 2 grace |
| `/aop/strict` | GET | 3 tokens, 1/sec, 0 grace |
| `/aop/burst` | GET | 10 tokens, 2/sec, 5 grace |
| `/aop/grace` | GET | 2 tokens, 1/sec, 3 grace |
| `/ping` | GET | Interceptor-based (10 req/min) |

## 🔧 Usage Examples

### Basic Health Check
```bash
curl http://localhost:8080/health
```

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00",
  "service": "RateLimiter"
}
```

### Rate Limiting Test
```bash
curl -H "X-Client-Id: test-client" http://localhost:8080/aop/test
```

**Response:**
```json
{
  "message": "AOP Rate Limited Endpoint",
  "timestamp": "2024-01-15T10:30:00",
  "status": "success"
}
```

### Rate Limit Exceeded
```bash
# Make multiple requests to trigger rate limiting
for i in {1..10}; do
  curl -H "X-Client-Id: test-client" http://localhost:8080/aop/strict
  sleep 0.1
done
```

**Response (when rate limited):**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 429,
  "error": "Rate Limit Exceeded",
  "message": "Rate limit exceeded for key: rate:test-client:/aop/strict",
  "path": "/aop/strict"
}
```

## 🎯 Rate Limiting Configuration

### Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `capacity` | Maximum tokens in bucket | 5 |
| `refillTokensPerSecond` | Tokens refilled per second | 1 |
| `grace` | Additional burst tokens | 0 |

### Key Generation
```
rate:<client-id>:<api-path>
```

**Examples:**
- `rate:test-client-123:/aop/test`
- `rate:user-456:/ping`
- `rate:anonymous:/apiTest`

### Client Identification
```bash
# Use X-Client-Id header
curl -H "X-Client-Id: mobile-app-v1" http://localhost:8080/aop/test
curl -H "X-Client-Id: web-client" http://localhost:8080/aop/test
curl -H "X-Client-Id: api-gateway" http://localhost:8080/aop/test
```

## 🧪 Testing Patterns

### Basic Rate Limiting Test
```bash
#!/bin/bash
for i in {1..10}; do
  echo "Request $i:"
  curl -s -w "HTTP Status: %{http_code}\n" \
    -H "X-Client-Id: test-client" \
    http://localhost:8080/aop/test
  sleep 0.1
done
```

### Multi-Client Test
```bash
#!/bin/bash
clients=("client-1" "client-2" "client-3")

for client in "${clients[@]}"; do
  echo "Testing client: $client"
  for i in {1..5}; do
    curl -s -w "HTTP Status: %{http_code}\n" \
      -H "X-Client-Id: $client" \
      http://localhost:8080/aop/test
    sleep 0.1
  done
  echo "---"
done
```

### Burst Test
```bash
#!/bin/bash
echo "Testing burst endpoint:"
for i in {1..15}; do
  curl -s -w "HTTP Status: %{http_code}\n" \
    -H "X-Client-Id: burst-test" \
    http://localhost:8080/aop/burst
  sleep 0.05
done
```

## 📊 Monitoring Endpoints

### Prometheus Metrics
```bash
curl http://localhost:8080/actuator/prometheus
```

### Application Info
```bash
curl http://localhost:8080/actuator/info
```

## 🔍 Debugging

### Enable Debug Logging
```properties
# application.properties
logging.level.com.project.RateLimiter=DEBUG
logging.level.org.springframework.data.redis=DEBUG
```

### Redis Debugging
```bash
# Monitor Redis operations
redis-cli monitor | grep "rate:"

# Check rate limit keys
redis-cli keys "rate:*"

# Get specific key data
redis-cli hgetall "rate:test-client:/aop/test"
```

### Application Logs
```bash
# Tail application logs
tail -f logs/application.log | grep "RateLimit"

# Filter by client
tail -f logs/application.log | grep "test-client"
```

## 📈 Performance Considerations

### Rate Limiting Overhead
- **Redis Operations**: ~1-2ms per request
- **In-Memory Fallback**: ~0.1ms per request
- **AOP Overhead**: ~0.05ms per request

### Recommended Limits
- **High-Frequency APIs**: 100-1000 requests/second
- **Medium-Frequency APIs**: 10-100 requests/second
- **Low-Frequency APIs**: 1-10 requests/second

### Scaling Guidelines
- **Single Instance**: Up to 10,000 requests/second
- **Multiple Instances**: Scale horizontally with Redis cluster
- **Redis Cluster**: Use Redis Sentinel for high availability

## 🚨 Error Responses

### Common Error Codes

| Status Code | Description |
|-------------|-------------|
| `200 OK` | Request successful |
| `429 Too Many Requests` | Rate limit exceeded |
| `500 Internal Server Error` | Server error |

### Error Response Format
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 429,
  "error": "Rate Limit Exceeded",
  "message": "Rate limit exceeded for key: rate:test-client:/aop/test",
  "path": "/aop/test"
}
```

## 🔧 Configuration

### Application Properties
```properties
# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=500ms

# Monitoring
management.endpoints.web.exposure.include=prometheus
management.endpoint.prometheus.enabled=true
```

### Environment Variables
```bash
# Redis Configuration
export SPRING_REDIS_HOST=your-redis-host
export SPRING_REDIS_PORT=6379
export SPRING_REDIS_PASSWORD=your-password

# Application Configuration
export SERVER_PORT=8080
export LOGGING_LEVEL_COM_PROJECT_RATELIMITER=INFO
```

---

*This API documentation provides comprehensive information for integrating with the Rate Limiter application.* 