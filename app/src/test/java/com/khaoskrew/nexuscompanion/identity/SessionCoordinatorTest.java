package com.khaoskrew.nexuscompanion.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Optional;

import org.junit.Test;

public final class SessionCoordinatorTest {
    private static final URI REDIRECT = URI.create("khaosnexus://auth/callback");

    @Test
    public void restoredEncryptedSessionRequiresBackendRefresh() throws Exception {
        InMemoryStore store = new InMemoryStore();
        store.save(tokens(1));
        SessionCoordinator coordinator = new SessionCoordinator(
            store,
            new SessionStateMachine()
        );

        SessionSnapshot restored = coordinator.restore(1_000, 0);

        assertEquals(SessionState.REFRESHING, restored.state());
        assertFalse(restored.isAuthenticated());
        assertFalse(restored.allowsDestination("dnd"));
    }

    @Test
    public void persistenceFailureRemovesAuthenticatedAuthority() {
        InMemoryStore store = new InMemoryStore();
        store.failSave = true;
        SessionStateMachine machine = new SessionStateMachine();
        SessionCoordinator coordinator = new SessionCoordinator(store, machine);
        AuthorizationAttempt attempt = machine.startAuthorization(1_000);
        AuthCallback callback = AuthCallback.validate(
            attempt,
            REDIRECT,
            URI.create(REDIRECT + "?code=one-time&state=" + attempt.state()),
            1_010,
            300
        );

        SessionSnapshot result = coordinator.completeAuthorization(
            callback,
            tokens(0),
            CapabilitySet.previewReadOnlyFixture()
        );

        assertEquals(SessionState.RECOVERABLE_ERROR, result.state());
        assertFalse(result.isAuthenticated());
        assertEquals("secure_storage_unavailable", result.safeErrorCode());
        assertTrue(store.clearCount > 0);
    }

    private static SessionTokens tokens(long rotation) {
        return new SessionTokens(
            "access-" + rotation,
            "refresh-" + rotation,
            1_500,
            2_500,
            "device-private",
            "subject-private",
            rotation
        );
    }

    private static final class InMemoryStore implements SecureSessionStore {
        private SessionTokens tokens;
        private boolean failSave;
        private int clearCount;

        @Override
        public Optional<SessionTokens> load() {
            return Optional.ofNullable(tokens);
        }

        @Override
        public void save(SessionTokens tokens) throws SecureStoreException {
            if (failSave) {
                throw new SecureStoreException("secure_store_encrypt_failed", null);
            }
            this.tokens = tokens;
        }

        @Override
        public void clear() {
            tokens = null;
            clearCount += 1;
        }
    }
}
