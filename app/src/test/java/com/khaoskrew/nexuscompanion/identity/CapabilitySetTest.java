package com.khaoskrew.nexuscompanion.identity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class CapabilitySetTest {
    @Test
    public void signedOutAllowsOnlyPublicDestinations() {
        CapabilitySet capabilities = CapabilitySet.signedOut();

        assertTrue(capabilities.allowsDestination("home"));
        assertTrue(capabilities.allowsDestination("account"));
        assertTrue(capabilities.allowsDestination("settings"));
        assertFalse(capabilities.allowsDestination("dnd"));
        assertFalse(capabilities.allowsDestination("servers"));
        assertFalse(capabilities.allowsDestination("ai"));
        assertFalse(capabilities.allowsDestination("notifications"));
    }

    @Test
    public void capabilityNamesControlProtectedDestinations() {
        CapabilitySet capabilities = CapabilitySet.of(
            1,
            List.of(CapabilitySet.DND_READ, CapabilitySet.NOTIFICATIONS_READ)
        );

        assertTrue(capabilities.allowsDestination("dnd"));
        assertTrue(capabilities.allowsDestination("notifications"));
        assertFalse(capabilities.allowsDestination("servers"));
        assertFalse(capabilities.allows(CapabilitySet.GUARDED_ACTIONS));
    }

    @Test
    public void incompatibleContractDeniesEverythingProtected() {
        CapabilitySet capabilities = CapabilitySet.incompatible(99);

        assertFalse(capabilities.isCompatible());
        assertFalse(capabilities.allows(CapabilitySet.DND_READ));
        assertFalse(capabilities.allowsDestination("dnd"));
    }

    @Test
    public void previewFixtureNeverGrantsActions() {
        CapabilitySet capabilities = CapabilitySet.previewReadOnlyFixture();

        assertTrue(capabilities.hasAnyProtectedCapability());
        assertFalse(capabilities.allows(CapabilitySet.GUARDED_ACTIONS));
        assertFalse(capabilities.allows(CapabilitySet.DEVICES_REVOKE));
    }
}
