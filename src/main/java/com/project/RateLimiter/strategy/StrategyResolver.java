package com.project.RateLimiter.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StrategyResolver {

    private final Map<String, RateLimitingStrategy> strategyMap;

    @Autowired
    public StrategyResolver(List<RateLimitingStrategy> strategies) {
        strategyMap = new HashMap<>();
        for (RateLimitingStrategy strategy : strategies) {
            String name = strategy.getClass().getAnnotation(Component.class).value();
            strategyMap.put(name, strategy);
        }
    }

    public RateLimitingStrategy resolve(String strategyName) {
        RateLimitingStrategy strategy = strategyMap.get(strategyName);
        if (strategy != null) {
            log.info("Resolved rate limiting strategy: {}", strategyName);
            return strategy;
        } else {
            log.warn("Strategy {} not found, falling back to token_bucket", strategyName);
            return strategyMap.get("token_bucket");
        }
    }
}
