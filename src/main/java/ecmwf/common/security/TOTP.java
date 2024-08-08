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

package ecmwf.common.security;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * The Class TOTP to allow authenticating against the TOTP (Time-based One-time
 * Password) server. Both user+password and user+passcode credentials are
 * allowed.
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import ecmwf.common.technical.Cnf;

/**
 * The Class TOTP.
 */
public class TOTP {

    /** The Constant ACTIVE. */
    public static final boolean ACTIVE = Cnf.at("TOTP", "active", false);

    /** The Constant URL. */
    public static final String URL = Cnf.at("TOTP", "url",
            "https://localhost/auth/realms/openpds/protocol/openid-connect/token");

    /** The Constant CLIENT_ID_FOR_PASSWORD. */
    public static final String CLIENT_ID_FOR_PASSWORD = Cnf.at("TOTP", "clientIdForPassword");

    /** The Constant CLIENT_SECRET_FOR_PASSWORD. */
    public static final String CLIENT_SECRET_FOR_PASSWORD = Cnf.at("TOTP", "clientSecretForPassword");

    /** The Constant CLIENT_ID_FOR_PASSCODE. */
    public static final String CLIENT_ID_FOR_PASSCODE = Cnf.at("TOTP", "clientIdForPasscode");

    /** The Constant CLIENT_SECRET_FOR_PASSCODE. */
    public static final String CLIENT_SECRET_FOR_PASSCODE = Cnf.at("TOTP", "clientSecretForPasscode");

    /** The Constant GRANT_TYPE. */
    public static final String GRANT_TYPE = Cnf.at("TOTP", "grantType", "password");

    /** The Constant EXPECTED_STATUS. */
    public static final int EXPECTED_STATUS = Cnf.at("TOTP", "expectedStatus", 200);

    /**
     * Instantiates a new totp.
     */
    private TOTP() {
        // Hiding implicit constructor!
    }

    /**
     * Authenticate.
     *
     * @param user
     *            the user
     * @param credentials
     *            the credentials
     * @param isPasscode
     *            the is passcode
     *
     * @return true, if successful
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws URISyntaxException
     *             the URI syntax exception
     */
    public static boolean authenticate(final String user, final String credentials, final boolean isPasscode)
            throws IOException, URISyntaxException {
        if (!ACTIVE) {
            throw new IOException("TOTP authentication not active");
        }
        final var con = (HttpURLConnection) new URI(URL).toURL().openConnection();
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("client_id", isPasscode ? CLIENT_ID_FOR_PASSCODE : CLIENT_ID_FOR_PASSWORD);
        parameters.put("grant_type", GRANT_TYPE);
        parameters.put("client_secret", isPasscode ? CLIENT_SECRET_FOR_PASSCODE : CLIENT_SECRET_FOR_PASSWORD);
        parameters.put("username", user);
        parameters.put(isPasscode ? "totp" : "password", credentials);
        con.setRequestMethod("GET");
        con.setDoOutput(true);
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        final var out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(getParamsString(parameters));
        out.close();
        return con.getResponseCode() == EXPECTED_STATUS;
    }

    /**
     * Gets the params string.
     *
     * @param params
     *            the params
     *
     * @return the params string
     *
     * @throws UnsupportedEncodingException
     *             the unsupported encoding exception
     */
    private static String getParamsString(final Map<String, String> params) throws UnsupportedEncodingException {
        final var result = new StringBuilder();
        for (final Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
        }
        final var length = result.length();
        return length > 0 ? result.substring(0, length - 1) : "";
    }
}
