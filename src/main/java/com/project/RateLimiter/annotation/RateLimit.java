package com.project.RateLimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for rate limiting methods.
 * This annotation can be applied to methods to enable rate limiting functionality.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * The key used for rate limiting. Defaults to empty string.
     * This key is used to identify the rate limit bucket.
     */
    String key() default "";
    
    /**
     * The maximum capacity of the rate limit bucket.
     * This represents the maximum number of tokens that can be stored.
     */
    int capacity();
    
    /**
     * The number of tokens refilled per second.
     * This determines how fast the bucket refills.
     */
    int refillTokensPerSecond();
    
    /**
     * Grace period in seconds. Defaults to 0.
     * During this period, requests are allowed even if the bucket is empty.
     */
    int grace() default 0;
} 