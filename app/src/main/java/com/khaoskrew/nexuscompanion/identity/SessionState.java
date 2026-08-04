package com.khaoskrew.nexuscompanion.identity;

public enum SessionState {
    SIGNED_OUT,
    AUTHORIZING,
    ACTIVE,
    REFRESHING,
    REVOKED,
    EXPIRED,
    INCOMPATIBLE,
    RECOVERABLE_ERROR
}
