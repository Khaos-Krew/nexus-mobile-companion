package com.khaoskrew.nexuscompanion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.widget.Toast;

public final class UpdateInstallReceiver extends BroadcastReceiver {
    public static final String ACTION_INSTALL_STATUS =
        "com.khaoskrew.nexuscompanion.action.UPDATE_INSTALL_STATUS";
    public static final String EXTRA_VERSION_NAME = "versionName";

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(
            PackageInstaller.EXTRA_STATUS,
            PackageInstaller.STATUS_FAILURE
        );
        String versionName = intent.getStringExtra(EXTRA_VERSION_NAME);
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirmation = installerConfirmation(intent);
            if (confirmation != null) {
                confirmation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(confirmation);
            } else {
                Toast.makeText(context, "Android installer confirmation was unavailable.", Toast.LENGTH_LONG).show();
            }
            return;
        }

        if (status == PackageInstaller.STATUS_SUCCESS) {
            Toast.makeText(
                context,
                "Khaos Nexus Mobile " + safeVersion(versionName) + " installed.",
                Toast.LENGTH_LONG
            ).show();
            return;
        }

        String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
        Toast.makeText(
            context,
            "Update installation failed" + (message == null ? "." : ": " + message),
            Toast.LENGTH_LONG
        ).show();
    }

    private static Intent installerConfirmation(Intent source) {
        if (Build.VERSION.SDK_INT >= 33) {
            return source.getParcelableExtra(Intent.EXTRA_INTENT, Intent.class);
        }
        @SuppressWarnings("deprecation")
        Intent value = source.getParcelableExtra(Intent.EXTRA_INTENT);
        return value;
    }

    private static String safeVersion(String versionName) {
        return versionName == null || versionName.trim().isEmpty() ? "update" : versionName;
    }
}
