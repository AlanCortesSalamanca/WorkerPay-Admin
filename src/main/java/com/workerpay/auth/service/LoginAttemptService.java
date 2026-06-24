package com.workerpay.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private final Map<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public void recordFailure(String username) {
        String key = key(username);
        if (key == null) {
            return;
        }
        Instant now = Instant.now();
        attempts.compute(key, (ignored, current) -> {
            if (current != null && current.isBlocked(now)) {
                return current;
            }
            int failedAttempts = current == null ? 1 : current.failedAttempts() + 1;
            Instant blockedUntil = failedAttempts >= MAX_FAILED_ATTEMPTS ? now.plus(BLOCK_DURATION) : null;
            return new LoginAttempt(failedAttempts, blockedUntil);
        });
    }

    public void recordSuccess(String username) {
        String key = key(username);
        if (key != null) {
            attempts.remove(key);
        }
    }

    public boolean isBlocked(String username) {
        String key = key(username);
        if (key == null) {
            return false;
        }
        LoginAttempt attempt = attempts.get(key);
        if (attempt == null || attempt.blockedUntil() == null) {
            return false;
        }
        boolean blocked = attempt.isBlocked(Instant.now());
        if (!blocked) {
            attempts.remove(key);
        }
        return blocked;
    }

    private String key(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private record LoginAttempt(int failedAttempts, Instant blockedUntil) {

        private boolean isBlocked(Instant now) {
            return blockedUntil != null && blockedUntil.isAfter(now);
        }
    }
}
