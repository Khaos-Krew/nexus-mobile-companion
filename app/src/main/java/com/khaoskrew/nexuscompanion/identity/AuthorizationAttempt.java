package com.khaoskrew.nexuscompanion.identity;

import java.security.SecureRandom;
import java.util.Objects;

/**
 * One authorization-code request. The state can be consumed exactly once so a
 * callback replay cannot be accepted by the same application process.
 */
public final class AuthorizationAttempt {
    private final String state;
    private final String verifier;
    private final String challenge;
    private final long createdAtEpochSeconds;
    private boolean consumed;

    private AuthorizationAttempt(
        String state,
        String verifier,
        String challenge,
        long createdAtEpochSeconds
    ) {
        this.state = Objects.requireNonNull(state, "state");
        this.verifier = Objects.requireNonNull(verifier, "verifier");
        this.challenge = Objects.requireNonNull(challenge, "challenge");
        this.createdAtEpochSeconds = createdAtEpochSeconds;
    }

    public static AuthorizationAttempt create(long nowEpochSeconds) {
        return create(nowEpochSeconds, new SecureRandom());
    }

    static AuthorizationAttempt create(long nowEpochSeconds, SecureRandom random) {
        if (nowEpochSeconds < 0) {
            throw new IllegalArgumentException("Authorization time cannot be negative");
        }
        String verifier = Pkce.generateVerifier(random);
        return new AuthorizationAttempt(
            Pkce.generateState(random),
            verifier,
            Pkce.challenge(verifier),
            nowEpochSeconds
        );
    }

    public String state() {
        return state;
    }

    public String verifier() {
        return verifier;
    }

    public String challenge() {
        return challenge;
    }

    public long createdAtEpochSeconds() {
        return createdAtEpochSeconds;
    }

    public boolean isExpired(long nowEpochSeconds, long maximumAgeSeconds) {
        if (maximumAgeSeconds <= 0 || nowEpochSeconds < createdAtEpochSeconds) {
            return true;
        }
        return nowEpochSeconds - createdAtEpochSeconds > maximumAgeSeconds;
    }

    public synchronized boolean consumeState(String callbackState) {
        if (consumed || !Pkce.constantTimeEquals(state, callbackState)) {
            return false;
        }
        consumed = true;
        return true;
    }

    public synchronized boolean isConsumed() {
        return consumed;
    }

    @Override
    public String toString() {
        return "AuthorizationAttempt{createdAtEpochSeconds="
            + createdAtEpochSeconds
            + ", consumed="
            + consumed
            + "}";
    }
}
