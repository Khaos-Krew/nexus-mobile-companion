package com.khaoskrew.nexuscompanion.identity;

import java.util.Objects;

/**
 * Local state coordinator. Protected capability access exists only while ACTIVE;
 * every failure, revocation, incompatibility, or storage error fails closed.
 */
public final class SessionStateMachine {
    private SessionState state = SessionState.SIGNED_OUT;
    private SessionTokens tokens;
    private CapabilitySet capabilities = CapabilitySet.signedOut();
    private AuthorizationAttempt authorizationAttempt;
    private String safeErrorCode;

    public synchronized AuthorizationAttempt startAuthorization(long nowEpochSeconds) {
        switch (state) {
            case SIGNED_OUT:
            case REVOKED:
            case EXPIRED:
            case INCOMPATIBLE:
            case RECOVERABLE_ERROR:
                break;
            default:
                throw new IllegalStateException("Cannot authorize from state " + state);
        }
        clearSensitiveState();
        authorizationAttempt = AuthorizationAttempt.create(nowEpochSeconds);
        state = SessionState.AUTHORIZING;
        return authorizationAttempt;
    }

    /**
     * Restored tokens never regain capabilities locally. They enter REFRESHING and
     * must be rotated and reauthorized by the shared backend before protected UI
     * can become visible.
     */
    public synchronized boolean restoreForRefresh(
        SessionTokens restoredTokens,
        long nowEpochSeconds,
        long clockSkewSeconds
    ) {
        requireState(SessionState.SIGNED_OUT);
        Objects.requireNonNull(restoredTokens, "restoredTokens");
        if (restoredTokens.refreshExpired(nowEpochSeconds, clockSkewSeconds)) {
            failClosed(SessionState.EXPIRED, "refresh_expired");
            return false;
        }
        tokens = restoredTokens;
        authorizationAttempt = null;
        capabilities = CapabilitySet.signedOut();
        safeErrorCode = null;
        state = SessionState.REFRESHING;
        return true;
    }

    public synchronized void completeAuthorization(
        AuthCallback callback,
        SessionTokens newTokens,
        CapabilitySet newCapabilities
    ) {
        requireState(SessionState.AUTHORIZING);
        Objects.requireNonNull(callback, "callback");
        if (!callback.isSuccess()) {
            failClosed(
                SessionState.RECOVERABLE_ERROR,
                callback.safeErrorCode() == null ? "authorization_rejected" : callback.safeErrorCode()
            );
            return;
        }
        activate(newTokens, newCapabilities);
        authorizationAttempt = null;
    }

    public synchronized void authorizationFailed(String safeCode) {
        requireState(SessionState.AUTHORIZING);
        failClosed(
            SessionState.RECOVERABLE_ERROR,
            RedactedDiagnostics.safeIdentifier(safeCode, "authorization_failed")
        );
    }

    public synchronized boolean beginRefresh(long nowEpochSeconds, long clockSkewSeconds) {
        requireState(SessionState.ACTIVE);
        if (tokens.refreshExpired(nowEpochSeconds, clockSkewSeconds)) {
            failClosed(SessionState.EXPIRED, "refresh_expired");
            return false;
        }
        state = SessionState.REFRESHING;
        capabilities = CapabilitySet.signedOut();
        safeErrorCode = null;
        return true;
    }

    public synchronized void completeRefresh(
        SessionTokens rotatedTokens,
        CapabilitySet refreshedCapabilities
    ) {
        requireState(SessionState.REFRESHING);
        if (tokens != null && rotatedTokens.rotationCounter() <= tokens.rotationCounter()) {
            failClosed(SessionState.REVOKED, "refresh_replay_detected");
            return;
        }
        activate(rotatedTokens, refreshedCapabilities);
    }

    public synchronized void refreshRejected(boolean revoked, String safeCode) {
        requireState(SessionState.REFRESHING);
        failClosed(
            revoked ? SessionState.REVOKED : SessionState.EXPIRED,
            RedactedDiagnostics.safeIdentifier(
                safeCode,
                revoked ? "device_revoked" : "refresh_rejected"
            )
        );
    }

    public synchronized void markIncompatible(int serverContractVersion) {
        clearSensitiveState();
        capabilities = CapabilitySet.incompatible(serverContractVersion);
        state = SessionState.INCOMPATIBLE;
        safeErrorCode = "incompatible_contract";
    }

    public synchronized void revoke() {
        failClosed(SessionState.REVOKED, "device_revoked");
    }

    public synchronized void expire() {
        failClosed(SessionState.EXPIRED, "session_expired");
    }

    public synchronized void secureStorageFailed(Throwable error) {
        failClosed(SessionState.RECOVERABLE_ERROR, "secure_storage_unavailable");
    }

    public synchronized void signOut() {
        clearSensitiveState();
        state = SessionState.SIGNED_OUT;
        safeErrorCode = null;
    }

    public synchronized SessionSnapshot snapshot() {
        return SessionSnapshot.of(state, tokens, capabilities, safeErrorCode);
    }

    public synchronized AuthorizationAttempt authorizationAttempt() {
        return authorizationAttempt;
    }

    private void activate(SessionTokens newTokens, CapabilitySet newCapabilities) {
        Objects.requireNonNull(newTokens, "newTokens");
        Objects.requireNonNull(newCapabilities, "newCapabilities");
        if (!newCapabilities.isCompatible()) {
            markIncompatible(newCapabilities.contractVersion());
            return;
        }
        tokens = newTokens;
        capabilities = newCapabilities;
        state = SessionState.ACTIVE;
        safeErrorCode = null;
    }

    private void failClosed(SessionState newState, String code) {
        clearSensitiveState();
        state = newState;
        safeErrorCode = RedactedDiagnostics.safeIdentifier(code, "session_error");
    }

    private void clearSensitiveState() {
        tokens = null;
        authorizationAttempt = null;
        capabilities = CapabilitySet.signedOut();
    }

    private void requireState(SessionState expected) {
        if (state != expected) {
            throw new IllegalStateException(
                "Expected session state " + expected + " but was " + state
            );
        }
    }
}
