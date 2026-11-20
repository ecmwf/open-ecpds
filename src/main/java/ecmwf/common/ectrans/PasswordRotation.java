/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ecmwf.common.ectrans;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import ecmwf.common.database.ECtransModule;
import ecmwf.common.technical.Cnf;

/** Opt-in password probing and delivery; implementations must never put credentials in connection URLs or logs. */
public interface PasswordRotation extends CredentialProbe<PasswordRotation.Endpoint, String> {
    interface Endpoint extends CredentialProbe.Endpoint {
        String host();

        int port();

        String login();
    }

    /** Checks the exact method and Host options without connecting or performing external I/O. */
    boolean supports(String method, String hostData, int port);

    /** A configuration binding safe to include in relay metadata; implementations must omit embedded credentials. */
    default String snapshotHostData(final String hostData) {
        return hostData;
    }

    /** Connects this module to the approved endpoint without using its legacy credentials or a reusable cookie. */
    void connect(TransferModule module, Endpoint endpoint, String directory, ECtransSetup setup, String password)
            throws IOException;

    record Approved(String moduleClass, String archive, PasswordRotation capability, Endpoint endpoint)
            implements Endpoint {
        @Override
        public String host() {
            return endpoint.host();
        }

        @Override
        public int port() {
            return endpoint.port();
        }

        @Override
        public String login() {
            return endpoint.login();
        }

        @Override
        public String fingerprint() {
            try {
                final var digest = MessageDigest.getInstance("SHA-256");
                for (final var value : new String[] { moduleClass, archive, endpoint.fingerprint() }) {
                    final var bytes = value.getBytes(StandardCharsets.UTF_8);
                    digest.update(Integer.toString(bytes.length).getBytes(StandardCharsets.US_ASCII));
                    digest.update((byte) ':');
                    digest.update(bytes);
                }
                return HexFormat.of().formatHex(digest.digest());
            } catch (final NoSuchAlgorithmException failure) {
                throw new IllegalStateException(failure);
            }
        }

        public boolean matches(final String implementation, final String moduleArchive) {
            return moduleClass.equals(implementation) && archive.equals(archiveValue(moduleArchive));
        }

        public Result probe(final String password) throws IOException {
            return capability.probe(endpoint, password);
        }
    }

    static Approved approved(final long host) {
        return parse(Cnf.at("PasswordProbeHosts", Long.toString(host), ""),
                Cnf.at("PasswordProbeArchives", Long.toString(host), ""));
    }

    /** The first field identifies the installed module; the remaining configuration belongs entirely to that module. */
    static Approved parse(final String configuration, final String archive) {
        final var fields = configuration.strip().split("\\s+", 2);
        if (fields.length != 2) {
            throw new IllegalArgumentException("Invalid password rotation endpoint configuration");
        }
        final var definition = new ECtransModule("password-rotation");
        definition.setClasse(fields[0]);
        definition.setArchive(archiveValue(archive));
        try {
            final var module = RemoteProvider.createTransferModule(definition);
            final var capability = module.passwordRotation().orElseThrow();
            return new Approved(definition.getClasse(), definition.getArchive(), capability,
                    capability.parseEndpoint(fields[1]));
        } catch (final Exception failure) {
            throw new IllegalArgumentException("Password rotation module is unavailable or unsupported");
        }
    }

    private static String archiveValue(final String archive) {
        final var value = archive == null ? "" : Cnf.getValue(archive);
        return value == null ? "" : value;
    }
}
