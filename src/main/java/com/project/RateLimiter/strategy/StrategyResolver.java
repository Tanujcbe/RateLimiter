    package com.project.RateLimiter.strategy;

    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.stereotype.Component;

    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import io.github.resilience4j.circuitbreaker.CircuitBreaker;

    @Slf4j
    @Component
    public class StrategyResolver {

        private final Map<String, RateLimitingStrategy> strategyMap;

        @Autowired
        @Qualifier("token_bucket")
        private RateLimitingStrategy redisStrategy;

        // InMemoryTokenBucket is not a RateLimitingStrategy, so we wrap it
        private final RateLimitingStrategy inMemoryStrategy = new RateLimitingStrategy() {
            private final java.util.concurrent.ConcurrentHashMap<String, com.project.RateLimiter.strategy.InMemoryTokenBucket> buckets = new java.util.concurrent.ConcurrentHashMap<>();
            @Override
            public boolean isAllowed(String clientId, String apiPath) {
                String key = "rate:" + clientId + ":" + apiPath;
                // Use default values or fetch config as needed
                com.project.RateLimiter.strategy.InMemoryTokenBucket bucket = buckets.computeIfAbsent(key, k -> new com.project.RateLimiter.strategy.InMemoryTokenBucket(10, 1, 10000));
                return bucket.isAllowed();
            }
        };
    
        @Autowired
        private CircuitBreaker redisCircuitBreaker;

        @Autowired
        public StrategyResolver(List<RateLimitingStrategy> strategies) {
            strategyMap = new HashMap<>();
            for (RateLimitingStrategy strategy : strategies) {
                String name = strategy.getClass().getAnnotation(Component.class).value();
                strategyMap.put(name, strategy);
            }
        }

        public RateLimitingStrategy resolve(String strategyName) {
            if (redisCircuitBreaker.getState() == CircuitBreaker.State.OPEN) {
                log.warn("Circuit breaker OPEN. Using in-memory fallback strategy.");
                return inMemoryStrategy;
            }
            return (clientId, apiPath) -> {
                try {
                    return redisCircuitBreaker.executeSupplier(() -> redisStrategy.isAllowed(clientId, apiPath));
                } catch (Exception e) {
                    log.warn("Redis unavailable, falling back to in-memory rate limiting: {}", e.getMessage());
                    return inMemoryStrategy.isAllowed(clientId, apiPath);
                }
            };
        }
    }
