package com.project.RateLimiter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateLimitConfig {
    private int maxTokens;
    private int refillRate;
    private int refillIntervalMs;
    private int graceLimit;

}
