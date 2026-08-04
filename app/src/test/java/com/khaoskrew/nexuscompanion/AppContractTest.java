package com.khaoskrew.nexuscompanion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public final class AppContractTest {
    @Test
    public void previewBuildHasAllPrimaryDestinations() {
        Set<String> destinations = new HashSet<>();
        for (AppContract.Destination destination : AppContract.DESTINATIONS) {
            destinations.add(destination.id);
        }

        assertEquals(6, destinations.size());
        assertTrue(destinations.contains("home"));
        assertTrue(destinations.contains("dnd"));
        assertTrue(destinations.contains("servers"));
        assertTrue(destinations.contains("ai"));
        assertTrue(destinations.contains("notifications"));
        assertTrue(destinations.contains("settings"));
    }

    @Test
    public void firstApkCannotReachPrivilegedAuthorities() {
        assertTrue(AppContract.PREVIEW_MODE);
        assertFalse(AppContract.NETWORK_ENABLED);
        assertFalse(AppContract.PRIVILEGED_ACTIONS_ENABLED);
    }

    @Test
    public void minimumAndroidVersionMatchesIssueContract() {
        assertEquals(26, AppContract.MIN_SUPPORTED_API);
    }
}
