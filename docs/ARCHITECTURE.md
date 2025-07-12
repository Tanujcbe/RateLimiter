# Rate Limiter - Architecture Overview

## 🏗️ System Design

### High-Level Architecture
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

## 🧩 Core Components

### 1. Controllers Layer
- **Purpose**: REST API endpoints
- **Key Classes**: `TestController`, `RateLimitConfigController`
- **Responsibilities**: Request handling, response formatting

### 2. Rate Limiting Layer
- **Token Bucket Strategy**: Core algorithm implementation
- **AOP Aspects**: Method-level rate limiting
- **Interceptors**: Request-level rate limiting
- **Fallback Mechanism**: In-memory implementation

### 3. Configuration Layer
- **Dynamic Configuration**: Redis-based configuration
- **Fallback Configs**: Default configurations
- **Environment Support**: Dev/Prod configurations

## 🔄 Data Flow

### Request Processing
```
1. HTTP Request
   ↓
2. RateLimitInterceptor (Optional)
   ↓
3. Controller Method
   ↓
4. @RateLimit Annotation (AOP)
   ↓
5. TokenBucketStrategy
   ↓
6. Redis Lua Script
   ↓
7. Response
```

### Rate Limiting Decision
```
1. Generate Rate Limit Key
   ↓
2. Load Configuration
   ↓
3. Check Token Bucket
   ├─ Redis Available → Lua Script
   └─ Redis Unavailable → In-Memory Fallback
   ↓
4. Token Available?
   ├─ Yes → Consume Token → Allow
   └─ No → Rate Limit Exceeded → Block
```

## 🗄️ Data Storage

### Redis Data Structure
```
Key Pattern: rate:<client-id>:<api-path>
Example: rate:test-client-123:/aop/test

Data Structure (Hash):
{
  "tokens": "3",
  "grace_tokens": "2", 
  "last_refill": "1640995200000"
}
```

### Configuration Storage
```
Key Pattern: config:<api-path>
Example: config:/aop/test

Data Structure (Hash):
{
  "maxTokens": "5",
  "refillRate": "1",
  "refillIntervalMs": "60000",
  "graceLimit": "2"
}
```

## ⚡ Performance Design

### Redis Optimization
- **Lua Scripts**: Atomic operations for consistency
- **Connection Pooling**: Efficient Redis connections
- **Key Expiration**: Automatic cleanup
- **Batch Operations**: Minimize round trips

### Memory Management
- **ConcurrentHashMap**: Thread-safe in-memory storage
- **Key Expiration**: Automatic cleanup of fallback buckets
- **Memory Monitoring**: Track usage

### Scalability
- **Horizontal Scaling**: Multiple application instances
- **Redis Clustering**: Distributed rate limiting
- **Load Balancing**: Distribute requests

## 🔒 Security Considerations

### Rate Limit Key Generation
```java
String key = "rate:" + clientId + ":" + apiPath;
```

### Input Validation
- Request validation for client IDs and API paths
- Configuration validation for rate limit parameters
- Secure error messages

### Access Control
- Client ID validation
- API path validation
- Configuration access control

## 🚨 Error Handling

### Exception Hierarchy
```
RateLimitExceededException
├── Rate limit exceeded
└── Grace period exceeded

GlobalExceptionHandler
├── Handle rate limit exceptions
├── Log errors
└── Return appropriate HTTP status
```

### Fallback Mechanisms
- **Redis Fallback**: In-memory implementation
- **Configuration Fallback**: Default configurations
- **Grace Tokens**: Burst handling

## 📊 Monitoring & Observability

### Metrics
- Rate limit success/failure rates
- Redis connection status
- Application performance metrics

### Health Checks
- Application health status
- Redis connection status
- Rate limiter functionality

### Logging
- Structured logging with JSON format
- Different log levels (DEBUG, INFO, WARN, ERROR)
- Context information for debugging

## 🔧 Configuration Management

### Dynamic Configuration
```java
RateLimitConfig {
    int maxTokens;        // Maximum bucket capacity
    int refillRate;       // Tokens per refill interval
    int refillIntervalMs; // Refill interval in milliseconds
    int graceLimit;       // Additional grace tokens
}
```

### Configuration Sources
1. **Redis Configuration**: Primary source
2. **Fallback Map**: Default configurations
3. **Default Configuration**: System-wide defaults

## 🧪 Testing Strategy

### Unit Testing
- Component-level testing
- Strategy algorithm testing
- Configuration testing

### Integration Testing
- Redis integration testing
- HTTP request/response testing
- AOP functionality testing

### Performance Testing
- Load testing under high traffic
- Stress testing system limits
- Fallback scenario testing

## 🔄 Deployment Architecture

### Single Instance
```
┌─────────────────┐
│   Application   │
│   (Port 8080)   │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│     Redis       │
│   (Port 6379)   │
└─────────────────┘
```

### Multi-Instance
```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   Instance 1    │  │   Instance 2    │  │   Instance 3    │
│   (Port 8080)   │  │   (Port 8081)   │  │   (Port 8082)   │
└─────────────────┘  └─────────────────┘  └─────────────────┘
         │                     │                     │
         └─────────────────────┼─────────────────────┘
                               ▼
                    ┌─────────────────┐
                    │   Redis Cluster │
                    │   (Port 6379)   │
                    └─────────────────┘
```

## 📈 Scalability Considerations

### Horizontal Scaling
- **Stateless Design**: Application instances are stateless
- **Shared Redis**: All instances share Redis cluster
- **Load Balancing**: Distribute requests across instances

### Vertical Scaling
- **Connection Pooling**: Optimize Redis connections
- **Memory Optimization**: Efficient in-memory storage
- **CPU Optimization**: Efficient algorithms

### Performance Tuning
- **Redis Optimization**: Tune Redis configuration
- **Application Tuning**: Optimize Spring Boot settings
- **Network Tuning**: Optimize network configuration

## 🔮 Future Enhancements

### Planned Features
- Rate limit analytics dashboard
- Dynamic rule configuration
- Multi-tenant support
- Rate limit templates

### Technical Improvements
- Redis Sentinel for high availability
- Circuit breaker patterns
- Advanced caching strategies
- API gateway integration

---

*This architecture demonstrates scalable design patterns, distributed systems concepts, and production-ready implementation strategies.* 