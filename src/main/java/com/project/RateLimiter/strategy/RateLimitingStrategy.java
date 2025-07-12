package com.project.RateLimiter.strategy;

import com.project.RateLimiter.dto.RateLimitConfig;
import jakarta.servlet.http.HttpServletRequest;

public interface RateLimitingStrategy {
    boolean isAllowed(HttpServletRequest request);
}

