package com.khaoskrew.nexuscompanion.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;

import org.junit.Test;

public final class SessionStateMachineTest {
    private static final URI REDIRECT = URI.create("khaosnexus://auth/callback");

    @Test
    public void authorizationActivatesOnlyValidatedCapabilities() {
        SessionStateMachine machine = new SessionStateMachine();
        AuthorizationAttempt attempt = machine.startAuthorization(1_000);
        AuthCallback callback = AuthCallback.validate(
            attempt,
            REDIRECT,
            URI.create(REDIRECT + "?code=one-time&state=" + attempt.state()),
            1_010,
            300
        );

        machine.completeAuthorization(
            callback,
            tokens(0, 1_100, 2_000),
            CapabilitySet.of(1, List.of(CapabilitySet.DND_READ))
        );

        SessionSnapshot snapshot = machine.snapshot();
        assertEquals(SessionState.ACTIVE, snapshot.state());
        assertTrue(snapshot.allows(CapabilitySet.DND_READ));
        assertFalse(snapshot.allows(CapabilitySet.SERVERS_READ));
        assertFalse(snapshot.toString().contains("access-secret"));
    }

    @Test
    public void restoredSessionHasNoCapabilitiesUntilBackendRefresh() {
        SessionStateMachine machine = new SessionStateMachine();

        assertTrue(machine.restoreForRefresh(tokens(1, 1_100, 2_000), 1_000, 30));
        assertEquals(SessionState.REFRESHING, machine.snapshot().state());
        assertFalse(machine.snapshot().allowsDestination("dnd"));

        machine.completeRefresh(
            tokens(2, 1_500, 2_500),
            CapabilitySet.of(1, List.of(CapabilitySet.DND_READ))
        );
        assertEquals(SessionState.ACTIVE, machine.snapshot().state());
        assertTrue(machine.snapshot().allowsDestination("dnd"));
    }

    @Test
    public void reusedRefreshRotationFailsClosedAsRevoked() {
        SessionStateMachine machine = new SessionStateMachine();
        machine.restoreForRefresh(tokens(4, 1_100, 2_000), 1_000, 0);
        machine.completeRefresh(
            tokens(4, 1_500, 2_500),
            CapabilitySet.previewReadOnlyFixture()
        );

        assertEquals(SessionState.REVOKED, machine.snapshot().state());
        assertFalse(machine.snapshot().capabilities().hasAnyProtectedCapability());
        assertEquals("refresh_replay_detected", machine.snapshot().safeErrorCode());
    }

    @Test
    public void expiredRestoredRefreshTokenNeverEntersRefreshing() {
        SessionStateMachine machine = new SessionStateMachine();

        assertFalse(machine.restoreForRefresh(tokens(1, 900, 950), 1_000, 0));
        assertEquals(SessionState.EXPIRED, machine.snapshot().state());
        assertFalse(machine.snapshot().isAuthenticated());
    }

    @Test
    public void secureStorageFailureClearsAuthority() {
        SessionStateMachine machine = activeMachine();
        machine.secureStorageFailed(new IllegalStateException("access_token=secret"));

        assertEquals(SessionState.RECOVERABLE_ERROR, machine.snapshot().state());
        assertFalse(machine.snapshot().isAuthenticated());
        assertFalse(machine.snapshot().allowsDestination("servers"));
        assertEquals("secure_storage_unavailable", machine.snapshot().safeErrorCode());
    }

    private static SessionStateMachine activeMachine() {
        SessionStateMachine machine = new SessionStateMachine();
        AuthorizationAttempt attempt = machine.startAuthorization(1_000);
        AuthCallback callback = AuthCallback.validate(
            attempt,
            REDIRECT,
            URI.create(REDIRECT + "?code=one-time&state=" + attempt.state()),
            1_010,
            300
        );
        machine.completeAuthorization(
            callback,
            tokens(0, 1_100, 2_000),
            CapabilitySet.previewReadOnlyFixture()
        );
        return machine;
    }

    private static SessionTokens tokens(
        long rotation,
        long accessExpiry,
        long refreshExpiry
    ) {
        return new SessionTokens(
            "access-secret-" + rotation,
            "refresh-secret-" + rotation,
            accessExpiry,
            refreshExpiry,
            "device-private-id",
            "subject-private-id",
            rotation
        );
    }
}
