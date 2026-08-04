package com.khaoskrew.nexuscompanion.identity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class RedactedDiagnosticsTest {
    @Test
    public void removesNamedBearerAndJwtSecrets() {
        String raw = "access_token=abc123 refresh_token:xyz987 "
            + "Authorization=Bearer super-secret "
            + "eyJheader12345.eyJpayload12345.signature12345";
        String sanitized = RedactedDiagnostics.sanitize(raw);

        assertFalse(sanitized.contains("abc123"));
        assertFalse(sanitized.contains("xyz987"));
        assertFalse(sanitized.contains("super-secret"));
        assertFalse(sanitized.contains("eyJpayload12345"));
        assertTrue(sanitized.contains("[REDACTED]"));
    }

    @Test
    public void sessionTokenToStringNeverIncludesSensitiveValues() {
        SessionTokens tokens = new SessionTokens(
            "access-secret",
            "refresh-secret",
            2_000,
            3_000,
            "device-secret",
            "subject-secret",
            0
        );
        String diagnostic = tokens.toString();

        assertFalse(diagnostic.contains("access-secret"));
        assertFalse(diagnostic.contains("refresh-secret"));
        assertFalse(diagnostic.contains("device-secret"));
        assertFalse(diagnostic.contains("subject-secret"));
        assertTrue(diagnostic.contains("[REDACTED]"));
    }

    @Test
    public void safeErrorStripsMultilineSecrets() {
        String diagnostic = RedactedDiagnostics.safeError(
            new IllegalStateException("code=secret-code\nrefresh_token=secret-refresh")
        );

        assertFalse(diagnostic.contains("secret-code"));
        assertFalse(diagnostic.contains("secret-refresh"));
        assertFalse(diagnostic.contains("\n"));
    }
}
