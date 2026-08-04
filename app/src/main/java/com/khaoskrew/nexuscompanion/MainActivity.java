package com.khaoskrew.nexuscompanion;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public final class MainActivity extends Activity {
    private static final int BLACK = Color.rgb(5, 6, 9);
    private static final int CHARCOAL = Color.rgb(11, 13, 18);
    private static final int PANEL = Color.rgb(17, 19, 26);
    private static final int PANEL_STRONG = Color.rgb(12, 14, 20);
    private static final int RUBY = Color.rgb(215, 25, 54);
    private static final int CRIMSON = Color.rgb(255, 53, 86);
    private static final int EMBER = Color.rgb(255, 106, 61);
    private static final int VIOLET = Color.rgb(169, 60, 255);
    private static final int TEXT = Color.rgb(241, 237, 239);
    private static final int MUTED = Color.rgb(172, 162, 168);
    private static final int DIM = Color.rgb(112, 105, 113);
    private static final int GOOD = Color.rgb(79, 220, 154);
    private static final int WARNING = Color.rgb(255, 188, 92);

    private final List<Button> navButtons = new ArrayList<>();
    private FrameLayout contentHost;
    private TextView headerTitle;
    private TextView headerSubtitle;
    private String currentDestination = "home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        setContentView(buildApplicationShell());
        renderDestination("home");
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(BLACK);
        window.setNavigationBarColor(BLACK);
    }

    private View buildApplicationShell() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(BLACK);
        root.addView(new NexusBackdropView(this), matchParent());

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setPadding(0, dp(4), 0, 0);
        root.addView(shell, matchParent());

        shell.addView(buildHeader(), new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        contentHost = new FrameLayout(this);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            1f
        );
        shell.addView(contentHost, contentParams);
        shell.addView(buildBottomNavigation(), new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return root;
    }

    private View buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(12), dp(14), dp(12));
        header.setBackground(panelBackground(Color.argb(235, 7, 8, 12), Color.argb(65, 255, 53, 86), dp(0), 0));

        TextView mark = text("N", 18, Color.WHITE, true);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(panelBackground(RUBY, Color.argb(130, 255, 105, 128), dp(14), 1));
        mark.setContentDescription("Khaos Nexus mark");
        header.addView(mark, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(12), 0, dp(8), 0);
        headerTitle = text("Home", 19, TEXT, true);
        headerSubtitle = text("Mobile command network", 11, MUTED, false);
        titles.addView(headerTitle);
        titles.addView(headerSubtitle);
        header.addView(titles, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView mode = text("OFFLINE PREVIEW", 9, Color.rgb(255, 151, 168), true);
        mode.setGravity(Gravity.CENTER);
        mode.setPadding(dp(9), dp(7), dp(9), dp(7));
        mode.setBackground(panelBackground(Color.argb(120, 95, 8, 27), Color.argb(120, 255, 53, 86), dp(18), 1));
        mode.setContentDescription("Offline preview mode. Live controls disabled.");
        header.addView(mode);
        return header;
    }

    private View buildBottomNavigation() {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.argb(248, 6, 7, 10));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8), dp(8), dp(8), dp(10));
        scroll.addView(nav, new HorizontalScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        for (AppContract.Destination destination : AppContract.DESTINATIONS) {
            Button button = new Button(this);
            button.setTag(destination.id);
            button.setText(destination.glyph + "\n" + destination.label);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            button.setTextColor(MUTED);
            button.setAllCaps(false);
            button.setGravity(Gravity.CENTER);
            button.setMinWidth(dp(68));
            button.setMinHeight(dp(58));
            button.setPadding(dp(7), dp(5), dp(7), dp(5));
            button.setBackground(navBackground(false));
            button.setContentDescription("Open " + destination.label);
            button.setOnClickListener(view -> renderDestination((String) view.getTag()));
            navButtons.add(button);
            nav.addView(button, new LinearLayout.LayoutParams(0, dp(62), 1f));
        }
        return scroll;
    }

    private void renderDestination(String id) {
        currentDestination = id;
        for (Button button : navButtons) {
            boolean selected = id.equals(button.getTag());
            button.setTextColor(selected ? Color.WHITE : MUTED);
            button.setBackground(navBackground(selected));
            button.setSelected(selected);
        }

        AppContract.Destination destination = findDestination(id);
        headerTitle.setText(destination.label);
        headerSubtitle.setText(subtitleFor(id));

        contentHost.removeAllViews();
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setPadding(0, 0, 0, dp(12));

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(16), dp(16), dp(16), dp(30));
        switch (id) {
            case "dnd":
                buildDndPage(page);
                break;
            case "servers":
                buildServersPage(page);
                break;
            case "ai":
                buildAiPage(page);
                break;
            case "notifications":
                buildNotificationsPage(page);
                break;
            case "settings":
                buildSettingsPage(page);
                break;
            case "home":
            default:
                buildHomePage(page);
                break;
        }
        scroll.addView(page, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        contentHost.addView(scroll, matchParent());
    }

    private void buildHomePage(LinearLayout page) {
        page.addView(hero(
            "NEXUS COMMAND LINK",
            "Your Khaos Nexus companion is installed",
            "This first APK proves the mobile shell, visual system, navigation, and security boundaries on a real Android device. Live platform data remains intentionally disconnected.",
            CRIMSON
        ));

        page.addView(sectionLabel("SYSTEM SNAPSHOT"));
        LinearLayout metrics = row();
        metrics.addView(metric("Desktop", "Awaiting link", WARNING), weight());
        metrics.addView(metric("Mobile API", "Not provisioned", WARNING), weight());
        page.addView(metrics);

        page.addView(card(
            "Ready for device testing",
            "Preview foundation",
            "Navigate every planned module, verify scaling and readability, and confirm the app installs cleanly on Android 8.0 or newer.",
            GOOD,
            null
        ));

        page.addView(card(
            "Live authority remains protected",
            "Security boundary",
            "This APK contains no RCON, Discord bot token, server password, provider key, AI token, scheduler, or production endpoint. Privileged actions unlock only after the shared mobile API and authentication phases are reviewed.",
            CRIMSON,
            null
        ));

        page.addView(sectionLabel("QUICK DESTINATIONS"));
        page.addView(destinationCard("D&D Command Table", "Campaigns, characters, sessions, maps, dice, and safe AI proposals.", "dnd", EMBER));
        page.addView(destinationCard("Server Watch", "Read-only status previews and future guarded operations.", "servers", CRIMSON));
        page.addView(destinationCard("Nexus AI", "Service health and advisory-review surfaces without autonomous execution.", "ai", VIOLET));
    }

    private void buildDndPage(LinearLayout page) {
        page.addView(hero(
            "EMBERFORGE ARCHIVE",
            "D&D at the table and on the move",
            "A mobile-first home for campaigns, characters, upcoming sessions, encounter context, player-safe maps, dice history, and reviewed AI assistance.",
            EMBER
        ));

        page.addView(sectionLabel("CAMPAIGN PREVIEW"));
        page.addView(card("Ashes of the Nexus", "Next session · Saturday 7:00 PM", "Party level 5 · 4 characters · Last sync: preview fixture", EMBER, statusPill("OFFLINE FIXTURE", WARNING)));
        page.addView(card("Vorkesh Emberforge", "Dragonborn Artificer", "Armor calibrated · Infusions prepared · Character controls remain read-only in this preview.", CRIMSON, statusPill("READY", GOOD)));

        page.addView(sectionLabel("TABLE TOOLS"));
        LinearLayout tools = row();
        tools.addView(miniCard("Sessions", "Recaps and attendance", "3"), weight());
        tools.addView(miniCard("Dice", "Recent roll history", "12"), weight());
        page.addView(tools);
        LinearLayout toolsTwo = row();
        toolsTwo.addView(miniCard("Maps", "Player-safe views", "2"), weight());
        toolsTwo.addView(miniCard("AI", "Proposals to review", "1"), weight());
        page.addView(toolsTwo);

        page.addView(disabledActionCard(
            "Live campaign connection",
            "Authentication, campaign capability bootstrap, encrypted offline storage, and the versioned mobile API are required before real campaign records can load."
        ));
    }

    private void buildServersPage(LinearLayout page) {
        page.addView(hero(
            "CONNECTED WORLDS",
            "Server monitoring without unsafe shortcuts",
            "The mobile companion will consume normalized server projections and guarded command lifecycles. It will never connect to RCON or hosting providers directly.",
            CRIMSON
        ));

        page.addView(sectionLabel("SERVER PREVIEW"));
        page.addView(serverCard("ARK · Ragnarok", "Online", "12 / 70 players", GOOD));
        page.addView(serverCard("ARK · Astraeos", "Restart scheduled", "5:50 AM warning · 6:00 AM restart", WARNING));
        page.addView(serverCard("Palworld", "Offline fixture", "Shared API not connected", DIM));
        page.addView(serverCard("Minecraft", "Online fixture", "Just Create Season 2", GOOD));

        page.addView(disabledActionCard(
            "Guarded actions locked",
            "Save, broadcast, restart, kick, and ban require account authentication, server-side permission revalidation, idempotency keys, explicit confirmation, expiry, and audit evidence."
        ));
    }

    private void buildAiPage(LinearLayout page) {
        page.addView(hero(
            "NEXUS INTELLIGENCE CORE",
            "Advisory services, separated by design",
            "D&D AI and Nexus AI Core remain independent services. Mobile surfaces review health and proposals; they do not host an AI runtime or execute maintenance.",
            VIOLET
        ));

        page.addView(sectionLabel("SERVICE PREVIEW"));
        page.addView(card("D&D AI", "Campaign intelligence", "Co-DM drafts, homebrew proposals, map proposals, and explicit AI Game Master turns. Only approved D&D context may be shared.", EMBER, statusPill("NOT CONNECTED", WARNING)));
        page.addView(card("Nexus AI Core", "Platform intelligence", "Game and mod update findings, assistance, and maintenance plans remain advisory and isolated from campaign records.", VIOLET, statusPill("NOT CONNECTED", WARNING)));
        page.addView(card("Authority contract", "Review before action", "No AI output can run a server command, change permissions, post publicly, schedule work, download updates, or mutate campaign state automatically.", CRIMSON, statusPill("ENFORCED", GOOD)));
    }

    private void buildNotificationsPage(LinearLayout page) {
        page.addView(hero(
            "ACTIONABLE INBOX",
            "Minimal alerts with safe detail fetch",
            "Future push payloads contain no secrets. Opening an alert requires authentication before authorized details are fetched from shared services.",
            CRIMSON
        ));

        page.addView(sectionLabel("PREVIEW ALERTS"));
        page.addView(notificationCard("Server restart warning", "Ragnarok restart in 10 minutes", "Preview · 2m ago", WARNING));
        page.addView(notificationCard("D&D session reminder", "Ashes of the Nexus begins Saturday at 7:00 PM", "Preview · 1h ago", EMBER));
        page.addView(notificationCard("Nexus AI finding", "One mod update requires owner review", "Preview · Yesterday", VIOLET));
        page.addView(notificationCard("Security notice", "This device is not registered with a live account", "Local · Now", CRIMSON));
    }

    private void buildSettingsPage(LinearLayout page) {
        page.addView(hero(
            "CONFIGURATION MATRIX",
            "Safe defaults for the preview build",
            "The first APK stores no credentials and exposes no production configuration. These controls demonstrate the intended mobile settings hierarchy.",
            CRIMSON
        ));

        page.addView(sectionLabel("PREVIEW SETTINGS"));
        page.addView(settingToggle("Reduced motion", "Animations are already disabled in this foundation.", true, false));
        page.addView(settingToggle("Hide sensitive previews", "Protect lock-screen and task-switcher content in a future authenticated build.", true, false));
        page.addView(settingToggle("Push notifications", "Requires device registration and shared notification services.", false, false));

        page.addView(card("Build information", AppContract.VERSION_LABEL, "Package: com.khaoskrew.nexuscompanion.preview\nMinimum Android: 8.0 (API 26)\nMode: Offline fixture\nNetwork permission: Not requested", CRIMSON, statusPill("INSTALLABLE APK", GOOD)));
        page.addView(card("Repository boundary", "Khaos-Krew/nexus-mobile-companion", "All mobile-only code, branches, pull requests, CI, and artifacts remain in the dedicated mobile repository.", CRIMSON, null));
    }

    private View destinationCard(String title, String body, String destination, int accent) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setText(title + "\n" + body + "\n\nOPEN  →");
        button.setTextColor(TEXT);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        button.setPadding(dp(18), dp(16), dp(18), dp(16));
        button.setBackground(panelBackground(Color.argb(235, 14, 16, 22), Color.argb(115, Color.red(accent), Color.green(accent), Color.blue(accent)), dp(16), 1));
        button.setContentDescription("Open " + title);
        button.setOnClickListener(view -> renderDestination(destination));
        button.setLayoutParams(blockParams());
        return button;
    }

    private View hero(String eyebrow, String title, String body, int accent) {
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(22), dp(22), dp(22), dp(22));
        hero.setBackground(gradientCard(accent));
        hero.setLayoutParams(blockParams(dp(18)));

        TextView eye = text(eyebrow, 10, blend(accent, Color.WHITE, 0.38f), true);
        eye.setLetterSpacing(0.18f);
        hero.addView(eye);

        TextView heading = text(title, 27, Color.WHITE, true);
        heading.setPadding(0, dp(8), 0, dp(8));
        hero.addView(heading);

        TextView description = text(body, 14, Color.rgb(205, 196, 201), false);
        description.setLineSpacing(0, 1.25f);
        hero.addView(description);

        TextView boundary = text(AppContract.PREVIEW_MODE ? "● OFFLINE-SAFE PREVIEW" : "● CONNECTED", 10, AppContract.PREVIEW_MODE ? WARNING : GOOD, true);
        boundary.setPadding(0, dp(18), 0, 0);
        hero.addView(boundary);
        return hero;
    }

    private View card(String title, String subtitle, String body, int accent, View trailing) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(17), dp(18), dp(17));
        card.setBackground(panelBackground(Color.argb(238, 16, 18, 25), Color.argb(85, Color.red(accent), Color.green(accent), Color.blue(accent)), dp(16), 1));
        card.setLayoutParams(blockParams());

        LinearLayout headingRow = row();
        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.VERTICAL);
        heading.addView(text(title, 17, TEXT, true));
        heading.addView(text(subtitle, 11, blend(accent, Color.WHITE, 0.28f), true));
        headingRow.addView(heading, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (trailing != null) {
            headingRow.addView(trailing);
        }
        card.addView(headingRow);

        TextView bodyView = text(body, 13, MUTED, false);
        bodyView.setPadding(0, dp(12), 0, 0);
        bodyView.setLineSpacing(0, 1.22f);
        card.addView(bodyView);
        return card;
    }

    private View serverCard(String name, String state, String detail, int accent) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(15), dp(16), dp(15));
        card.setBackground(panelBackground(Color.argb(238, 15, 17, 23), Color.argb(78, Color.red(accent), Color.green(accent), Color.blue(accent)), dp(15), 1));
        card.setLayoutParams(blockParams());

        View dot = new View(this);
        dot.setBackground(circle(accent));
        card.addView(dot, new LinearLayout.LayoutParams(dp(10), dp(10)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(13), 0, dp(8), 0);
        copy.addView(text(name, 16, TEXT, true));
        copy.addView(text(detail, 11, DIM, false));
        card.addView(copy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        card.addView(statusPill(state.toUpperCase(), accent));
        return card;
    }

    private View notificationCard(String title, String body, String meta, int accent) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.TOP);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(panelBackground(Color.argb(238, 15, 17, 23), Color.argb(72, Color.red(accent), Color.green(accent), Color.blue(accent)), dp(15), 1));
        card.setLayoutParams(blockParams());

        TextView icon = text("!", 15, Color.WHITE, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(circle(accent));
        card.addView(icon, new LinearLayout.LayoutParams(dp(34), dp(34)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(13), 0, 0, 0);
        copy.addView(text(title, 15, TEXT, true));
        TextView bodyView = text(body, 12, MUTED, false);
        bodyView.setPadding(0, dp(4), 0, dp(7));
        copy.addView(bodyView);
        copy.addView(text(meta, 10, DIM, false));
        card.addView(copy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return card;
    }

    private View disabledActionCard(String title, String explanation) {
        LinearLayout card = (LinearLayout) card(title, "Connected phase required", explanation, CRIMSON, statusPill("LOCKED", DIM));
        Button button = new Button(this);
        button.setText("LIVE ACTIONS DISABLED");
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        button.setTextColor(Color.rgb(123, 116, 121));
        button.setAllCaps(false);
        button.setEnabled(false);
        button.setPadding(dp(12), dp(9), dp(12), dp(9));
        button.setBackground(panelBackground(Color.rgb(23, 24, 29), Color.rgb(55, 55, 61), dp(12), 1));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(15);
        card.addView(button, params);
        return card;
    }

    private View settingToggle(String title, String body, boolean checked, boolean enabled) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(15), dp(12), dp(15));
        row.setBackground(panelBackground(Color.argb(238, 15, 17, 23), Color.argb(50, 255, 53, 86), dp(15), 1));
        row.setLayoutParams(blockParams());

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.addView(text(title, 15, enabled ? TEXT : MUTED, true));
        copy.addView(text(body, 11, DIM, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Switch toggle = new Switch(this);
        toggle.setChecked(checked);
        toggle.setEnabled(enabled);
        toggle.setContentDescription(title);
        toggle.setThumbTintList(new ColorStateList(
            new int[][] { new int[] { android.R.attr.state_checked }, new int[] {} },
            new int[] { CRIMSON, Color.rgb(90, 88, 92) }
        ));
        row.addView(toggle);
        return row;
    }

    private View miniCard(String title, String body, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(14), dp(15), dp(14));
        card.setBackground(panelBackground(Color.argb(235, 15, 17, 23), Color.argb(60, 255, 53, 86), dp(14), 1));
        TextView number = text(value, 23, Color.WHITE, true);
        card.addView(number);
        card.addView(text(title, 13, TEXT, true));
        TextView detail = text(body, 10, DIM, false);
        detail.setPadding(0, dp(4), 0, 0);
        card.addView(detail);
        return card;
    }

    private View metric(String title, String value, int accent) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(13), dp(14), dp(13));
        card.setBackground(panelBackground(Color.argb(236, 15, 17, 23), Color.argb(70, Color.red(accent), Color.green(accent), Color.blue(accent)), dp(14), 1));
        card.addView(text(title.toUpperCase(), 9, DIM, true));
        TextView valueView = text(value, 14, TEXT, true);
        valueView.setPadding(0, dp(7), 0, 0);
        card.addView(valueView);
        return card;
    }

    private TextView sectionLabel(String label) {
        TextView view = text(label, 10, Color.rgb(125, 111, 118), true);
        view.setLetterSpacing(0.16f);
        view.setPadding(dp(4), dp(12), 0, dp(9));
        return view;
    }

    private TextView statusPill(String value, int accent) {
        TextView pill = text(value, 9, blend(accent, Color.WHITE, 0.3f), true);
        pill.setGravity(Gravity.CENTER);
        pill.setPadding(dp(9), dp(6), dp(9), dp(6));
        pill.setBackground(panelBackground(Color.argb(62, Color.red(accent), Color.green(accent), Color.blue(accent)), Color.argb(105, Color.red(accent), Color.green(accent), Color.blue(accent)), dp(20), 1));
        return pill;
    }

    private AppContract.Destination findDestination(String id) {
        for (AppContract.Destination destination : AppContract.DESTINATIONS) {
            if (destination.id.equals(id)) {
                return destination;
            }
        }
        return AppContract.DESTINATIONS.get(0);
    }

    private String subtitleFor(String id) {
        switch (id) {
            case "dnd": return "Campaign command table";
            case "servers": return "Connected worlds";
            case "ai": return "Advisory intelligence";
            case "notifications": return "Actionable inbox";
            case "settings": return "Device and security";
            default: return "Mobile command network";
        }
    }

    private LinearLayout row() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(10));
        return row;
    }

    private LinearLayout.LayoutParams weight() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(dp(4), 0, dp(4), 0);
        return params;
    }

    private LinearLayout.LayoutParams blockParams() {
        return blockParams(dp(10));
    }

    private LinearLayout.LayoutParams blockParams(int bottomMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = bottomMargin;
        return params;
    }

    private FrameLayout.LayoutParams matchParent() {
        return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private TextView text(String value, int sizeSp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        view.setTextColor(color);
        view.setEllipsize(TextUtils.TruncateAt.END);
        view.setTypeface(android.graphics.Typeface.create("sans", bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL));
        return view;
    }

    private GradientDrawable panelBackground(int fill, int stroke, int radius, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        if (strokeWidth > 0) {
            drawable.setStroke(dp(strokeWidth), stroke);
        }
        return drawable;
    }

    private GradientDrawable gradientCard(int accent) {
        GradientDrawable drawable = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[] {
                Color.argb(210, Math.max(20, Color.red(accent) / 2), Math.max(5, Color.green(accent) / 4), Math.max(10, Color.blue(accent) / 3)),
                Color.rgb(18, 19, 26),
                Color.rgb(8, 9, 13)
            }
        );
        drawable.setCornerRadius(dp(20));
        drawable.setStroke(dp(1), Color.argb(110, Color.red(accent), Color.green(accent), Color.blue(accent)));
        return drawable;
    }

    private GradientDrawable navBackground(boolean selected) {
        return panelBackground(
            selected ? Color.argb(145, 96, 9, 28) : Color.TRANSPARENT,
            selected ? Color.argb(120, 255, 53, 86) : Color.TRANSPARENT,
            dp(14),
            selected ? 1 : 0
        );
    }

    private GradientDrawable circle(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        return drawable;
    }

    private int blend(int first, int second, float secondWeight) {
        float firstWeight = 1f - secondWeight;
        return Color.rgb(
            Math.round(Color.red(first) * firstWeight + Color.red(second) * secondWeight),
            Math.round(Color.green(first) * firstWeight + Color.green(second) * secondWeight),
            Math.round(Color.blue(first) * firstWeight + Color.blue(second) * secondWeight)
        );
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class NexusBackdropView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path sigil = new Path();

        NexusBackdropView(Context context) {
            super(context);
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float width = getWidth();
            float height = getHeight();

            paint.setShader(new LinearGradient(0, 0, width, height, new int[] {
                Color.rgb(3, 4, 7),
                Color.rgb(10, 11, 16),
                Color.rgb(5, 6, 9)
            }, null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, width, height, paint);

            paint.setShader(new RadialGradient(width * 0.82f, height * 0.12f, width * 0.65f,
                new int[] { Color.argb(78, 215, 25, 54), Color.argb(20, 91, 10, 35), Color.TRANSPARENT },
                null, Shader.TileMode.CLAMP));
            canvas.drawCircle(width * 0.82f, height * 0.12f, width * 0.65f, paint);
            paint.setShader(null);

            paint.setColor(Color.argb(17, 255, 255, 255));
            paint.setStrokeWidth(1f);
            float step = getResources().getDisplayMetrics().density * 46f;
            for (float x = 0; x < width; x += step) {
                canvas.drawLine(x, 0, x, height, paint);
            }
            for (float y = 0; y < height; y += step) {
                canvas.drawLine(0, y, width, y, paint);
            }

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(getResources().getDisplayMetrics().density * 1.2f);
            paint.setColor(Color.argb(40, 255, 53, 86));
            float cx = width * 0.80f;
            float cy = height * 0.22f;
            for (int i = 1; i <= 4; i++) {
                canvas.drawCircle(cx, cy, step * i * 0.82f, paint);
            }

            sigil.reset();
            sigil.moveTo(width * 0.08f, height * 0.72f);
            sigil.lineTo(width * 0.28f, height * 0.60f);
            sigil.lineTo(width * 0.42f, height * 0.68f);
            sigil.lineTo(width * 0.58f, height * 0.52f);
            sigil.lineTo(width * 0.77f, height * 0.58f);
            sigil.lineTo(width * 0.94f, height * 0.41f);
            paint.setColor(Color.argb(52, 255, 53, 86));
            paint.setStrokeWidth(getResources().getDisplayMetrics().density * 2f);
            canvas.drawPath(sigil, paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }
}
