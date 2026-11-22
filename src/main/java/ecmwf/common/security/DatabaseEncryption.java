package ecmwf.common.security;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ecmwf.common.technical.Cnf;

/**
 * Encrypts database values using a stable field-specific purpose and record identity as authenticated context.
 */
public final class DatabaseEncryption {
    private static final String PREFIX = "dbenc:v1:";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int NONCE_BYTES = 12;
    private static final int TAG_BYTES = 16;
    private static volatile SecretKeySpec encryptionKey;

    private DatabaseEncryption() {
    }

    private static SecretKeySpec encryptionKey() throws IOException, GeneralSecurityException {
        var result = encryptionKey;
        if (result == null) {
            synchronized (DatabaseEncryption.class) {
                result = encryptionKey;
                if (result == null) {
                    var file = Cnf.at("DatabaseEncryption", "keyStore", "");
                    final String password;
                    final String alias;
                    if (file.isBlank()) {
                        file = Cnf.at("LocalPasswords", "s3EncryptionKeyStore", "");
                        password = Cnf.at("LocalPasswords", "s3EncryptionKeyStorePassword", "");
                        alias = Cnf.at("LocalPasswords", "s3EncryptionKeyAlias", "s3-signing");
                    } else {
                        password = Cnf.at("DatabaseEncryption", "keyStorePassword", "");
                        alias = Cnf.at("DatabaseEncryption", "keyAlias", "database-encryption");
                    }
                    if (file.isBlank() || password.isEmpty() || alias.isBlank()) {
                        throw new IOException("Configure the database encryption keystore, password and alias");
                    }
                    final var store = KeyStore.getInstance("PKCS12");
                    final var chars = password.toCharArray();
                    try {
                        try (final var input = Files.newInputStream(Path.of(file))) {
                            store.load(input, chars);
                        }
                        final var key = store.getKey(alias, chars);
                        final var encoded = key == null ? null : key.getEncoded();
                        try {
                            if (!(key instanceof SecretKey) || !"AES".equalsIgnoreCase(key.getAlgorithm())
                                    || encoded == null || encoded.length != 32) {
                                throw new GeneralSecurityException(
                                        "Database encryption key must be an AES-256 secret key");
                            }
                            encryptionKey = result = new SecretKeySpec(encoded, "AES");
                        } finally {
                            if (encoded != null) {
                                Arrays.fill(encoded, (byte) 0);
                            }
                        }
                    } finally {
                        Arrays.fill(chars, '\0');
                    }
                }
            }
        }
        return result;
    }

    private static byte[] context(final String purpose, final String record) {
        if (purpose == null || purpose.isEmpty() || record == null || record.isEmpty()) {
            throw new IllegalArgumentException("Encryption purpose and record must not be empty");
        }
        final var version = PREFIX.getBytes(StandardCharsets.UTF_8);
        final var purposeBytes = purpose.getBytes(StandardCharsets.UTF_8);
        final var recordBytes = record.getBytes(StandardCharsets.UTF_8);
        return ByteBuffer.allocate(version.length + Integer.BYTES + purposeBytes.length + recordBytes.length)
                .put(version).putInt(purposeBytes.length).put(purposeBytes).put(recordBytes).array();
    }

    private static Cipher cipher(final int mode, final byte[] nonce, final byte[] context)
            throws IOException, GeneralSecurityException {
        final var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(mode, encryptionKey(), new GCMParameterSpec(128, nonce));
        cipher.updateAAD(context);
        return cipher;
    }

    public static String encrypt(final String purpose, final String record, final byte[] value)
            throws IOException, GeneralSecurityException {
        final var nonce = new byte[NONCE_BYTES];
        RANDOM.nextBytes(nonce);
        final var encrypted = cipher(Cipher.ENCRYPT_MODE, nonce, context(purpose, record)).doFinal(value);
        final var payload = Arrays.copyOf(nonce, nonce.length + encrypted.length);
        System.arraycopy(encrypted, 0, payload, nonce.length, encrypted.length);
        return PREFIX + Base64.getEncoder().encodeToString(payload);
    }

    public static byte[] decrypt(final String purpose, final String record, final String stored)
            throws IOException, GeneralSecurityException {
        if (stored == null || !stored.startsWith(PREFIX)) {
            throw new GeneralSecurityException("Unsupported database ciphertext version");
        }
        return decryptPayload(stored.substring(PREFIX.length()), context(purpose, record));
    }

    static byte[] decryptLegacyS3(final String record, final String stored)
            throws IOException, GeneralSecurityException {
        if (record == null || record.isEmpty()) {
            throw new IllegalArgumentException("Encryption record must not be empty");
        }
        return decryptPayload(stored, record.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] decryptPayload(final String stored, final byte[] context)
            throws IOException, GeneralSecurityException {
        if (stored == null) {
            throw new GeneralSecurityException("Invalid encrypted database value");
        }
        final byte[] payload;
        try {
            payload = Base64.getDecoder().decode(stored);
        } catch (final IllegalArgumentException e) {
            throw new GeneralSecurityException("Invalid encrypted database value", e);
        }
        if (payload.length < NONCE_BYTES + TAG_BYTES) {
            throw new GeneralSecurityException("Invalid encrypted database value");
        }
        return cipher(Cipher.DECRYPT_MODE, Arrays.copyOf(payload, NONCE_BYTES), context).doFinal(payload, NONCE_BYTES,
                payload.length - NONCE_BYTES);
    }
}
