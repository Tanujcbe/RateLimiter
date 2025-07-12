package com.project.RateLimiter.controller;

import com.project.RateLimiter.annotation.RateLimit;
import com.project.RateLimiter.exception.RateLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class TestController {
    
    // Basic test endpoints (interceptor-based rate limiting)
    @GetMapping("/test")
    public String test() {
        log.info("/test endpoint hit");
        return "Hello World";
    }
    
    @GetMapping("/apiTest")
    public String apiTest() {
        return "{Status : UP}";
    }
    
    @GetMapping("/ping")
    public String ping() {
        log.info("/ping endpoint hit");
        return "pong";
    }
    
    // AOP-based rate limiting test endpoints
    @RateLimit(capacity = 5, refillTokensPerSecond = 1, grace = 2)
    @GetMapping("/aop/test")
    public ResponseEntity<Map<String, Object>> aopTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "AOP Rate Limited Endpoint");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "success");
        log.info("AOP test endpoint hit");
        return ResponseEntity.ok(response);
    }
    
    @RateLimit(key = "strict", capacity = 3, refillTokensPerSecond = 1, grace = 0)
    @GetMapping("/aop/strict")
    public ResponseEntity<Map<String, Object>> aopStrict() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Strict AOP Rate Limited Endpoint");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "success");
        log.info("AOP strict endpoint hit");
        return ResponseEntity.ok(response);
    }
    
    @RateLimit(key = "burst", capacity = 10, refillTokensPerSecond = 2, grace = 5)
    @GetMapping("/aop/burst")
    public ResponseEntity<Map<String, Object>> aopBurst() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Burst AOP Rate Limited Endpoint");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "success");
        log.info("AOP burst endpoint hit");
        return ResponseEntity.ok(response);
    }
    
    // Grace token test endpoint
    @RateLimit(key = "grace", capacity = 2, refillTokensPerSecond = 1, grace = 3)
    @GetMapping("/aop/grace")
    public ResponseEntity<Map<String, Object>> aopGrace() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Grace Token Test Endpoint");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "success");
        log.info("AOP grace endpoint hit");
        return ResponseEntity.ok(response);
    }
    
    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "RateLimiter");
        return ResponseEntity.ok(response);
    }
    
    // Rate limit status endpoint
    @GetMapping("/rate-limit/status")
    public ResponseEntity<Map<String, Object>> rateLimitStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Rate Limiter Status");
        response.put("timestamp", LocalDateTime.now());
        response.put("features", Map.of(
            "interceptor_based", true,
            "aop_based", true,
            "grace_tokens", true,
            "redis_fallback", true
        ));
        return ResponseEntity.ok(response);
    }
    
    // Test endpoint for exception handling
    @RateLimit(capacity = 1, refillTokensPerSecond = 1, grace = 0)
    @GetMapping("/aop/exception-test")
    public ResponseEntity<Map<String, Object>> exceptionTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Exception Test Endpoint");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "success");
        log.info("AOP exception test endpoint hit");
        return ResponseEntity.ok(response);
    }
}
