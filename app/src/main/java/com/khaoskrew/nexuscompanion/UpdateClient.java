package com.khaoskrew.nexuscompanion;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public final class UpdateClient {
    public static final long MAX_APK_BYTES = 250L * 1024L * 1024L;
    private static final int MAX_MANIFEST_BYTES = 256 * 1024;
    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 30_000;

    private final Context context;

    public UpdateClient(Context context) {
        this.context = context.getApplicationContext();
    }

    public CheckResult check() throws Exception {
        if (!BuildConfig.UPDATE_CHECK_ENABLED || isEmpty(BuildConfig.UPDATE_MANIFEST_URL)) {
            return CheckResult.notConfigured();
        }
        UpdateSecurity.requireAllowedHttpsUrl(
            BuildConfig.UPDATE_MANIFEST_URL,
            BuildConfig.UPDATE_MANIFEST_URL,
            BuildConfig.UPDATE_ALLOWED_HOSTS
        );
        byte[] bytes = downloadBytes(BuildConfig.UPDATE_MANIFEST_URL, MAX_MANIFEST_BYTES);
        UpdateManifest manifest = UpdateManifest.parse(new String(bytes, StandardCharsets.UTF_8));
        UpdateSecurity.validateManifest(manifest, context);

        long installedVersion = installedVersionCode();
        if (manifest.versionCode <= installedVersion) {
            return CheckResult.current(manifest, installedVersion);
        }
        return CheckResult.available(manifest, installedVersion);
    }

    public File downloadAndVerify(UpdateManifest manifest, ProgressListener listener) throws Exception {
        UpdateSecurity.validateManifest(manifest, context);
        File destination = new File(context.getCacheDir(), "verified-mobile-update.apk");
        File temporary = new File(context.getCacheDir(), "verified-mobile-update.apk.part");
        if (temporary.exists() && !temporary.delete()) {
            throw new IOException("Unable to replace temporary update file");
        }

        HttpURLConnection connection = open(manifest.apkUrl);
        long declaredLength = connection.getContentLengthLong();
        if (declaredLength > MAX_APK_BYTES) {
            connection.disconnect();
            throw new IOException("Update APK is larger than the allowed limit");
        }
        if (declaredLength > 0 && declaredLength != manifest.sizeBytes) {
            connection.disconnect();
            throw new GeneralSecurityException("Update APK size does not match the signed manifest");
        }

        long total = 0;
        byte[] buffer = new byte[64 * 1024];
        try (InputStream input = connection.getInputStream();
             FileOutputStream output = new FileOutputStream(temporary)) {
            int read;
            while ((read = input.read(buffer)) >= 0) {
                if (read == 0) {
                    continue;
                }
                total += read;
                if (total > MAX_APK_BYTES || total > manifest.sizeBytes) {
                    throw new IOException("Update APK exceeded the signed size limit");
                }
                output.write(buffer, 0, read);
                if (listener != null) {
                    listener.onProgress(total, manifest.sizeBytes);
                }
            }
            output.getFD().sync();
        } finally {
            connection.disconnect();
        }

        if (total != manifest.sizeBytes) {
            temporary.delete();
            throw new GeneralSecurityException("Downloaded APK size does not match the signed manifest");
        }
        UpdateSecurity.verifyDownloadedApk(context, temporary, manifest);
        if (destination.exists() && !destination.delete()) {
            temporary.delete();
            throw new IOException("Unable to replace previous verified update");
        }
        if (!temporary.renameTo(destination)) {
            temporary.delete();
            throw new IOException("Unable to finalize verified update");
        }
        return destination;
    }

    private long installedVersionCode() throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        if (Build.VERSION.SDK_INT >= 33) {
            packageInfo = packageManager.getPackageInfo(
                context.getPackageName(),
                PackageManager.PackageInfoFlags.of(0)
            );
        } else {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        }
        return UpdateSecurity.longVersionCode(packageInfo);
    }

    private byte[] downloadBytes(String url, int maximumBytes) throws IOException {
        HttpURLConnection connection = open(url);
        try (InputStream input = connection.getInputStream();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            int total = 0;
            while ((read = input.read(buffer)) >= 0) {
                if (read == 0) {
                    continue;
                }
                total += read;
                if (total > maximumBytes) {
                    throw new IOException("Update manifest exceeded the size limit");
                }
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection open(String value) throws IOException {
        URL url = new URL(value);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setInstanceFollowRedirects(false);
        connection.setUseCaches(false);
        connection.setRequestProperty("Accept", "application/json, application/vnd.android.package-archive;q=0.9");
        connection.setRequestProperty("User-Agent", "Khaos-Nexus-Mobile/" + BuildConfig.VERSION_NAME);
        int status = connection.getResponseCode();
        if (status < 200 || status >= 300) {
            connection.disconnect();
            throw new IOException("Update server returned HTTP " + status);
        }
        return connection;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public interface ProgressListener {
        void onProgress(long downloadedBytes, long totalBytes);
    }

    public static final class CheckResult {
        public enum State {
            NOT_CONFIGURED,
            CURRENT,
            AVAILABLE
        }

        public final State state;
        public final UpdateManifest manifest;
        public final long installedVersionCode;

        private CheckResult(State state, UpdateManifest manifest, long installedVersionCode) {
            this.state = state;
            this.manifest = manifest;
            this.installedVersionCode = installedVersionCode;
        }

        public static CheckResult notConfigured() {
            return new CheckResult(State.NOT_CONFIGURED, null, -1);
        }

        public static CheckResult current(UpdateManifest manifest, long installedVersionCode) {
            return new CheckResult(State.CURRENT, manifest, installedVersionCode);
        }

        public static CheckResult available(UpdateManifest manifest, long installedVersionCode) {
            return new CheckResult(State.AVAILABLE, manifest, installedVersionCode);
        }
    }
}
