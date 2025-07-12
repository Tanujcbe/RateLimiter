package com.project.RateLimiter.config;

import com.project.RateLimiter.dto.RateLimitConfig;
import com.project.RateLimiter.util.KeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@Service
public class RateLimitConfigService {

    private final Map<String, RateLimitConfig> fallbackMap = Map.of(
            "/ping", new RateLimitConfig(10, 1, 60000, 2)
    );

    @Autowired
    private StringRedisTemplate redisTemplate;

    public RateLimitConfig getConfig(HttpServletRequest request) {
        String redisKey = KeyGenerator.generateKey(request);
        Map<Object, Object> configMap = redisTemplate.opsForHash().entries(redisKey);

        String apiPath = request.getRequestURI();
        if (configMap.isEmpty()) {
            log.warn("No Redis config found for key {}, falling back", redisKey);
            return fallbackMap.getOrDefault(apiPath, new RateLimitConfig(5, 1, 60000, 2));
        }

        try {
            int maxTokens = Integer.parseInt((String) configMap.getOrDefault("maxTokens", "5"));
            int refillRate = Integer.parseInt((String) configMap.getOrDefault("refillRate", "1"));
            int intervalMs = Integer.parseInt((String) configMap.getOrDefault("refillIntervalMs", "60000"));
            int graceLimit = Integer.parseInt((String) configMap.getOrDefault("graceLimit", "2"));
            RateLimitConfig config = new RateLimitConfig(maxTokens, refillRate, intervalMs, graceLimit);
            log.debug("Loaded dynamic rate config from Redis for key {}: {}", redisKey, config);
            return config;
        } catch (Exception e) {
            log.error("Invalid rate limit config in Redis for key {}: {}, falling back", redisKey, e.getMessage());
            return fallbackMap.getOrDefault(apiPath, new RateLimitConfig(5, 1, 60000, 2));
        }
    }
}

