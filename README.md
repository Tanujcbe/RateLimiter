# Rate Limiter - Spring Boot Application

A production-ready rate limiting solution built with Spring Boot, featuring token bucket algorithm, Redis integration, and dual rate limiting approaches.

## 🚀 Key Features

- **Token Bucket Algorithm**: Configurable rate limiting with burst handling
- **Dual Approaches**: AOP-based and interceptor-based rate limiting
- **Redis Integration**: Distributed rate limiting with Lua scripts
- **Grace Tokens**: Burst handling with additional tokens
- **Fallback Mechanism**: In-memory implementation when Redis is down
- **Dynamic Configuration**: Runtime configuration via Redis
- **Monitoring**: Prometheus metrics and health checks

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Controllers   │    │   Interceptors  │    │   AOP Aspects   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │  Token Bucket   │
                    │    Strategy     │
                    └─────────────────┘
                                 │
                    ┌─────────────────┐
                    │   Redis Store   │
                    │  (with Lua)     │
                    └─────────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Redis 6.0+

### Setup
```bash
# Clone and build
git clone <repository-url>
cd RateLimiter
mvn clean install

# Start Redis
redis-server

# Run application
mvn spring-boot:run
```

### Test
```bash
# Health check
curl http://localhost:8080/health

# Test rate limiting
curl -H "X-Client-Id: test-client" http://localhost:8080/aop/test

# Run comprehensive tests
./test_rate_limiter.sh
```

## 📚 API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Application health |
| `/rate-limit/status` | GET | Rate limiter status |
| `/aop/test` | GET | AOP-based rate limiting (5 tokens, 1/sec, 2 grace) |
| `/aop/strict` | GET | Strict rate limiting (3 tokens, 1/sec, 0 grace) |
| `/aop/burst` | GET | Burst handling (10 tokens, 2/sec, 5 grace) |
| `/ping` | GET | Interceptor-based rate limiting |

## ⚙️ Configuration

### Application Properties
```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=500ms
management.endpoints.web.exposure.include=prometheus
```

### Rate Limiting Configuration
```java
@RateLimit(capacity = 5, refillTokensPerSecond = 1, grace = 2)
@GetMapping("/aop/test")
public ResponseEntity<Map<String, Object>> aopTest() {
    // Method implementation
}
```

## 🧪 Testing

### Manual Testing
```bash
# Test basic rate limiting
for i in {1..10}; do
  curl -H "X-Client-Id: test-client" http://localhost:8080/aop/test
  sleep 0.1
done
```


## 🔧 Technical Implementation

### Token Bucket Algorithm
- **Max Tokens**: Bucket capacity
- **Refill Rate**: Tokens per interval
- **Grace Tokens**: Additional burst tokens
- **Redis Lua Script**: Atomic operations

### Rate Limiting Approaches
1. **AOP-Based**: Method-level with `@RateLimit` annotation
2. **Interceptor-Based**: Request-level rate limiting

### Key Components
- `TokenBucketStrategy`: Core algorithm implementation
- `RateLimitAspect`: AOP aspect for method-level rate limiting
- `RateLimitConfigService`: Dynamic configuration management
- `InMemoryTokenBucket`: Fallback implementation

## 📊 Monitoring

### Health Checks
```bash
curl http://localhost:8080/health
curl http://localhost:8080/rate-limit/status
```

### Prometheus Metrics
```bash
curl http://localhost:8080/actuator/prometheus
```

## 🚨 Error Handling

### Rate Limit Exceeded
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 429,
  "error": "Rate Limit Exceeded",
  "message": "Rate limit exceeded for key: rate:test-client:/aop/test"
}
```

## 🔍 Debugging

### Enable Debug Logging
```properties
logging.level.com.project.RateLimiter=DEBUG
logging.level.org.springframework.data.redis=DEBUG
```

### Redis Debugging
```bash
# Monitor Redis operations
redis-cli monitor | grep "rate:"

# Check rate limit keys
redis-cli keys "rate:*"
```

## 🐳 Docker Deployment

```yaml
# docker-compose.yml
version: '3.8'
services:
  redis:
    image: redis:latest
    ports:
      - "6379:6379"
  
  rate-limiter:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_REDIS_HOST=redis
    depends_on:
      - redis
```

## 🎯 Key Achievements

- **Production-Ready**: Comprehensive error handling and fallback mechanisms
- **Scalable**: Redis-based distributed rate limiting
- **Flexible**: Dual rate limiting approaches
- **Observable**: Prometheus metrics and health checks
- **Testable**: Comprehensive test coverage and automation
- **Maintainable**: Clean architecture and documentation

## 📁 Project Structure

```
src/main/java/com/project/RateLimiter/
├── annotation/          # @RateLimit annotation
├── aspect/             # AOP aspects
├── config/             # Configuration classes
├── controller/         # REST endpoints
├── dto/               # Data transfer objects
├── exception/          # Custom exceptions
├── strategy/          # Rate limiting strategies
└── util/              # Utility classes
```

---

**Built with Spring Boot 3.5.3, Java 17, and Redis**
