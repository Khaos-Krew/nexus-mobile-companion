package com.khaoskrew.nexuscompanion;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class UpdateSecurity {
    private UpdateSecurity() {
    }

    public static void validateManifest(UpdateManifest manifest, Context context)
        throws GeneralSecurityException, IOException {
        if (manifest.schemaVersion != UpdateManifest.SUPPORTED_SCHEMA_VERSION) {
            throw new GeneralSecurityException("Unsupported update schema");
        }
        if (!BuildConfig.UPDATE_CHANNEL.equals(manifest.channel)) {
            throw new GeneralSecurityException("Update channel mismatch");
        }
        if (!context.getPackageName().equals(manifest.packageName)) {
            throw new GeneralSecurityException("Update package mismatch");
        }
        if (!manifest.sha256.matches("(?i)[0-9a-f]{64}")) {
            throw new GeneralSecurityException("Invalid update SHA-256");
        }
        if (manifest.sizeBytes <= 0 || manifest.sizeBytes > UpdateClient.MAX_APK_BYTES) {
            throw new GeneralSecurityException("Invalid update size");
        }
        requireAllowedHttpsUrl(BuildConfig.UPDATE_MANIFEST_URL, manifest.apkUrl, BuildConfig.UPDATE_ALLOWED_HOSTS);
        verifyManifestSignature(manifest, BuildConfig.UPDATE_PUBLIC_KEY_B64);
    }

    public static void verifyManifestSignature(UpdateManifest manifest, String publicKeyBase64)
        throws GeneralSecurityException {
        if (isEmpty(publicKeyBase64)) {
            throw new GeneralSecurityException("Update signing key is not configured");
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64.replaceAll("\\s", ""));
            PublicKey publicKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(keyBytes));
            java.security.Signature verifier = java.security.Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(manifest.canonicalPayload().getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64.getDecoder().decode(manifest.signature.replaceAll("\\s", ""));
            if (!verifier.verify(signatureBytes)) {
                throw new SignatureException("Update manifest signature is invalid");
            }
        } catch (IllegalArgumentException error) {
            throw new GeneralSecurityException("Update signature encoding is invalid", error);
        }
    }

    public static void requireAllowedHttpsUrl(String manifestUrl, String candidateUrl, String configuredHosts)
        throws GeneralSecurityException {
        URI manifestUri = parseHttps(manifestUrl);
        URI candidateUri = parseHttps(candidateUrl);
        Set<String> allowedHosts = new HashSet<>();
        allowedHosts.add(manifestUri.getHost().toLowerCase(Locale.US));
        if (!isEmpty(configuredHosts)) {
            for (String host : configuredHosts.split(",")) {
                String normalized = host.trim().toLowerCase(Locale.US);
                if (!normalized.isEmpty()) {
                    allowedHosts.add(normalized);
                }
            }
        }
        String candidateHost = candidateUri.getHost().toLowerCase(Locale.US);
        if (!allowedHosts.contains(candidateHost)) {
            throw new GeneralSecurityException("Update download host is not allowed");
        }
    }

    private static URI parseHttps(String value) throws GeneralSecurityException {
        try {
            URI uri = new URI(value);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null) {
                throw new GeneralSecurityException("Update URLs must use HTTPS without embedded credentials");
            }
            return uri;
        } catch (URISyntaxException | NullPointerException error) {
            throw new GeneralSecurityException("Update URL is invalid", error);
        }
    }

    public static String sha256(File file) throws IOException, GeneralSecurityException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[64 * 1024];
        try (FileInputStream input = new FileInputStream(file)) {
            int read;
            while ((read = input.read(buffer)) >= 0) {
                if (read > 0) {
                    digest.update(buffer, 0, read);
                }
            }
        }
        return hex(digest.digest());
    }

    public static void verifyDownloadedApk(Context context, File apk, UpdateManifest manifest)
        throws PackageManager.NameNotFoundException, GeneralSecurityException, IOException {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo archive = getArchivePackageInfo(packageManager, apk);
        if (archive == null || archive.packageName == null) {
            throw new GeneralSecurityException("Downloaded file is not a valid APK");
        }
        if (!context.getPackageName().equals(archive.packageName)) {
            throw new GeneralSecurityException("Downloaded APK package does not match this app");
        }
        if (longVersionCode(archive) != manifest.versionCode) {
            throw new GeneralSecurityException("Downloaded APK version does not match the manifest");
        }
        PackageInfo installed = getInstalledPackageInfo(packageManager, context.getPackageName());
        String installedCertificate = certificateDigest(installed);
        String archiveCertificate = certificateDigest(archive);
        if (!installedCertificate.equals(archiveCertificate)) {
            throw new GeneralSecurityException("Downloaded APK signing certificate does not match the installed app");
        }
        String actualSha256 = sha256(apk);
        if (!actualSha256.equalsIgnoreCase(manifest.sha256)) {
            throw new GeneralSecurityException("Downloaded APK SHA-256 does not match the signed manifest");
        }
    }

    private static PackageInfo getArchivePackageInfo(PackageManager packageManager, File apk) {
        if (Build.VERSION.SDK_INT >= 33) {
            return packageManager.getPackageArchiveInfo(
                apk.getAbsolutePath(),
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES)
            );
        }
        return packageManager.getPackageArchiveInfo(apk.getAbsolutePath(), PackageManager.GET_SIGNATURES);
    }

    private static PackageInfo getInstalledPackageInfo(PackageManager packageManager, String packageName)
        throws PackageManager.NameNotFoundException {
        if (Build.VERSION.SDK_INT >= 33) {
            return packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES)
            );
        }
        return packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
    }

    @SuppressWarnings("deprecation")
    private static String certificateDigest(PackageInfo info) throws GeneralSecurityException {
        Signature[] signatures;
        if (Build.VERSION.SDK_INT >= 28 && info.signingInfo != null) {
            signatures = info.signingInfo.getApkContentsSigners();
        } else {
            signatures = info.signatures;
        }
        if (signatures == null || signatures.length != 1) {
            throw new GeneralSecurityException("Expected exactly one APK signer");
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return hex(digest.digest(signatures[0].toByteArray()));
    }

    @SuppressWarnings("deprecation")
    public static long longVersionCode(PackageInfo info) {
        return Build.VERSION.SDK_INT >= 28 ? info.getLongVersionCode() : info.versionCode;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String hex(byte[] bytes) {
        StringBuilder output = new StringBuilder(bytes.length * 2);
        for (byte item : bytes) {
            output.append(String.format(Locale.US, "%02x", item & 0xff));
        }
        return output.toString();
    }
}
