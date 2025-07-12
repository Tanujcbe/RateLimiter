# Rate Limiter - Deployment Guide

## 📋 Overview

This guide provides comprehensive instructions for deploying the Rate Limiter application in various environments, from local development to production clusters.

## 🏗️ Deployment Architecture

### Single Instance Deployment

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

### Multi-Instance Deployment

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

## 🚀 Local Development Deployment

### Prerequisites

- Java 17+
- Maven 3.6+
- Redis 6.0+

### Step 1: Install Dependencies

```bash
# Install Java 17
# macOS
brew install openjdk@17

# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# Windows
# Download from https://adoptium.net/
```

```bash
# Install Maven
# macOS
brew install maven

# Ubuntu/Debian
sudo apt install maven

# Windows
# Download from https://maven.apache.org/
```

```bash
# Install Redis
# macOS
brew install redis

# Ubuntu/Debian
sudo apt install redis-server

# Windows
# Download from https://redis.io/
```

### Step 2: Start Redis

```bash
# Start Redis server
redis-server

# Or using Docker
docker run -d -p 6379:6379 --name redis-rate-limiter redis:latest
```

### Step 3: Build and Run Application

```bash
# Clone repository
git clone <repository-url>
cd RateLimiter

# Build application
mvn clean install

# Run application
mvn spring-boot:run
```

### Step 4: Verify Deployment

```bash
# Check application health
curl http://localhost:8080/health

# Check rate limiter status
curl http://localhost:8080/rate-limit/status

# Test rate limiting
curl -H "X-Client-Id: test-client" http://localhost:8080/aop/test
```

## 🐳 Docker Deployment

### Single Container Deployment

#### Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean install -DskipTests

# Create runtime image
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy built JAR
COPY --from=0 /app/target/RateLimiter-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run application
CMD ["java", "-jar", "app.jar"]
```

#### Docker Compose

```yaml
version: '3.8'

services:
  redis:
    image: redis:latest
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    command: redis-server --appendonly yes

  rate-limiter:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
    depends_on:
      - redis
    restart: unless-stopped

volumes:
  redis-data:
```

#### Deployment Commands

```bash
# Build and run with Docker Compose
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f rate-limiter

# Stop services
docker-compose down
```

### Multi-Container Deployment

#### Docker Compose with Multiple Instances

```yaml
version: '3.8'

services:
  redis:
    image: redis:latest
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    command: redis-server --appendonly yes

  rate-limiter-1:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
      - SERVER_PORT=8080
    depends_on:
      - redis
    restart: unless-stopped

  rate-limiter-2:
    build: .
    ports:
      - "8081:8080"
    environment:
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
      - SERVER_PORT=8080
    depends_on:
      - redis
    restart: unless-stopped

  rate-limiter-3:
    build: .
    ports:
      - "8082:8080"
    environment:
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
      - SERVER_PORT=8080
    depends_on:
      - redis
    restart: unless-stopped

  nginx:
    image: nginx:latest
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - rate-limiter-1
      - rate-limiter-2
      - rate-limiter-3
    restart: unless-stopped

volumes:
  redis-data:
```

#### Nginx Configuration

```nginx
events {
    worker_connections 1024;
}

http {
    upstream rate_limiter {
        server rate-limiter-1:8080;
        server rate-limiter-2:8080;
        server rate-limiter-3:8080;
    }

    server {
        listen 80;
        
        location / {
            proxy_pass http://rate_limiter;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

## ☁️ Cloud Deployment

### AWS Deployment

#### EC2 Deployment

```bash
# Launch EC2 instance
aws ec2 run-instances \
  --image-id ami-0c02fb55956c7d316 \
  --instance-type t3.medium \
  --key-name your-key-pair \
  --security-group-ids sg-xxxxxxxxx \
  --subnet-id subnet-xxxxxxxxx

# Connect to instance
ssh -i your-key.pem ec2-user@your-instance-ip

# Install dependencies
sudo yum update -y
sudo yum install java-17-amazon-corretto -y
sudo yum install maven -y

# Install Redis
sudo yum install redis -y
sudo systemctl start redis
sudo systemctl enable redis

# Deploy application
git clone <repository-url>
cd RateLimiter
mvn clean install
nohup java -jar target/RateLimiter-0.0.1-SNAPSHOT.jar &
```

#### ECS Deployment

```yaml
# task-definition.json
{
  "family": "rate-limiter",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "executionRoleArn": "arn:aws:iam::account:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "rate-limiter",
      "image": "your-account.dkr.ecr.region.amazonaws.com/rate-limiter:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_REDIS_HOST",
          "value": "your-redis-endpoint"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/rate-limiter",
          "awslogs-region": "us-west-2",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

### Google Cloud Platform Deployment

#### GKE Deployment

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rate-limiter
spec:
  replicas: 3
  selector:
    matchLabels:
      app: rate-limiter
  template:
    metadata:
      labels:
        app: rate-limiter
    spec:
      containers:
      - name: rate-limiter
        image: gcr.io/your-project/rate-limiter:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_REDIS_HOST
          value: "redis-service"
        - name: SPRING_REDIS_PORT
          value: "6379"
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: rate-limiter-service
spec:
  selector:
    app: rate-limiter
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

### Azure Deployment

#### Azure Container Instances

```yaml
# azure-deployment.yaml
apiVersion: 2019-12-01
location: eastus
name: rate-limiter
properties:
  containers:
  - name: rate-limiter
    properties:
      image: your-registry.azurecr.io/rate-limiter:latest
      ports:
      - port: 8080
      environmentVariables:
      - name: SPRING_REDIS_HOST
        value: your-redis-host
      - name: SPRING_REDIS_PORT
        value: "6379"
      resources:
        requests:
          memoryInGB: 0.5
          cpu: 0.5
        limits:
          memoryInGB: 1.0
          cpu: 1.0
  osType: Linux
  restartPolicy: Always
  ipAddress:
    type: Public
    ports:
    - protocol: tcp
      port: 8080
```

## 🔧 Configuration Management

### Environment-Specific Configuration

#### Development Environment

```properties
# application-dev.properties
spring.application.name=RateLimiter
spring.profiles.active=dev

# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=500ms

# Logging
logging.level.com.project.RateLimiter=DEBUG
logging.level.org.springframework.data.redis=DEBUG

# Monitoring
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.health.show-details=always
```

#### Production Environment

```properties
# application-prod.properties
spring.application.name=RateLimiter
spring.profiles.active=prod

# Redis Configuration
spring.redis.host=${REDIS_HOST:localhost}
spring.redis.port=${REDIS_PORT:6379}
spring.redis.password=${REDIS_PASSWORD:}
spring.redis.timeout=200ms
spring.redis.lettuce.pool.max-active=8
spring.redis.lettuce.pool.max-idle=4
spring.redis.lettuce.pool.min-idle=2

# Logging
logging.level.com.project.RateLimiter=INFO
logging.level.org.springframework.data.redis=WARN
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Monitoring
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.health.show-details=when-authorized
management.endpoint.health.roles=ADMIN

# Security
server.servlet.session.timeout=30m
```

### Configuration via Environment Variables

```bash
# Application Configuration
export SPRING_PROFILES_ACTIVE=prod
export SERVER_PORT=8080

# Redis Configuration
export SPRING_REDIS_HOST=your-redis-host
export SPRING_REDIS_PORT=6379
export SPRING_REDIS_PASSWORD=your-redis-password

# JVM Configuration
export JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# Application Configuration
export RATE_LIMITER_DEFAULT_CAPACITY=10
export RATE_LIMITER_DEFAULT_REFILL_RATE=1
export RATE_LIMITER_DEFAULT_GRACE_LIMIT=2
```

## 📊 Monitoring and Observability

### Health Checks

```bash
# Application health
curl http://localhost:8080/health

# Rate limiter status
curl http://localhost:8080/rate-limit/status

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

### Logging Configuration

```properties
# application.properties
logging.level.root=INFO
logging.level.com.project.RateLimiter=INFO
logging.level.org.springframework.data.redis=WARN
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.file.name=logs/application.log
logging.file.max-size=100MB
logging.file.max-history=30
```

### Metrics Collection

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'rate-limiter'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
```

## 🔒 Security Configuration

### SSL/TLS Configuration

```properties
# application.properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=your-password
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
```

### Authentication and Authorization

```java
// Security configuration
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/health", "/actuator/health").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic();
        return http.build();
    }
}
```

## 🚨 Troubleshooting

### Common Issues

#### Redis Connection Issues

```bash
# Check Redis connectivity
redis-cli ping

# Check Redis logs
sudo journalctl -u redis

# Test Redis connection from application
curl -H "X-Client-Id: test" http://localhost:8080/aop/test
```

#### Application Startup Issues

```bash
# Check application logs
tail -f logs/application.log

# Check JVM memory
jstat -gc <pid>

# Check system resources
top -p <pid>
```

#### Performance Issues

```bash
# Monitor Redis performance
redis-cli info memory
redis-cli info stats

# Monitor application performance
curl http://localhost:8080/actuator/prometheus | grep rate_limiter

# Check network connectivity
netstat -an | grep 6379
```

### Debug Commands

```bash
# Enable debug logging
export LOGGING_LEVEL_COM_PROJECT_RATELIMITER=DEBUG

# Check Redis keys
redis-cli keys "rate:*"

# Monitor Redis operations
redis-cli monitor | grep "rate:"

# Check application metrics
curl http://localhost:8080/actuator/metrics/rate.limiter.requests
```

## 🔄 Deployment Automation

### CI/CD Pipeline

```yaml
# .github/workflows/deploy.yml
name: Deploy Rate Limiter

on:
  push:
    branches: [ main ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build with Maven
      run: mvn clean install
    
    - name: Build Docker image
      run: docker build -t rate-limiter .
    
    - name: Deploy to production
      run: |
        # Deploy to your cloud platform
        # Example for AWS ECS
        aws ecs update-service --cluster production --service rate-limiter --force-new-deployment
```

### Infrastructure as Code

```yaml
# docker-compose.prod.yml
version: '3.8'

services:
  redis:
    image: redis:latest
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD}
    restart: unless-stopped

  rate-limiter:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
      - SPRING_REDIS_PASSWORD=${REDIS_PASSWORD}
      - JAVA_OPTS=-Xms512m -Xmx1024m
    depends_on:
      - redis
    restart: unless-stopped
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 1G
          cpus: '0.5'
        reservations:
          memory: 512M
          cpus: '0.25'

volumes:
  redis-data:
```

---

*This deployment guide provides comprehensive instructions for deploying the Rate Limiter application in various environments and configurations.* 