package com.project.RateLimiter.exception;

/**
 * Exception thrown when a rate limit is exceeded.
 * This is a runtime exception that can be thrown when the rate limiting
 * logic determines that a request should be blocked.
 */
public class RateLimitExceededException extends RuntimeException {
    
    /**
     * Constructs a new RateLimitExceededException with the specified detail message.
     * 
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public RateLimitExceededException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new RateLimitExceededException with the specified detail message and cause.
     * 
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
} 