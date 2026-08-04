package com.khaoskrew.nexuscompanion.identity;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Validated result of one OAuth authorization callback. */
public final class AuthCallback {
    public enum Outcome {
        SUCCESS,
        PROVIDER_ERROR,
        REJECTED
    }

    private final Outcome outcome;
    private final String authorizationCode;
    private final String safeErrorCode;

    private AuthCallback(Outcome outcome, String authorizationCode, String safeErrorCode) {
        this.outcome = outcome;
        this.authorizationCode = authorizationCode;
        this.safeErrorCode = safeErrorCode;
    }

    public static AuthCallback validate(
        AuthorizationAttempt attempt,
        URI expectedRedirectUri,
        URI callbackUri,
        long nowEpochSeconds,
        long maximumAgeSeconds
    ) {
        Objects.requireNonNull(attempt, "attempt");
        Objects.requireNonNull(expectedRedirectUri, "expectedRedirectUri");
        Objects.requireNonNull(callbackUri, "callbackUri");

        if (!sameRedirect(expectedRedirectUri, callbackUri)) {
            return rejected("redirect_mismatch");
        }
        if (attempt.isExpired(nowEpochSeconds, maximumAgeSeconds)) {
            return rejected("authorization_expired");
        }

        final Map<String, String> parameters;
        try {
            parameters = queryParameters(callbackUri);
        } catch (IllegalArgumentException error) {
            return rejected("malformed_callback");
        }

        String callbackState = parameters.get("state");
        if (!attempt.consumeState(callbackState)) {
            return rejected(attempt.isConsumed() ? "state_replayed" : "state_mismatch");
        }

        String providerError = parameters.get("error");
        if (providerError != null && !providerError.trim().isEmpty()) {
            return new AuthCallback(
                Outcome.PROVIDER_ERROR,
                null,
                RedactedDiagnostics.safeIdentifier(providerError, "provider_error")
            );
        }

        String code = parameters.get("code");
        if (code == null || code.trim().isEmpty()) {
            return rejected("missing_authorization_code");
        }
        return new AuthCallback(Outcome.SUCCESS, code, null);
    }

    public Outcome outcome() {
        return outcome;
    }

    /** Sensitive. The caller must exchange and discard this value immediately. */
    public String authorizationCode() {
        return authorizationCode;
    }

    public String safeErrorCode() {
        return safeErrorCode;
    }

    public boolean isSuccess() {
        return outcome == Outcome.SUCCESS;
    }

    @Override
    public String toString() {
        return "AuthCallback{outcome=" + outcome + ", safeErrorCode=" + safeErrorCode + "}";
    }

    private static AuthCallback rejected(String safeErrorCode) {
        return new AuthCallback(Outcome.REJECTED, null, safeErrorCode);
    }

    private static boolean sameRedirect(URI expected, URI actual) {
        return equalIgnoreCase(expected.getScheme(), actual.getScheme())
            && equalIgnoreCase(expected.getHost(), actual.getHost())
            && expected.getPort() == actual.getPort()
            && Objects.equals(normalizePath(expected.getPath()), normalizePath(actual.getPath()));
    }

    private static String normalizePath(String value) {
        return value == null || value.isEmpty() ? "/" : value;
    }

    private static boolean equalIgnoreCase(String left, String right) {
        return left == null ? right == null : right != null && left.equalsIgnoreCase(right);
    }

    private static Map<String, String> queryParameters(URI uri) {
        String rawQuery = uri.getRawQuery();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (String pair : rawQuery.split("&", -1)) {
            if (pair.isEmpty()) {
                throw new IllegalArgumentException("Empty callback parameter");
            }
            int separator = pair.indexOf('=');
            String rawName = separator >= 0 ? pair.substring(0, separator) : pair;
            String rawValue = separator >= 0 ? pair.substring(separator + 1) : "";
            String name = decode(rawName);
            if (name.isEmpty() || values.containsKey(name)) {
                throw new IllegalArgumentException("Duplicate or empty callback parameter");
            }
            values.put(name, decode(rawValue));
        }
        return Collections.unmodifiableMap(values);
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException impossible) {
            throw new IllegalStateException("UTF-8 is unavailable", impossible);
        }
    }
}
