package com.project.RateLimiter.strategy;

public interface RateLimitingStrategy {
    boolean isAllowed(String key);
}

