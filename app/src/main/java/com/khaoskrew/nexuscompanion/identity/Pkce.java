package com.khaoskrew.nexuscompanion.identity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

/** RFC 7636 PKCE helpers with cryptographically secure, URL-safe values. */
public final class Pkce {
    private static final int VERIFIER_BYTES = 64;
    private static final int STATE_BYTES = 32;
    private static final Pattern VERIFIER_PATTERN =
        Pattern.compile("^[A-Za-z0-9\\-._~]{43,128}$");

    private Pkce() {
    }

    public static String generateVerifier() {
        return generateVerifier(new SecureRandom());
    }

    static String generateVerifier(SecureRandom random) {
        Objects.requireNonNull(random, "random");
        byte[] bytes = new byte[VERIFIER_BYTES];
        random.nextBytes(bytes);
        String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        if (!isValidVerifier(verifier)) {
            throw new IllegalStateException("Generated PKCE verifier is outside RFC 7636 constraints");
        }
        return verifier;
    }

    public static String generateState() {
        return generateState(new SecureRandom());
    }

    static String generateState(SecureRandom random) {
        Objects.requireNonNull(random, "random");
        byte[] bytes = new byte[STATE_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String challenge(String verifier) {
        if (!isValidVerifier(verifier)) {
            throw new IllegalArgumentException("Invalid PKCE verifier");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }

    public static boolean isValidVerifier(String verifier) {
        return verifier != null && VERIFIER_PATTERN.matcher(verifier).matches();
    }

    public static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return MessageDigest.isEqual(
            left.getBytes(StandardCharsets.UTF_8),
            right.getBytes(StandardCharsets.UTF_8)
        );
    }
}
