package com.project.RateLimiter.strategy;

import java.util.concurrent.atomic.AtomicLong;

public class InMemoryTokenBucket {
    private final long maxTokens;
    private final long refillRate;
    private final long refillIntervalMs;
    private double tokens;
    private long lastRefillTimestamp;
    private final Object lock = new Object();

    public InMemoryTokenBucket(long maxTokens, long refillRate, long refillIntervalMs) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.refillIntervalMs = refillIntervalMs;
        this.tokens = maxTokens;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    public boolean isAllowed() {
        synchronized (lock) {
            refill();
            if (tokens >= 1) {
                tokens -= 1;
                return true;
            } else {
                return false;
            }
        }
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTimestamp;
        if (elapsed > 0) {
            double tokensToAdd = (elapsed / (double) refillIntervalMs) * refillRate;
            if (tokensToAdd > 0) {
                tokens = Math.min(maxTokens, tokens + tokensToAdd);
                lastRefillTimestamp = now;
            }
        }
    }
} 