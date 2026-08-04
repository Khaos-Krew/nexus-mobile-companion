package com.khaoskrew.nexuscompanion;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class UpdateManifestTest {
    @Test
    public void canonicalPayloadIsStableAndExcludesSignature() {
        UpdateManifest manifest = new UpdateManifest(
            1,
            "preview",
            "com.khaoskrew.nexuscompanion.preview",
            3,
            "0.3.0-preview",
            "2026-08-04T17:00:00Z",
            "https://updates.example.com/khaos-nexus-0.3.0.apk",
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
            123456,
            false,
            "Fixes",
            "not-part-of-the-payload"
        );

        assertEquals(
            "schemaVersion=1\n"
                + "channel=preview\n"
                + "packageName=com.khaoskrew.nexuscompanion.preview\n"
                + "versionCode=3\n"
                + "versionName=0.3.0-preview\n"
                + "publishedAt=2026-08-04T17:00:00Z\n"
                + "apkUrl=https://updates.example.com/khaos-nexus-0.3.0.apk\n"
                + "sha256=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef\n"
                + "sizeBytes=123456\n"
                + "mandatory=false\n"
                + "releaseNotesSha256=59e5965495d92feeab9a57f4fad73dd3169ca0e0752d6d1983c6b2fc006fc13e",
            manifest.canonicalPayload()
        );
    }
}
