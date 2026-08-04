package com.khaoskrew.nexuscompanion.identity;

import java.util.Optional;

/** Storage contract whose implementations must never persist plaintext tokens. */
public interface SecureSessionStore {
    Optional<SessionTokens> load() throws SecureStoreException;

    void save(SessionTokens tokens) throws SecureStoreException;

    void clear() throws SecureStoreException;
}
