package com.project.RateLimiter.strategy;

import com.project.RateLimiter.dto.RateLimitConfig;

public interface RateLimitingStrategy {
    boolean isAllowed(String clientId, RateLimitConfig config);
}

