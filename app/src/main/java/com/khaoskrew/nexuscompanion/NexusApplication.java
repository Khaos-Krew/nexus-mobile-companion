package com.khaoskrew.nexuscompanion;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

/**
 * Applies Android system-bar and display-cutout safe areas to every activity.
 *
 * Target API 36 uses enforced edge-to-edge rendering. Keeping this policy in the
 * Application layer protects both the companion shell and Update Center without
 * duplicating device-specific padding logic in individual screens.
 */
public final class NexusApplication extends Application
    implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        configureEdgeToEdge(activity);
        applySafeArea(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        applySafeArea(activity);
    }

    private void configureEdgeToEdge(Activity activity) {
        Window window = activity.getWindow();
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);

        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false);
        } else {
            View decor = window.getDecorView();
            decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }
    }

    private void applySafeArea(Activity activity) {
        View content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }

        content.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            SafeArea safeArea = resolveSafeArea(windowInsets);
            int bottomBreathingRoom = dp(activity, 4);
            view.setPadding(
                safeArea.left,
                safeArea.top,
                safeArea.right,
                safeArea.bottom + bottomBreathingRoom
            );
            return windowInsets;
        });
        content.post(content::requestApplyInsets);
    }

    static SafeArea resolveSafeArea(WindowInsets windowInsets) {
        if (Build.VERSION.SDK_INT >= 30) {
            Insets insets = windowInsets.getInsets(
                WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout()
            );
            return new SafeArea(insets.left, insets.top, insets.right, insets.bottom);
        }

        return new SafeArea(
            windowInsets.getSystemWindowInsetLeft(),
            windowInsets.getSystemWindowInsetTop(),
            windowInsets.getSystemWindowInsetRight(),
            windowInsets.getSystemWindowInsetBottom()
        );
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    static final class SafeArea {
        final int left;
        final int top;
        final int right;
        final int bottom;

        SafeArea(int left, int top, int right, int bottom) {
            this.left = Math.max(0, left);
            this.top = Math.max(0, top);
            this.right = Math.max(0, right);
            this.bottom = Math.max(0, bottom);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}