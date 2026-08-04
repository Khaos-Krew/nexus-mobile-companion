package com.khaoskrew.nexuscompanion.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;

public final class AuthCallbackTest {
    private static final URI REDIRECT = URI.create("khaosnexus://auth/callback");

    @Test
    public void acceptsOneMatchingCallbackAndRejectsReplay() {
        AuthorizationAttempt attempt = AuthorizationAttempt.create(1_000);
        URI callback = URI.create(
            REDIRECT + "?code=temporary-code&state=" + attempt.state()
        );

        AuthCallback accepted = AuthCallback.validate(attempt, REDIRECT, callback, 1_030, 300);
        AuthCallback replay = AuthCallback.validate(attempt, REDIRECT, callback, 1_031, 300);

        assertTrue(accepted.isSuccess());
        assertEquals("temporary-code", accepted.authorizationCode());
        assertFalse(replay.isSuccess());
        assertEquals("state_replayed", replay.safeErrorCode());
        assertFalse(accepted.toString().contains("temporary-code"));
    }

    @Test
    public void rejectsStateAndRedirectMismatch() {
        AuthorizationAttempt stateAttempt = AuthorizationAttempt.create(1_000);
        AuthCallback wrongState = AuthCallback.validate(
            stateAttempt,
            REDIRECT,
            URI.create(REDIRECT + "?code=x&state=wrong"),
            1_010,
            300
        );
        AuthorizationAttempt redirectAttempt = AuthorizationAttempt.create(1_000);
        AuthCallback wrongRedirect = AuthCallback.validate(
            redirectAttempt,
            REDIRECT,
            URI.create("evil://auth/callback?code=x&state=" + redirectAttempt.state()),
            1_010,
            300
        );

        assertEquals("state_mismatch", wrongState.safeErrorCode());
        assertEquals("redirect_mismatch", wrongRedirect.safeErrorCode());
    }

    @Test
    public void rejectsExpiredAttemptBeforeConsumingState() {
        AuthorizationAttempt attempt = AuthorizationAttempt.create(1_000);
        AuthCallback result = AuthCallback.validate(
            attempt,
            REDIRECT,
            URI.create(REDIRECT + "?code=x&state=" + attempt.state()),
            1_301,
            300
        );

        assertEquals("authorization_expired", result.safeErrorCode());
        assertFalse(attempt.isConsumed());
    }

    @Test
    public void returnsSafeProviderErrorWithoutDescription() {
        AuthorizationAttempt attempt = AuthorizationAttempt.create(1_000);
        AuthCallback result = AuthCallback.validate(
            attempt,
            REDIRECT,
            URI.create(REDIRECT + "?error=access_denied&error_description=secret&state=" + attempt.state()),
            1_010,
            300
        );

        assertEquals(AuthCallback.Outcome.PROVIDER_ERROR, result.outcome());
        assertEquals("access_denied", result.safeErrorCode());
        assertFalse(result.toString().contains("secret"));
    }

    @Test
    public void rejectsDuplicateSecurityParametersWithoutConsumingState() {
        AuthorizationAttempt attempt = AuthorizationAttempt.create(1_000);
        AuthCallback result = AuthCallback.validate(
            attempt,
            REDIRECT,
            URI.create(
                REDIRECT
                    + "?code=first&code=second&state="
                    + attempt.state()
            ),
            1_010,
            300
        );

        assertEquals("malformed_callback", result.safeErrorCode());
        assertFalse(attempt.isConsumed());
    }
}
