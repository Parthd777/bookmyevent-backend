package com.bookmyevent.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Throttles /auth/login per client IP so stolen-password guessing is not free.
 * In-memory and per-instance: behind multiple replicas use a shared store instead.
 */
@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 15 * 60 * 1000L;
    private static final int MAX_TRACKED_CLIENTS = 10_000;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    private static final class Window {
        private long startMs;
        private int count;

        Window(long startMs) {
            this.startMs = startMs;
            this.count = 1;
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod())
                && "/auth/login".equals(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        long now = System.currentTimeMillis();
        evictIfOversized(now);

        // Uses the socket address, not X-Forwarded-For, which a client can forge.
        String clientKey = request.getRemoteAddr();

        if (isBlocked(clientKey, now)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(WINDOW_MS / 1000));
            response.getWriter().write("{\"error\":\"Too many login attempts. Try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);

        // Only failures count, so a legitimate user is never locked out by signing in repeatedly.
        if (response.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
            recordFailure(clientKey, now);
        }
    }

    private boolean isBlocked(String clientKey, long now) {
        Window window = windows.get(clientKey);
        return window != null
                && now - window.startMs < WINDOW_MS
                && window.count >= MAX_ATTEMPTS;
    }

    private void recordFailure(String clientKey, long now) {
        windows.compute(clientKey, (key, existing) -> {
            if (existing == null || now - existing.startMs >= WINDOW_MS) {
                return new Window(now);
            }
            existing.count++;
            return existing;
        });
    }

    private void evictIfOversized(long now) {
        if (windows.size() < MAX_TRACKED_CLIENTS) {
            return;
        }
        windows.entrySet().removeIf(entry -> now - entry.getValue().startMs >= WINDOW_MS);
        if (windows.size() >= MAX_TRACKED_CLIENTS) {
            windows.clear();
        }
    }
}
