package com.project.RateLimiter.strategy;

import java.util.concurrent.atomic.AtomicLong;

public class InMemoryTokenBucket {
    private final long maxTokens;
    private final long graceTokens;
    private final long refillRate;
    private final long refillIntervalMs;
    private double tokens;
    private long graceTokensRemaining;
    private long lastRefillTimestamp;
    private final Object lock = new Object();

    public InMemoryTokenBucket(long maxTokens, long graceTokens, long refillRate, long refillIntervalMs) {
        this.maxTokens = maxTokens;
        this.graceTokens = graceTokens;
        this.refillRate = refillRate;
        this.refillIntervalMs = refillIntervalMs;
        this.tokens = maxTokens;
        this.graceTokensRemaining = graceTokens;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    // Constructor for backward compatibility
    public InMemoryTokenBucket(long maxTokensWithGrace, long refillRate, long refillIntervalMs) {
        this(maxTokensWithGrace, 0, refillRate, refillIntervalMs);
    }

    public boolean isAllowed() {
        synchronized (lock) {
            refill();
            if (tokens >= 1) {
                // Consume 1 normal token
                tokens -= 1;
                return true;
            } else if (graceTokensRemaining > 0) {
                // Consume 1 grace token
                graceTokensRemaining -= 1;
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