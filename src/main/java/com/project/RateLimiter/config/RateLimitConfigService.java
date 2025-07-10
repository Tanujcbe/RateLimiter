package com.project.RateLimiter.config;

import com.project.RateLimiter.dto.RateLimitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class RateLimitConfigService {
    private final Map<String, RateLimitConfig> apiConfigMap = Map.of(
            "/ping", new RateLimitConfig(10, 1, 60000)
    );

    public RateLimitConfig getConfig(String apiPath) {
        RateLimitConfig config = apiConfigMap.getOrDefault(apiPath, new RateLimitConfig(5, 1, 60000));
        log.debug("Fetched rate limit config for path {}: {}", apiPath, config);
        return config;
    }
}
