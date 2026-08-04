package com.khaoskrew.nexuscompanion.identity;

import java.util.Objects;

/** Sensitive token bundle. Never include this object in logs, analytics, or exceptions. */
public final class SessionTokens {
    private final String accessToken;
    private final String refreshToken;
    private final long accessExpiresAtEpochSeconds;
    private final long refreshExpiresAtEpochSeconds;
    private final String deviceId;
    private final String subjectId;
    private final long rotationCounter;

    public SessionTokens(
        String accessToken,
        String refreshToken,
        long accessExpiresAtEpochSeconds,
        long refreshExpiresAtEpochSeconds,
        String deviceId,
        String subjectId,
        long rotationCounter
    ) {
        this.accessToken = requireSecret(accessToken, "accessToken");
        this.refreshToken = requireSecret(refreshToken, "refreshToken");
        if (accessExpiresAtEpochSeconds <= 0
            || refreshExpiresAtEpochSeconds <= accessExpiresAtEpochSeconds) {
            throw new IllegalArgumentException("Token expiries are invalid");
        }
        if (rotationCounter < 0) {
            throw new IllegalArgumentException("rotationCounter cannot be negative");
        }
        this.accessExpiresAtEpochSeconds = accessExpiresAtEpochSeconds;
        this.refreshExpiresAtEpochSeconds = refreshExpiresAtEpochSeconds;
        this.deviceId = requireIdentifier(deviceId, "deviceId");
        this.subjectId = requireIdentifier(subjectId, "subjectId");
        this.rotationCounter = rotationCounter;
    }

    public String accessToken() {
        return accessToken;
    }

    public String refreshToken() {
        return refreshToken;
    }

    public long accessExpiresAtEpochSeconds() {
        return accessExpiresAtEpochSeconds;
    }

    public long refreshExpiresAtEpochSeconds() {
        return refreshExpiresAtEpochSeconds;
    }

    public String deviceId() {
        return deviceId;
    }

    public String subjectId() {
        return subjectId;
    }

    public long rotationCounter() {
        return rotationCounter;
    }

    public boolean accessExpired(long nowEpochSeconds, long clockSkewSeconds) {
        return nowEpochSeconds + Math.max(0, clockSkewSeconds) >= accessExpiresAtEpochSeconds;
    }

    public boolean refreshExpired(long nowEpochSeconds, long clockSkewSeconds) {
        return nowEpochSeconds + Math.max(0, clockSkewSeconds) >= refreshExpiresAtEpochSeconds;
    }

    public SessionTokens rotate(
        String newAccessToken,
        String newRefreshToken,
        long newAccessExpiry,
        long newRefreshExpiry
    ) {
        return new SessionTokens(
            newAccessToken,
            newRefreshToken,
            newAccessExpiry,
            newRefreshExpiry,
            deviceId,
            subjectId,
            rotationCounter + 1
        );
    }

    @Override
    public String toString() {
        return "SessionTokens{accessToken=[REDACTED], refreshToken=[REDACTED], "
            + "accessExpiresAtEpochSeconds="
            + accessExpiresAtEpochSeconds
            + ", refreshExpiresAtEpochSeconds="
            + refreshExpiresAtEpochSeconds
            + ", deviceId=[REDACTED], subjectId=[REDACTED], rotationCounter="
            + rotationCounter
            + "}";
    }

    private static String requireSecret(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return value;
    }

    private static String requireIdentifier(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.trim().isEmpty() || value.length() > 256) {
            throw new IllegalArgumentException(name + " is invalid");
        }
        return value;
    }
}
