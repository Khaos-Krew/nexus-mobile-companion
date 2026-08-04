package com.khaoskrew.nexuscompanion.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;

public final class AuthorizationRequestTest {
    @Test
    public void buildsPkceAuthorizationUriWithoutClientSecret() {
        AuthorizationAttempt attempt = AuthorizationAttempt.create(1_000);
        AuthorizationRequest request = new AuthorizationRequest(
            URI.create("https://accounts.example.com/oauth/authorize"),
            "mobile-public-client",
            URI.create("khaosnexus://auth/callback"),
            "openid profile mobile",
            attempt
        );

        String uri = request.toUri().toString();
        assertTrue(uri.startsWith("https://accounts.example.com/oauth/authorize?"));
        assertTrue(uri.contains("response_type=code"));
        assertTrue(uri.contains("client_id=mobile-public-client"));
        assertTrue(uri.contains("redirect_uri=khaosnexus%3A%2F%2Fauth%2Fcallback"));
        assertTrue(uri.contains("scope=openid%20profile%20mobile"));
        assertTrue(uri.contains("state=" + attempt.state()));
        assertTrue(uri.contains("code_challenge=" + attempt.challenge()));
        assertTrue(uri.contains("code_challenge_method=S256"));
        assertFalse(uri.toLowerCase().contains("client_secret"));
        assertFalse(request.toString().contains(attempt.state()));
        assertFalse(request.toString().contains(attempt.verifier()));
    }

    @Test
    public void rejectsNonHttpsOrCredentialBearingEndpoint() {
        AuthorizationAttempt attempt = AuthorizationAttempt.create(1_000);

        assertThrows(
            IllegalArgumentException.class,
            () -> new AuthorizationRequest(
                URI.create("http://accounts.example.com/oauth/authorize"),
                "mobile-public-client",
                URI.create("khaosnexus://auth/callback"),
                "openid",
                attempt
            )
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> new AuthorizationRequest(
                URI.create("https://user:password@accounts.example.com/oauth/authorize"),
                "mobile-public-client",
                URI.create("khaosnexus://auth/callback"),
                "openid",
                attempt
            )
        );
    }

    @Test
    public void preservesExistingEndpointQuery() {
        AuthorizationAttempt attempt = AuthorizationAttempt.create(1_000);
        AuthorizationRequest request = new AuthorizationRequest(
            URI.create("https://accounts.example.com/oauth/authorize?tenant=nexus"),
            "mobile-public-client",
            URI.create("khaosnexus://auth/callback"),
            "openid",
            attempt
        );

        assertTrue(request.toUri().toString().contains("tenant=nexus&response_type=code"));
        assertEquals("khaosnexus", request.redirectUri().getScheme());
    }
}
