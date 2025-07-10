package com.project.RateLimiter.interceptor;

import com.project.RateLimiter.config.RateLimitConfigService;
import com.project.RateLimiter.dto.RateLimitConfig;
import com.project.RateLimiter.strategy.RateLimitingStrategy;
import com.project.RateLimiter.strategy.StrategyResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private StrategyResolver strategyResolver;

    @Autowired
    private RateLimitConfigService configService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        log.info("API has been triggered of path {} with X-Client-Id {}",request.getPathInfo(),request.getHeader("X-Client-Id"));
        String clientId = request.getHeader("X-Client-Id");
        if (clientId == null) clientId = "anonymous";

        String apiPath = request.getRequestURI();
        String strategy = "token_bucket"; // For now, hardcoded
        RateLimitConfig config = configService.getConfig(apiPath);

        String key = "rate:" + clientId + ":" + apiPath;
        RateLimitingStrategy limiter = strategyResolver.resolve(strategy);

        if (!limiter.isAllowed(key,config)) {
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded");
            return false;
        }

        return true;
    }
}

