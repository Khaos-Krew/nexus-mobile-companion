package com.khaoskrew.nexuscompanion;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Compile-time boundary for the Android preview.
 *
 * Platform networking and privileged authority remain disabled. Direct preview
 * builds may use a separately configured, signed HTTPS channel only for user-
 * approved application updates.
 */
public final class AppContract {
    public static final int MIN_SUPPORTED_API = 26;
    public static final boolean PREVIEW_MODE = true;
    public static final boolean NETWORK_ENABLED = false;
    public static final boolean PRIVILEGED_ACTIONS_ENABLED = false;
    public static final String VERSION_LABEL = "0.2.1 Preview";

    public static final List<Destination> DESTINATIONS = Collections.unmodifiableList(Arrays.asList(
        new Destination("home", "Home", "⌂"),
        new Destination("dnd", "D&D", "✦"),
        new Destination("servers", "Servers", "▦"),
        new Destination("ai", "Nexus AI", "◉"),
        new Destination("notifications", "Alerts", "!"),
        new Destination("settings", "Settings", "⚙")
    ));

    private AppContract() {
    }

    public static final class Destination {
        public final String id;
        public final String label;
        public final String glyph;

        public Destination(String id, String label, String glyph) {
            this.id = id;
            this.label = label;
            this.glyph = glyph;
        }
    }
}
