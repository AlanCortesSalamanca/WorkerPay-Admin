package com.workerpay.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LoginAttemptServiceTests {

    private final LoginAttemptService loginAttemptService = new LoginAttemptService();

    @Test
    void blocksUserAfterFiveFailedAttempts() {
        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailure("Admin");
        }

        assertThat(loginAttemptService.isBlocked("admin")).isTrue();
    }

    @Test
    void successfulLoginClearsFailures() {
        for (int i = 0; i < 4; i++) {
            loginAttemptService.recordFailure("admin");
        }

        loginAttemptService.recordSuccess("admin");

        assertThat(loginAttemptService.isBlocked("admin")).isFalse();
    }
}
