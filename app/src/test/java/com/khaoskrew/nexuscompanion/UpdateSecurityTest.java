package com.khaoskrew.nexuscompanion;

import static org.junit.Assert.assertThrows;

import java.security.GeneralSecurityException;

import org.junit.Test;

public final class UpdateSecurityTest {
    @Test
    public void allowsManifestHostAndExplicitCdnHost() throws Exception {
        UpdateSecurity.requireAllowedHttpsUrl(
            "https://updates.example.com/preview.json",
            "https://updates.example.com/mobile.apk",
            "cdn.example.com"
        );
        UpdateSecurity.requireAllowedHttpsUrl(
            "https://updates.example.com/preview.json",
            "https://cdn.example.com/mobile.apk",
            "cdn.example.com"
        );
    }

    @Test
    public void rejectsHttpAndUnlistedHosts() {
        assertThrows(
            GeneralSecurityException.class,
            () -> UpdateSecurity.requireAllowedHttpsUrl(
                "https://updates.example.com/preview.json",
                "http://updates.example.com/mobile.apk",
                ""
            )
        );
        assertThrows(
            GeneralSecurityException.class,
            () -> UpdateSecurity.requireAllowedHttpsUrl(
                "https://updates.example.com/preview.json",
                "https://evil.example/mobile.apk",
                "cdn.example.com"
            )
        );
    }
}
