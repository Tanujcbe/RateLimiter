package com.project.RateLimiter.config;

import com.project.RateLimiter.aspect.RateLimitAspect;
import com.project.RateLimiter.strategy.TokenBucketStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration class for rate limiting components.
 * Provides default beans for RateLimitAspect that can be overridden by the application.
 */
@Configuration
public class RateLimitAutoConfiguration {

    /**
     * Creates a RateLimitAspect bean if one doesn't already exist.
     * 
     * @param tokenBucketStrategy the token bucket strategy for rate limiting operations
     * @return a new RateLimitAspect instance
     */
    @Bean
    @ConditionalOnMissingBean(RateLimitAspect.class)
    public RateLimitAspect rateLimitAspect(TokenBucketStrategy tokenBucketStrategy) {
        return new RateLimitAspect(tokenBucketStrategy);
    }
} 