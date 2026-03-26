package com.mentoria.backend.security;

import com.mentoria.backend.exception.RateLimitException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filtro simples de Rate Limiting em memória para endpoints de autenticação.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, RateLimitInfo> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 10;
    private static final long TIME_WINDOW_MS = 60000; // 1 minuto

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/")) {
            String clientIp = getClientIp(request);
            long currentTime = System.currentTimeMillis();

            RateLimitInfo info = requestCounts.compute(clientIp, (k, v) -> {
                if (v == null || currentTime - v.startTime > TIME_WINDOW_MS) {
                    return new RateLimitInfo(currentTime, new AtomicInteger(1));
                }
                v.count.incrementAndGet();
                return v;
            });

            if (info.count.get() > MAX_REQUESTS) {
                response.setStatus(429);
                response.getWriter().write(
                        "{\"status\":\"error\",\"message\":\"Muitas tentativas. Tente novamente em 1 minuto.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private static class RateLimitInfo {
        long startTime;
        AtomicInteger count;

        RateLimitInfo(long startTime, AtomicInteger count) {
            this.startTime = startTime;
            this.count = count;
        }
    }
}
