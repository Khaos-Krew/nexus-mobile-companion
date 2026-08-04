package com.khaoskrew.nexuscompanion.identity;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** Immutable, fail-closed capability projection returned by the shared backend. */
public final class CapabilitySet {
    public static final String DND_READ = "dnd.read";
    public static final String SERVERS_READ = "servers.read";
    public static final String NEXUS_AI_READ = "nexus-ai.read";
    public static final String NOTIFICATIONS_READ = "notifications.read";
    public static final String DEVICES_READ = "devices.read";
    public static final String DEVICES_REVOKE = "devices.revoke";
    public static final String GUARDED_ACTIONS = "guarded-actions.request";

    private final Set<String> values;
    private final int contractVersion;
    private final boolean compatible;

    private CapabilitySet(Set<String> values, int contractVersion, boolean compatible) {
        this.values = Collections.unmodifiableSet(values);
        this.contractVersion = contractVersion;
        this.compatible = compatible;
    }

    public static CapabilitySet signedOut() {
        return new CapabilitySet(new LinkedHashSet<>(), 0, true);
    }

    public static CapabilitySet incompatible(int contractVersion) {
        return new CapabilitySet(new LinkedHashSet<>(), contractVersion, false);
    }

    public static CapabilitySet of(int contractVersion, Collection<String> capabilities) {
        if (contractVersion <= 0) {
            throw new IllegalArgumentException("contractVersion must be positive");
        }
        Objects.requireNonNull(capabilities, "capabilities");
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String capability : capabilities) {
            if (capability == null || capability.trim().isEmpty()) {
                throw new IllegalArgumentException("Capability names cannot be blank");
            }
            normalized.add(capability.trim());
        }
        return new CapabilitySet(normalized, contractVersion, true);
    }

    /** Safe local fixture for rendering; never grants action capability. */
    public static CapabilitySet previewReadOnlyFixture() {
        LinkedHashSet<String> fixture = new LinkedHashSet<>();
        fixture.add(DND_READ);
        fixture.add(SERVERS_READ);
        fixture.add(NEXUS_AI_READ);
        fixture.add(NOTIFICATIONS_READ);
        fixture.add(DEVICES_READ);
        return new CapabilitySet(fixture, 1, true);
    }

    public boolean allows(String capability) {
        return compatible && capability != null && values.contains(capability);
    }

    public boolean allowsDestination(String destinationId) {
        if (destinationId == null) {
            return false;
        }
        switch (destinationId) {
            case "home":
            case "account":
            case "settings":
                return true;
            case "dnd":
                return allows(DND_READ);
            case "servers":
                return allows(SERVERS_READ);
            case "ai":
                return allows(NEXUS_AI_READ);
            case "notifications":
                return allows(NOTIFICATIONS_READ);
            default:
                return false;
        }
    }

    public Set<String> values() {
        return values;
    }

    public int contractVersion() {
        return contractVersion;
    }

    public boolean isCompatible() {
        return compatible;
    }

    public boolean hasAnyProtectedCapability() {
        return compatible && !values.isEmpty();
    }

    @Override
    public String toString() {
        return "CapabilitySet{contractVersion="
            + contractVersion
            + ", compatible="
            + compatible
            + ", capabilityCount="
            + values.size()
            + "}";
    }
}
