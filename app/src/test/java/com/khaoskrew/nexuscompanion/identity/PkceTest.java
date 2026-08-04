package com.khaoskrew.nexuscompanion.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PkceTest {
    @Test
    public void generatedVerifierMeetsRfcConstraints() {
        String first = Pkce.generateVerifier();
        String second = Pkce.generateVerifier();

        assertTrue(Pkce.isValidVerifier(first));
        assertTrue(first.length() >= 43 && first.length() <= 128);
        assertNotEquals(first, second);
        assertEquals(43, Pkce.challenge(first).length());
    }

    @Test
    public void challengeMatchesRfc7636Example() {
        assertEquals(
            "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
            Pkce.challenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk")
        );
    }

    @Test
    public void stateComparisonRejectsNullAndMismatch() {
        assertTrue(Pkce.constantTimeEquals("same", "same"));
        assertFalse(Pkce.constantTimeEquals("same", "different"));
        assertFalse(Pkce.constantTimeEquals(null, "same"));
    }
}
