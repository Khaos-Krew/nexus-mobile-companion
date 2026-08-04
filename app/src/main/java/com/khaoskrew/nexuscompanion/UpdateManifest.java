package com.khaoskrew.nexuscompanion;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/** Immutable, signed metadata for one mobile companion APK update. */
public final class UpdateManifest {
    public static final int SUPPORTED_SCHEMA_VERSION = 1;

    public final int schemaVersion;
    public final String channel;
    public final String packageName;
    public final long versionCode;
    public final String versionName;
    public final String publishedAt;
    public final String apkUrl;
    public final String sha256;
    public final long sizeBytes;
    public final boolean mandatory;
    public final String releaseNotes;
    public final String signature;

    public UpdateManifest(
        int schemaVersion,
        String channel,
        String packageName,
        long versionCode,
        String versionName,
        String publishedAt,
        String apkUrl,
        String sha256,
        long sizeBytes,
        boolean mandatory,
        String releaseNotes,
        String signature
    ) {
        this.schemaVersion = schemaVersion;
        this.channel = channel;
        this.packageName = packageName;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.publishedAt = publishedAt;
        this.apkUrl = apkUrl;
        this.sha256 = sha256.toLowerCase(Locale.US);
        this.sizeBytes = sizeBytes;
        this.mandatory = mandatory;
        this.releaseNotes = releaseNotes;
        this.signature = signature;
    }

    public static UpdateManifest parse(String json) throws JSONException {
        JSONObject object = new JSONObject(json);
        return new UpdateManifest(
            object.getInt("schemaVersion"),
            object.getString("channel"),
            object.getString("packageName"),
            object.getLong("versionCode"),
            object.getString("versionName"),
            object.getString("publishedAt"),
            object.getString("apkUrl"),
            object.getString("sha256"),
            object.getLong("sizeBytes"),
            object.optBoolean("mandatory", false),
            object.optString("releaseNotes", ""),
            object.getString("signature")
        );
    }

    /**
     * Canonical payload signed with SHA256withRSA. Release notes are represented by
     * their SHA-256 digest so arbitrary newlines cannot change field framing.
     */
    public String canonicalPayload() {
        return "schemaVersion=" + schemaVersion + "\n"
            + "channel=" + channel + "\n"
            + "packageName=" + packageName + "\n"
            + "versionCode=" + versionCode + "\n"
            + "versionName=" + versionName + "\n"
            + "publishedAt=" + publishedAt + "\n"
            + "apkUrl=" + apkUrl + "\n"
            + "sha256=" + sha256 + "\n"
            + "sizeBytes=" + sizeBytes + "\n"
            + "mandatory=" + mandatory + "\n"
            + "releaseNotesSha256=" + sha256Hex(releaseNotes.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(byte[] value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value);
            StringBuilder output = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                output.append(String.format(Locale.US, "%02x", item & 0xff));
            }
            return output.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }
}
