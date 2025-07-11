# High-Level Design (HLD): RateLimiter System

## Overview
This document describes the high-level architecture and relationships between the main components of the RateLimiter system.

## Mermaid Diagram
```mermaid
flowchart TD
    A["Client"] -->|HTTP Request| B["Spring Boot Web Layer"]
    B --> C["RateLimitInterceptor"]
    C -->|Resolve Strategy| D["StrategyResolver"]
    D -->|If Redis OK| E["TokenBucketStrategy (Redis)"]
    D -->|If Redis Down| F["InMemoryTokenBucket"]
    E -->|Fetch Config| G["RateLimitConfigService"]
    F -->|Fetch Config| G
    E -->|Run Lua| H["Redis (token_bucket.lua)"]
    C -->|If Allowed| I["Controller (TestController)"]
    C -->|If Throttled| J["429 Response"]
    G -->|Config Data| E
    G -->|Config Data| F
    E -->|Fallback| F
    subgraph Config
      G
      H
    end
    subgraph API
      I
    end
    style H fill:#f9f,stroke:#333,stroke-width:2
    style F fill:#bbf,stroke:#333,stroke-width:2
    style E fill:#bfb,stroke:#333,stroke-width:2
    style D fill:#ffd,stroke:#333,stroke-width:2
    style C fill:#ffd,stroke:#333,stroke-width:2
    style B fill:#ffd,stroke:#333,stroke-width:2
    style A fill:#fff,stroke:#333,stroke-width:2
    style J fill:#faa,stroke:#333,stroke-width:2
    style I fill:#afa,stroke:#333,stroke-width:2
```

## Textual Summary
- **Client** sends HTTP requests to the application.
- **Spring Boot Web Layer** receives the request.
- **RateLimitInterceptor** intercepts each request to enforce rate limiting.
- **StrategyResolver** determines which rate limiting strategy to use:
    - **TokenBucketStrategy (Redis)** is used if Redis is available.
    - **InMemoryTokenBucket** is used as a fallback if Redis is down or the circuit breaker is open.
- **RateLimitConfigService** provides rate limit configuration for each client and API path.
- **TokenBucketStrategy** executes a Lua script (**token_bucket.lua**) in Redis to enforce rate limits.
- If the request is allowed, it proceeds to the **Controller** (e.g., TestController).
- If the request is throttled, a **429 Response** is returned to the client. 