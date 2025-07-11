package com.project.RateLimiter.strategy;

import com.project.RateLimiter.config.RateLimitConfigService;
import com.project.RateLimiter.dto.RateLimitConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component("token_bucket")
public class TokenBucketStrategy implements RateLimitingStrategy {


    private final StringRedisTemplate redisTemplate;
    private final RateLimitConfigService rateLimitConfigService;

    private String luaScript;

    private final ConcurrentHashMap<String, InMemoryTokenBucket> fallbackBuckets = new ConcurrentHashMap<>();

    public TokenBucketStrategy(StringRedisTemplate redisTemplate, RateLimitConfigService rateLimitConfigService) {
        this.redisTemplate = redisTemplate;
        this.rateLimitConfigService = rateLimitConfigService;
    }

    @PostConstruct
    public void loadScript() throws IOException {
        try {
            luaScript = Files.readString(
                    Paths.get("src/main/resources/lua/token_bucket.lua"));
            log.info("Loaded Lua script for token bucket strategy");
        } catch (IOException e) {
            log.error("Failed to load Lua script for token bucket strategy", e);
            throw e;
        }
    }

    /**
     * @param clientId like "user123"
     * @param apiPath like "/test"
     */
    public boolean isAllowed(String clientId,String apiPath) {
        String redisKey = String.format("rate:%s:%s", clientId, apiPath);
        RateLimitConfig config = rateLimitConfigService.getConfig(clientId,apiPath);
        log.debug("Checking rate limit for key: {}, config: {}", redisKey, config);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);
        List<String> keys = Collections.singletonList(redisKey);
        long now = System.currentTimeMillis();
        try {
            Long result = redisTemplate.execute(
                    script,
                    keys,
                    String.valueOf(config.getMaxTokens()),
                    String.valueOf(config.getRefillRate()),
                    String.valueOf(config.getRefillIntervalMs()),
                    String.valueOf(now)
            );
            boolean allowed = result == 1L;
            log.info("Rate limit check for key {}: {}", redisKey, allowed ? "ALLOWED" : "DENIED");
            return allowed;
        } catch (Exception e) {
            log.error("Error executing rate limit script for key {}. Falling back to in-memory bucket.", redisKey, e);
            // In-memory fallback
            InMemoryTokenBucket bucket = fallbackBuckets.computeIfAbsent(redisKey, k ->
                new InMemoryTokenBucket(
                    config.getMaxTokens(),
                    config.getRefillRate(),
                    config.getRefillIntervalMs()
                )
            );
            boolean allowed = bucket.isAllowed();
            log.info("[Fallback] Rate limit check for key {}: {}", redisKey, allowed ? "ALLOWED" : "DENIED");
            return allowed;
        }
    }
}