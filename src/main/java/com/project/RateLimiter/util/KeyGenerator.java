package com.project.RateLimiter.util;

import jakarta.servlet.http.HttpServletRequest;

public class KeyGenerator {
    public static String generateKey(HttpServletRequest request) {
        // Example extraction logic; adapt as needed for your context
        String userId = (String) request.getAttribute("userId"); // or from session/token
        String clientId = request.getHeader("X-Client-Id");
        String ip = request.getRemoteAddr();
        String apiPath = request.getRequestURI();

        // Treat localhost/loopback addresses as if no IP is available
        if (ip != null && (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("localhost"))) {
            ip = null;
        }

        if (userId != null && apiPath != null) {
            return String.format("rate:user:%s:%s", userId, apiPath);
        } else if (clientId != null && apiPath != null) {
            return String.format("rate:client:%s:%s", clientId, apiPath);
//        } else if (ip != null && apiPath != null) { // commented to check only apiPath without IP
//            return String.format("rate:ip:%s:%s", ip, apiPath);
        } else if (apiPath != null) {
            return String.format("rate:api:%s", apiPath);
        } else {
            return "rate:global";
        }
    }
} 