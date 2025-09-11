package com.ekomobil.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basit bir sliding window rate limiter (IP+email başına).
 * Prod'da Redis/Bucket4j önerilir.
 */
@Component
public class ResetRateLimiter {

    private static final int MAX_REQUESTS_PER_MINUTE = 10;

    private static class Counter {
        int count;
        Instant windowStart;
    }

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public boolean allow(String key) {
        final Instant now = Instant.now();
        Counter c = counters.computeIfAbsent(key, k -> {
            Counter x = new Counter(); x.windowStart = now; x.count = 0; return x;
        });

        synchronized (c) {
            if (now.isAfter(c.windowStart.plusSeconds(60))) {
                c.windowStart = now;
                c.count = 0;
            }
            if (c.count >= MAX_REQUESTS_PER_MINUTE) return false;
            c.count++;
            return true;
        }
    }
}
