package com.khaoskrew.nexuscompanion;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class UpdateInstaller {
    private UpdateInstaller() {
    }

    public static boolean canRequestPackageInstalls(Context context) {
        return Build.VERSION.SDK_INT < 26 || context.getPackageManager().canRequestPackageInstalls();
    }

    public static Intent unknownSourcesSettings(Context context) {
        return new Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:" + context.getPackageName())
        );
    }

    public static void install(Context context, File apk, UpdateManifest manifest) throws IOException {
        PackageInstaller installer = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
            PackageInstaller.SessionParams.MODE_FULL_INSTALL
        );
        params.setAppPackageName(context.getPackageName());
        params.setSize(apk.length());
        params.setInstallReason(PackageManager.INSTALL_REASON_USER);
        if (Build.VERSION.SDK_INT >= 31) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED);
        }

        int sessionId = installer.createSession(params);
        try (PackageInstaller.Session session = installer.openSession(sessionId);
             FileInputStream input = new FileInputStream(apk);
             OutputStream output = session.openWrite("base.apk", 0, apk.length())) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                if (read > 0) {
                    output.write(buffer, 0, read);
                }
            }
            session.fsync(output);

            Intent resultIntent = new Intent(context, UpdateInstallReceiver.class);
            resultIntent.setAction(UpdateInstallReceiver.ACTION_INSTALL_STATUS);
            resultIntent.putExtra(UpdateInstallReceiver.EXTRA_VERSION_NAME, manifest.versionName);
            int pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= 31) {
                pendingIntentFlags |= PendingIntent.FLAG_MUTABLE;
            }
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                resultIntent,
                pendingIntentFlags
            );
            IntentSender sender = pendingIntent.getIntentSender();
            session.commit(sender);
        } catch (IOException | RuntimeException error) {
            installer.abandonSession(sessionId);
            throw error;
        }
    }
}
