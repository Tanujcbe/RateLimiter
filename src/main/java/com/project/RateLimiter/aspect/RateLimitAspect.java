package com.project.RateLimiter.aspect;

import com.project.RateLimiter.annotation.RateLimit;
import com.project.RateLimiter.exception.RateLimitExceededException;
import com.project.RateLimiter.strategy.TokenBucketStrategy;
import com.project.RateLimiter.util.KeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Aspect that intercepts methods annotated with @RateLimit and applies rate limiting.
 * Provides atomic rate limiting operations using the token bucket algorithm.
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private final TokenBucketStrategy tokenBucketStrategy;

    public RateLimitAspect(TokenBucketStrategy tokenBucketStrategy) {
        this.tokenBucketStrategy = tokenBucketStrategy;
    }

    /**
     * Intercepts methods annotated with @RateLimit and applies rate limiting.
     * 
     * @param joinPoint the method execution join point
     * @param rateLimit the @RateLimit annotation
     * @return the method result if rate limit is not exceeded
     * @throws RateLimitExceededException if rate limit is exceeded
     * @throws Throwable if the method execution fails
     */
    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        
        if (request == null) {
            log.warn("No HTTP request found in context, skipping rate limiting");
            return joinPoint.proceed();
        }

        String key = generateKey(request, rateLimit);
        log.debug("Checking rate limit for key: {} with annotation: {}", key, rateLimit);
        
        boolean allowed = tokenBucketStrategy.isAllowed(request);

        if (!allowed) {
            String message = String.format("Rate limit exceeded for key: %s", key);
            log.warn(message);
            throw new RateLimitExceededException(message);
        }

        log.debug("Rate limit check passed for key: {}", key);
        return joinPoint.proceed();
    }

    /**
     * Gets the current HTTP request from the request context.
     * 
     * @return the current HTTP request, or null if not available
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.debug("Could not get current request: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Generates a rate limiting key based on the request and annotation.
     * 
     * @param request the HTTP request
     * @param rateLimit the @RateLimit annotation
     * @return the rate limiting key
     */
    private String generateKey(HttpServletRequest request, RateLimit rateLimit) {
        String baseKey = KeyGenerator.generateKey(request);
        String annotationKey = rateLimit.key();
        
        if (annotationKey != null && !annotationKey.isEmpty()) {
            return baseKey + ":" + annotationKey;
        }
        
        return baseKey;
    }
} 