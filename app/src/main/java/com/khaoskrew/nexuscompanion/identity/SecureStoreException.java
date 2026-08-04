package com.khaoskrew.nexuscompanion.identity;

public final class SecureStoreException extends Exception {
    private final String safeCode;

    public SecureStoreException(String safeCode, Throwable cause) {
        super(RedactedDiagnostics.safeIdentifier(safeCode, "secure_store_error"), cause);
        this.safeCode = RedactedDiagnostics.safeIdentifier(safeCode, "secure_store_error");
    }

    public String safeCode() {
        return safeCode;
    }

    @Override
    public String toString() {
        return "SecureStoreException{safeCode=" + safeCode + "}";
    }
}
