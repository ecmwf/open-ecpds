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
 *
 * In applying the License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.common.database;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

public class ApiClient extends DataBaseObject {
    private static final long serialVersionUID = 1L;

    protected String APU_ID;
    protected String APU_SECRET_HASH;
    protected String APU_COMMENT;
    protected boolean APU_ACTIVE = true;
    protected BigDecimal APU_CREATED;
    protected BigDecimal APU_LAST_USED;
    protected String APU_LAST_USED_HOST;
    protected String COU_ISO;

    public ApiClient() {
    }

    public ApiClient(final String id) {
        setId(id);
    }

    public String getId() {
        return APU_ID;
    }

    public void setId(final String v) {
        APU_ID = v;
    }

    public String getSecretHash() {
        return APU_SECRET_HASH;
    }

    public void setSecretHash(final String v) {
        APU_SECRET_HASH = v;
    }

    public String getComment() {
        return APU_COMMENT;
    }

    public void setComment(final String v) {
        APU_COMMENT = v;
    }

    public boolean getActive() {
        return APU_ACTIVE;
    }

    public void setActive(final boolean v) {
        APU_ACTIVE = v;
    }

    public void setActive(final String v) {
        APU_ACTIVE = Boolean.parseBoolean(v);
    }

    public java.sql.Timestamp getCreated() {
        return bigDecimalToTimestamp(APU_CREATED);
    }

    public void setCreated(final java.sql.Timestamp v) {
        APU_CREATED = timestampToBigDecimal(v);
    }

    public java.sql.Timestamp getLastUsed() {
        return bigDecimalToTimestamp(APU_LAST_USED);
    }

    public void setLastUsed(final java.sql.Timestamp v) {
        APU_LAST_USED = timestampToBigDecimal(v);
    }

    public String getLastUsedHost() {
        return APU_LAST_USED_HOST;
    }

    public void setLastUsedHost(final String v) {
        APU_LAST_USED_HOST = v;
    }

    public String getCountryIso() {
        return COU_ISO;
    }

    public void setCountryIso(final String v) {
        COU_ISO = v != null && v.isBlank() ? null : v;
    }

    /**
     * Generates a random hex secret string (48 hex chars).
     */
    public static String generateSecret() {
        final var random = new SecureRandom();
        final byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        final var sb = new StringBuilder();
        for (final byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Returns the SHA-256 hex digest of the given input string.
     */
    public static String sha256Hex(final String input) {
        try {
            final var md = MessageDigest.getInstance("SHA-256");
            final byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            final var sb = new StringBuilder();
            for (final byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(APU_ID);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(APU_ID, ((ApiClient) obj).APU_ID);
    }
}
