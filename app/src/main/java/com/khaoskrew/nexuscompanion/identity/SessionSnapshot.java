package com.khaoskrew.nexuscompanion.identity;

import java.util.Objects;

/** Immutable public view of the local session state. */
public final class SessionSnapshot {
    private final SessionState state;
    private final SessionTokens tokens;
    private final CapabilitySet capabilities;
    private final String safeErrorCode;

    private SessionSnapshot(
        SessionState state,
        SessionTokens tokens,
        CapabilitySet capabilities,
        String safeErrorCode
    ) {
        this.state = Objects.requireNonNull(state, "state");
        this.tokens = tokens;
        this.capabilities = Objects.requireNonNull(capabilities, "capabilities");
        this.safeErrorCode = safeErrorCode;
    }

    public static SessionSnapshot signedOut() {
        return new SessionSnapshot(
            SessionState.SIGNED_OUT,
            null,
            CapabilitySet.signedOut(),
            null
        );
    }

    public static SessionSnapshot of(
        SessionState state,
        SessionTokens tokens,
        CapabilitySet capabilities,
        String safeErrorCode
    ) {
        if ((state == SessionState.ACTIVE || state == SessionState.REFRESHING) && tokens == null) {
            throw new IllegalArgumentException("Active and refreshing states require tokens");
        }
        if (state != SessionState.ACTIVE && state != SessionState.REFRESHING) {
            capabilities = CapabilitySet.signedOut();
        }
        return new SessionSnapshot(state, tokens, capabilities, safeErrorCode);
    }

    public SessionState state() {
        return state;
    }

    SessionTokens tokens() {
        return tokens;
    }

    public CapabilitySet capabilities() {
        return state == SessionState.ACTIVE ? capabilities : CapabilitySet.signedOut();
    }

    public String safeErrorCode() {
        return safeErrorCode;
    }

    public boolean isAuthenticated() {
        return state == SessionState.ACTIVE;
    }

    public boolean allows(String capability) {
        return isAuthenticated() && capabilities.allows(capability);
    }

    public boolean allowsDestination(String destinationId) {
        return capabilities().allowsDestination(destinationId);
    }

    @Override
    public String toString() {
        return "SessionSnapshot{state="
            + state
            + ", authenticated="
            + isAuthenticated()
            + ", capabilityCount="
            + capabilities().values().size()
            + ", safeErrorCode="
            + safeErrorCode
            + "}";
    }
}
