package com.khaoskrew.nexuscompanion;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class NexusApplicationTest {
    @Test
    public void safeAreaClampsInvalidNegativeInsets() {
        NexusApplication.SafeArea safeArea = new NexusApplication.SafeArea(-10, 24, -3, 48);

        assertEquals(0, safeArea.left);
        assertEquals(24, safeArea.top);
        assertEquals(0, safeArea.right);
        assertEquals(48, safeArea.bottom);
    }

    @Test
    public void safeAreaKeepsDeviceInsetsUnchanged() {
        NexusApplication.SafeArea safeArea = new NexusApplication.SafeArea(12, 36, 14, 62);

        assertEquals(12, safeArea.left);
        assertEquals(36, safeArea.top);
        assertEquals(14, safeArea.right);
        assertEquals(62, safeArea.bottom);
    }
}
