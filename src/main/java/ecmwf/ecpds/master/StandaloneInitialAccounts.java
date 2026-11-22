package ecmwf.ecpds.master;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;

import ecmwf.common.security.LocalPassword;
import ecmwf.common.security.S3SigningSecret;

/**
 * Creates one-time standalone account credentials and a hash-only bootstrap script.
 */
public final class StandaloneInitialAccounts {
    private StandaloneInitialAccounts() {
    }

    public static void main(final String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Expected private credential and SQL file paths");
        }
        final var passwords = new LinkedHashMap<String, String>();
        passwords.put("admin", LocalPassword.generate());
        passwords.put("monitor", LocalPassword.generate());
        passwords.put("operator", LocalPassword.generate());
        passwords.put("test", LocalPassword.generate());
        passwords.put("hawtio", LocalPassword.generate());
        passwords.put("jolokia", LocalPassword.generate());
        final var s3Secret = LocalPassword.generate();
        final var encryptedS3 = S3SigningSecret.encrypt("test", s3Secret);
        final var credentials = new StringBuilder();
        passwords.forEach((name, password) -> credentials.append(name).append('=').append(password).append('\n'));
        credentials.append("test_s3=").append(s3Secret).append('\n');

        final var sql = new StringBuilder("START TRANSACTION;\n");
        for (final var name : new String[] { "admin", "monitor", "operator" }) {
            sql.append("UPDATE WEB_USER SET WEU_PASSWORD=NULL, WEU_PASSWORD_HASH='")
                    .append(LocalPassword.hash(passwords.get(name))).append("' WHERE WEU_ID='").append(name)
                    .append("';\n");
        }
        sql.append("UPDATE INCOMING_USER SET INU_PASSWORD=NULL, INU_PASSWORD_HASH='")
                .append(LocalPassword.hash(passwords.get("test"))).append("', INU_S3_SECRET='").append(encryptedS3)
                .append("' WHERE INU_ID='test';\nCOMMIT;\n");
        Files.writeString(Path.of(args[0]), credentials, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        Files.writeString(Path.of(args[1]), sql, StandardCharsets.US_ASCII, StandardOpenOption.CREATE_NEW);
    }
}
