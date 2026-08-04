package com.khaoskrew.nexuscompanion.identity;

import java.util.Objects;
import java.util.Optional;

/** Coordinates local state with encrypted persistence; network exchange remains backend-owned. */
public final class SessionCoordinator {
    private final SecureSessionStore store;
    private final SessionStateMachine stateMachine;

    public SessionCoordinator(SecureSessionStore store, SessionStateMachine stateMachine) {
        this.store = Objects.requireNonNull(store, "store");
        this.stateMachine = Objects.requireNonNull(stateMachine, "stateMachine");
    }

    public SessionSnapshot restore(long nowEpochSeconds, long clockSkewSeconds) {
        try {
            Optional<SessionTokens> restored = store.load();
            if (!restored.isPresent()) {
                stateMachine.signOut();
            } else if (!stateMachine.restoreForRefresh(
                restored.get(),
                nowEpochSeconds,
                clockSkewSeconds
            )) {
                bestEffortClear();
            }
        } catch (SecureStoreException error) {
            stateMachine.secureStorageFailed(error);
            bestEffortClear();
        }
        return stateMachine.snapshot();
    }

    public SessionSnapshot completeAuthorization(
        AuthCallback callback,
        SessionTokens tokens,
        CapabilitySet capabilities
    ) {
        stateMachine.completeAuthorization(callback, tokens, capabilities);
        return persistOrFailClosed(tokens);
    }

    public SessionSnapshot completeRefresh(
        SessionTokens rotatedTokens,
        CapabilitySet capabilities
    ) {
        stateMachine.completeRefresh(rotatedTokens, capabilities);
        return persistOrFailClosed(rotatedTokens);
    }

    public SessionSnapshot refreshRejected(boolean revoked, String safeCode) {
        stateMachine.refreshRejected(revoked, safeCode);
        bestEffortClear();
        return stateMachine.snapshot();
    }

    public SessionSnapshot revoke() {
        stateMachine.revoke();
        bestEffortClear();
        return stateMachine.snapshot();
    }

    public SessionSnapshot signOut() {
        stateMachine.signOut();
        bestEffortClear();
        return stateMachine.snapshot();
    }

    public SessionSnapshot snapshot() {
        return stateMachine.snapshot();
    }

    private SessionSnapshot persistOrFailClosed(SessionTokens tokens) {
        SessionSnapshot snapshot = stateMachine.snapshot();
        if (!snapshot.isAuthenticated()) {
            bestEffortClear();
            return snapshot;
        }
        try {
            store.save(tokens);
            return snapshot;
        } catch (SecureStoreException error) {
            stateMachine.secureStorageFailed(error);
            bestEffortClear();
            return stateMachine.snapshot();
        }
    }

    private void bestEffortClear() {
        try {
            store.clear();
        } catch (SecureStoreException ignored) {
            // The state machine is already fail-closed. Never log storage causes or tokens.
        }
    }
}
