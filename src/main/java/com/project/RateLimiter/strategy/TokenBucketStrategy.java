package com.project.RateLimiter.strategy;

import org.springframework.stereotype.Component;

@Component
public class TokenBucketStrategy implements RateLimitingStrategy {
    @Override
    public boolean isAllowed(String key) {
        return false;
    }
}

