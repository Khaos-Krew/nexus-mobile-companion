package com.khaoskrew.nexuscompanion.identity;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Removes common OAuth, token, device, and identity secrets from diagnostics. */
public final class RedactedDiagnostics {
    private static final Pattern NAMED_SECRET = Pattern.compile(
        "(?i)(access_token|refresh_token|id_token|authorization_code|code_verifier|code|state|device_id|subject_id|authorization)\\s*[:=]\\s*([^\\s&,;]+)"
    );
    private static final Pattern BEARER = Pattern.compile("(?i)bearer\\s+[A-Za-z0-9._~+\\-/]+=*");
    private static final Pattern JWT = Pattern.compile(
        "\\b[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\b"
    );
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_.-]{1,64}$");
    private static final int MAX_MESSAGE_LENGTH = 240;

    private RedactedDiagnostics() {
    }

    public static String sanitize(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "No diagnostic detail available";
        }
        // Redact a full bearer credential before the named-field pass can consume
        // only the word "Bearer" and leave the credential behind.
        String value = BEARER.matcher(message).replaceAll("Bearer [REDACTED]");
        value = JWT.matcher(value).replaceAll("[REDACTED_JWT]");
        value = NAMED_SECRET.matcher(value).replaceAll("$1=[REDACTED]");
        value = value.replace('\n', ' ').replace('\r', ' ').trim();
        if (value.length() > MAX_MESSAGE_LENGTH) {
            value = value.substring(0, MAX_MESSAGE_LENGTH) + "…";
        }
        return value;
    }

    public static String safeError(Throwable error) {
        if (error == null) {
            return "UnknownError: No diagnostic detail available";
        }
        String type = safeIdentifier(error.getClass().getSimpleName(), "Error");
        return type + ": " + sanitize(error.getMessage());
    }

    public static String safeIdentifier(String value, String fallback) {
        String normalizedFallback = fallback == null || fallback.trim().isEmpty()
            ? "unknown"
            : fallback.toLowerCase(Locale.US);
        if (value == null) {
            return normalizedFallback;
        }
        String normalized = value.trim();
        return SAFE_IDENTIFIER.matcher(normalized).matches() ? normalized : normalizedFallback;
    }

    /** Returns true when an unredacted string contains a token-like value. */
    static boolean containsLikelySecret(String value) {
        if (value == null) {
            return false;
        }
        String withoutMarkers = value
            .replace("[REDACTED]", "")
            .replace("[REDACTED_JWT]", "");
        Matcher named = NAMED_SECRET.matcher(withoutMarkers);
        return named.find()
            || BEARER.matcher(withoutMarkers).find()
            || JWT.matcher(withoutMarkers).find();
    }
}
