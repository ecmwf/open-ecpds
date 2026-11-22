package ecmwf.common.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public final class S3SigningSecret {
    private static final String PURPOSE = "incoming-user-s3-signing";

    private S3SigningSecret() {
    }

    public static String encrypt(final String user, final String secret) throws IOException, GeneralSecurityException {
        if (!LocalPassword.isSet(secret) || secret.length() > 128) {
            throw new IllegalArgumentException("S3 secret must be between 1 and 128 characters");
        }
        return DatabaseEncryption.encrypt(PURPOSE, user, secret.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decrypt(final String user, final String stored) throws IOException, GeneralSecurityException {
        if (stored != null && stored.startsWith("dbenc:")) {
            return DatabaseEncryption.decrypt(PURPOSE, user, stored);
        }
        return DatabaseEncryption.decryptLegacyS3(user, stored);
    }
}
