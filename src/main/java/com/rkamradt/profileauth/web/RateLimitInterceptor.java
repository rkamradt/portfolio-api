package com.rkamradt.profileauth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int AUTH_LIMIT = 5;
    private static final int DEFAULT_LIMIT = 30;
    private static final long WINDOW_MS = 60_000L;

    private final ConcurrentHashMap<String, WindowState> windows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String ip = extractIp(request);
        String path = request.getRequestURI();
        boolean isAuth = path.startsWith("/api/auth/");
        int limit = isAuth ? AUTH_LIMIT : DEFAULT_LIMIT;
        String prefix = isAuth ? "auth" : "other";
        String key = ip + ":" + prefix;

        long now = System.currentTimeMillis();
        WindowState state = windows.compute(key, (k, existing) -> {
            if (existing == null || (now - existing.windowStart) >= WINDOW_MS) {
                return new WindowState(now, 1);
            }
            existing.count++;
            return existing;
        });

        if (state.count > limit) {
            response.setContentType("application/json");
            response.setStatus(429);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Too many requests"));
            return false;
        }

        return true;
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class WindowState {
        long windowStart;
        int count;

        WindowState(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
