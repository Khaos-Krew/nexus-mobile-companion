package com.khaoskrew.nexuscompanion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

/**
 * Startup update gate for the direct preview channel.
 *
 * Metadata checks may run automatically. Download and installation never do: the
 * user must explicitly review and approve both the app dialog and Android installer.
 */
public final class UpdateGateActivity extends Activity {
    private static final int BLACK = Color.rgb(5, 6, 9);
    private static final int PANEL = Color.rgb(14, 16, 22);
    private static final int CRIMSON = Color.rgb(255, 53, 86);
    private static final int TEXT = Color.rgb(241, 237, 239);
    private static final int MUTED = Color.rgb(172, 162, 168);
    private static final int DIM = Color.rgb(112, 105, 113);
    private static final int GOOD = Color.rgb(79, 220, 154);
    private static final int WARNING = Color.rgb(255, 188, 92);

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private UpdateClient updateClient;
    private TextView statusView;
    private TextView detailsView;
    private TextView channelView;
    private ProgressBar progressBar;
    private Button checkButton;
    private Button installButton;
    private Button continueButton;
    private UpdateManifest availableUpdate;
    private File pendingInstall;
    private boolean companionOpened;
    private boolean busy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        updateClient = new UpdateClient(this);
        setContentView(buildScreen());
        checkForUpdates(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pendingInstall != null && UpdateInstaller.canRequestPackageInstalls(this)) {
            File verifiedApk = pendingInstall;
            pendingInstall = null;
            commitInstall(verifiedApk);
        }
    }

    @Override
    public void onBackPressed() {
        openCompanion();
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(BLACK);
        window.setNavigationBarColor(BLACK);
    }

    private View buildScreen() {
        FrameLayout root = new FrameLayout(this);
        root.setBackground(new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[] { Color.rgb(46, 5, 18), Color.rgb(11, 12, 18), BLACK }
        ));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        root.addView(scroll, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(20), dp(38), dp(20), dp(30));
        scroll.addView(page, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView mark = text("N", 27, Color.WHITE, true);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(panel(CRIMSON, Color.rgb(255, 112, 136), dp(20), 1));
        page.addView(mark, new LinearLayout.LayoutParams(dp(72), dp(72)));

        TextView eyebrow = text("KHAOS NEXUS MOBILE", 10, Color.rgb(255, 144, 162), true);
        eyebrow.setLetterSpacing(0.20f);
        eyebrow.setPadding(0, dp(22), 0, dp(7));
        page.addView(eyebrow);

        TextView title = text("Update Center", 30, Color.WHITE, true);
        page.addView(title);

        TextView description = text(
            "Signed release checks, verified downloads, and Android-confirmed installation.",
            13,
            MUTED,
            false
        );
        description.setGravity(Gravity.CENTER);
        description.setPadding(dp(12), dp(8), dp(12), dp(24));
        page.addView(description);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(19), dp(18), dp(19), dp(18));
        card.setBackground(panel(Color.argb(245, 14, 16, 22), Color.argb(115, 255, 53, 86), dp(18), 1));
        page.addView(card, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        channelView = text("CHANNEL · " + BuildConfig.UPDATE_CHANNEL.toUpperCase(Locale.US), 9, DIM, true);
        channelView.setLetterSpacing(0.14f);
        card.addView(channelView);

        statusView = text("Preparing update check…", 20, TEXT, true);
        statusView.setPadding(0, dp(10), 0, dp(7));
        card.addView(statusView);

        detailsView = text(
            "Installed version " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")",
            12,
            MUTED,
            false
        );
        detailsView.setLineSpacing(0, 1.2f);
        card.addView(detailsView);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(1000);
        progressBar.setProgressTintList(ColorStateList.valueOf(CRIMSON));
        progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(45, 45, 52)));
        progressBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(7)
        );
        progressParams.topMargin = dp(17);
        card.addView(progressBar, progressParams);

        installButton = actionButton("REVIEW & INSTALL UPDATE", CRIMSON, Color.WHITE);
        installButton.setVisibility(View.GONE);
        installButton.setOnClickListener(view -> confirmUpdate());
        card.addView(installButton, buttonParams());

        checkButton = actionButton("CHECK AGAIN", Color.rgb(34, 36, 44), TEXT);
        checkButton.setOnClickListener(view -> checkForUpdates(false));
        card.addView(checkButton, buttonParams());

        continueButton = actionButton("CONTINUE TO COMPANION", Color.TRANSPARENT, MUTED);
        continueButton.setBackground(panel(Color.TRANSPARENT, Color.rgb(70, 70, 78), dp(13), 1));
        continueButton.setOnClickListener(view -> openCompanion());
        card.addView(continueButton, buttonParams());

        TextView policy = text(
            "Automatic installation is disabled. Every APK must match the signed manifest, SHA-256, package identity, version, and installed signing certificate before Android is allowed to present its installer.",
            11,
            DIM,
            false
        );
        policy.setLineSpacing(0, 1.24f);
        policy.setPadding(dp(6), dp(20), dp(6), 0);
        page.addView(policy);

        return root;
    }

    private void checkForUpdates(boolean automatic) {
        if (busy) {
            return;
        }
        busy = true;
        availableUpdate = null;
        installButton.setVisibility(View.GONE);
        checkButton.setEnabled(false);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
        statusView.setText("Checking signed channel…");
        detailsView.setText("No APK will download without your approval.");

        new Thread(() -> {
            try {
                UpdateClient.CheckResult result = updateClient.check();
                runOnUiThread(() -> showCheckResult(result, automatic));
            } catch (Exception error) {
                runOnUiThread(() -> showCheckFailure(error, automatic));
            }
        }, "nexus-update-check").start();
    }

    private void showCheckResult(UpdateClient.CheckResult result, boolean automatic) {
        busy = false;
        checkButton.setEnabled(true);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);

        switch (result.state) {
            case AVAILABLE:
                availableUpdate = result.manifest;
                statusView.setText("Update available · " + availableUpdate.versionName);
                statusView.setTextColor(WARNING);
                detailsView.setText(
                    availableUpdate.releaseNotes.trim().isEmpty()
                        ? "A verified update is ready for review."
                        : availableUpdate.releaseNotes
                );
                installButton.setVisibility(View.VISIBLE);
                break;
            case CURRENT:
                statusView.setText("You are up to date");
                statusView.setTextColor(GOOD);
                detailsView.setText("Installed version " + BuildConfig.VERSION_NAME + " is current on the " + BuildConfig.UPDATE_CHANNEL + " channel.");
                if (automatic) {
                    mainHandler.postDelayed(this::openCompanion, 850);
                }
                break;
            case NOT_CONFIGURED:
            default:
                statusView.setText("Update channel not configured");
                statusView.setTextColor(WARNING);
                detailsView.setText(
                    BuildConfig.STABLE_PREVIEW_SIGNING
                        ? "A signed preview key is present, but no approved HTTPS release manifest is configured."
                        : "The updater is installed, but live checks remain disabled until a stable preview signing key and approved HTTPS release manifest are configured."
                );
                if (automatic) {
                    mainHandler.postDelayed(this::openCompanion, 1150);
                }
                break;
        }
    }

    private void showCheckFailure(Exception error, boolean automatic) {
        busy = false;
        checkButton.setEnabled(true);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
        statusView.setText("Update check unavailable");
        statusView.setTextColor(WARNING);
        detailsView.setText(cleanMessage(error));
        if (automatic) {
            mainHandler.postDelayed(this::openCompanion, 1500);
        }
    }

    private void confirmUpdate() {
        if (availableUpdate == null || busy) {
            return;
        }
        String notes = availableUpdate.releaseNotes.trim().isEmpty()
            ? "No release notes were provided."
            : availableUpdate.releaseNotes;
        new AlertDialog.Builder(this)
            .setTitle("Install " + availableUpdate.versionName + "?")
            .setMessage(
                notes
                    + "\n\nThe APK will be downloaded only after this confirmation, verified locally, and then handed to Android's installer for a second confirmation."
            )
            .setNegativeButton("Not now", null)
            .setPositiveButton("Download and verify", (dialog, which) -> downloadUpdate())
            .show();
    }

    private void downloadUpdate() {
        if (availableUpdate == null || busy) {
            return;
        }
        busy = true;
        checkButton.setEnabled(false);
        installButton.setEnabled(false);
        continueButton.setEnabled(false);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
        statusView.setText("Downloading verified update…");
        statusView.setTextColor(TEXT);
        detailsView.setText("0% · Signature and package verification follow download.");

        UpdateManifest manifest = availableUpdate;
        new Thread(() -> {
            try {
                File apk = updateClient.downloadAndVerify(manifest, (downloaded, total) -> {
                    int progress = total <= 0 ? 0 : (int) Math.min(1000, (downloaded * 1000L) / total);
                    int percent = progress / 10;
                    runOnUiThread(() -> {
                        progressBar.setProgress(progress);
                        detailsView.setText(percent + "% · " + humanBytes(downloaded) + " of " + humanBytes(total));
                    });
                });
                runOnUiThread(() -> prepareInstall(apk));
            } catch (Exception error) {
                runOnUiThread(() -> showDownloadFailure(error));
            }
        }, "nexus-update-download").start();
    }

    private void prepareInstall(File apk) {
        busy = false;
        checkButton.setEnabled(true);
        installButton.setEnabled(true);
        continueButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        statusView.setText("Update verified");
        statusView.setTextColor(GOOD);
        detailsView.setText("Package, version, signing certificate, signed metadata, file size, and SHA-256 all match.");

        if (!UpdateInstaller.canRequestPackageInstalls(this)) {
            pendingInstall = apk;
            new AlertDialog.Builder(this)
                .setTitle("Allow this preview installer")
                .setMessage("Android 8 and newer require you to allow this app as an installation source. This permission is used only after you approve a verified Khaos Nexus Mobile update.")
                .setNegativeButton("Cancel", (dialog, which) -> pendingInstall = null)
                .setPositiveButton("Open Android setting", (dialog, which) -> startActivity(UpdateInstaller.unknownSourcesSettings(this)))
                .show();
            return;
        }
        commitInstall(apk);
    }

    private void commitInstall(File apk) {
        if (availableUpdate == null) {
            return;
        }
        try {
            UpdateInstaller.install(this, apk, availableUpdate);
            statusView.setText("Android installer opened");
            statusView.setTextColor(GOOD);
            detailsView.setText("Review Android's final package confirmation to complete the update.");
        } catch (Exception error) {
            showDownloadFailure(error);
        }
    }

    private void showDownloadFailure(Exception error) {
        busy = false;
        checkButton.setEnabled(true);
        installButton.setEnabled(true);
        continueButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        statusView.setText("Update rejected");
        statusView.setTextColor(CRIMSON);
        detailsView.setText(cleanMessage(error));
    }

    private void openCompanion() {
        if (companionOpened || isFinishing()) {
            return;
        }
        companionOpened = true;
        mainHandler.removeCallbacksAndMessages(null);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private String cleanMessage(Throwable error) {
        String message = error.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return "The update channel could not be verified. The companion can still be opened safely.";
        }
        return message;
    }

    private String humanBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kib = bytes / 1024d;
        if (kib < 1024) {
            return String.format(Locale.US, "%.1f KiB", kib);
        }
        return String.format(Locale.US, "%.1f MiB", kib / 1024d);
    }

    private Button actionButton(String label, int fill, int color) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        button.setTextColor(color);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(12), dp(11), dp(12), dp(11));
        button.setBackground(panel(fill, Color.argb(115, 255, 53, 86), dp(13), 1));
        return button;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(12);
        return params;
    }

    private TextView text(String value, int sizeSp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        view.setTextColor(color);
        view.setTypeface(android.graphics.Typeface.create(
            "sans",
            bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL
        ));
        return view;
    }

    private GradientDrawable panel(int fill, int stroke, int radius, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        if (strokeWidth > 0) {
            drawable.setStroke(dp(strokeWidth), stroke);
        }
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
