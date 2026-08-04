package com.khaoskrew.nexuscompanion.identity;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Objects;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Stores only AES-GCM ciphertext and IV in private preferences. The non-exportable
 * encryption key is generated and retained by Android Keystore.
 */
public final class AndroidKeystoreSessionStore implements SecureSessionStore {
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "khaos_nexus_mobile_session_v1";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String PREFERENCES = "nexus.identity.secure.v1";
    private static final String FIELD_CIPHERTEXT = "ciphertext";
    private static final String FIELD_IV = "iv";
    private static final String FIELD_FORMAT = "format";
    private static final int FORMAT_VERSION = 1;
    private static final byte[] AAD =
        "com.khaoskrew.nexuscompanion.identity.session.v1".getBytes(StandardCharsets.UTF_8);

    private final SharedPreferences preferences;

    public AndroidKeystoreSessionStore(Context context) {
        Objects.requireNonNull(context, "context");
        preferences = context.getApplicationContext().getSharedPreferences(
            PREFERENCES,
            Context.MODE_PRIVATE
        );
    }

    @Override
    public synchronized Optional<SessionTokens> load() throws SecureStoreException {
        String encodedCiphertext = preferences.getString(FIELD_CIPHERTEXT, null);
        String encodedIv = preferences.getString(FIELD_IV, null);
        int format = preferences.getInt(FIELD_FORMAT, 0);
        if (encodedCiphertext == null && encodedIv == null) {
            return Optional.empty();
        }
        if (encodedCiphertext == null || encodedIv == null || format != FORMAT_VERSION) {
            clearCorruptState();
            throw new SecureStoreException("secure_store_corrupt", null);
        }

        try {
            byte[] ciphertext = Base64.decode(encodedCiphertext, Base64.NO_WRAP);
            byte[] iv = Base64.decode(encodedIv, Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
            cipher.updateAAD(AAD);
            byte[] plaintext = cipher.doFinal(ciphertext);
            try {
                return Optional.of(deserialize(new String(plaintext, StandardCharsets.UTF_8)));
            } finally {
                java.util.Arrays.fill(plaintext, (byte) 0);
            }
        } catch (Exception error) {
            clearCorruptState();
            throw new SecureStoreException("secure_store_decrypt_failed", error);
        }
    }

    @Override
    public synchronized void save(SessionTokens tokens) throws SecureStoreException {
        Objects.requireNonNull(tokens, "tokens");
        byte[] plaintext = serialize(tokens).getBytes(StandardCharsets.UTF_8);
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
            cipher.updateAAD(AAD);
            byte[] ciphertext = cipher.doFinal(plaintext);
            boolean stored = preferences.edit()
                .putString(FIELD_CIPHERTEXT, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
                .putString(FIELD_IV, Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP))
                .putInt(FIELD_FORMAT, FORMAT_VERSION)
                .commit();
            if (!stored) {
                throw new IllegalStateException("Encrypted session commit failed");
            }
        } catch (Exception error) {
            clearCorruptState();
            throw new SecureStoreException("secure_store_encrypt_failed", error);
        } finally {
            java.util.Arrays.fill(plaintext, (byte) 0);
        }
    }

    @Override
    public synchronized void clear() throws SecureStoreException {
        try {
            if (!preferences.edit().clear().commit()) {
                throw new IllegalStateException("Encrypted session clear failed");
            }
        } catch (Exception error) {
            throw new SecureStoreException("secure_store_clear_failed", error);
        }
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE);
        keyStore.load(null);
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        }

        KeyGenerator generator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE
        );
        generator.init(new KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build());
        return generator.generateKey();
    }

    private String serialize(SessionTokens tokens) {
        JSONObject object = new JSONObject();
        try {
            object.put("accessToken", tokens.accessToken());
            object.put("refreshToken", tokens.refreshToken());
            object.put("accessExpiresAt", tokens.accessExpiresAtEpochSeconds());
            object.put("refreshExpiresAt", tokens.refreshExpiresAtEpochSeconds());
            object.put("deviceId", tokens.deviceId());
            object.put("subjectId", tokens.subjectId());
            object.put("rotationCounter", tokens.rotationCounter());
            return object.toString();
        } catch (JSONException error) {
            throw new IllegalStateException("Session serialization failed", error);
        }
    }

    private SessionTokens deserialize(String json) throws JSONException {
        JSONObject object = new JSONObject(json);
        return new SessionTokens(
            object.getString("accessToken"),
            object.getString("refreshToken"),
            object.getLong("accessExpiresAt"),
            object.getLong("refreshExpiresAt"),
            object.getString("deviceId"),
            object.getString("subjectId"),
            object.getLong("rotationCounter")
        );
    }

    private void clearCorruptState() {
        preferences.edit().clear().commit();
    }
}
