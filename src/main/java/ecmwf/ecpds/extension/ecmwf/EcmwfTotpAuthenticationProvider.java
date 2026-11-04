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
 * In applying this license, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization.
 */

package ecmwf.ecpds.extension.ecmwf;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import ecmwf.common.security.TOTP;
import ecmwf.ecpds.extension.AuthenticationProvider;
import ecmwf.ecpds.extension.ExtensionContext;

/**
 * ECMWF's password/passcode verification endpoint adapter.
 */
public class EcmwfTotpAuthenticationProvider implements AuthenticationProvider {
    private String url;
    private String passwordClientId;
    private String passwordClientSecret;
    private String passcodeClientId;
    private String passcodeClientSecret;
    private String grantType;
    private int expectedStatus;

    @Override
    public int apiVersion() {
        return API_VERSION;
    }

    @Override
    public void start(final ExtensionContext context) {
        url = option(context, "url", TOTP.URL);
        passwordClientId = option(context, "clientIdForPassword", TOTP.CLIENT_ID_FOR_PASSWORD);
        passwordClientSecret = option(context, "clientSecretForPassword", TOTP.CLIENT_SECRET_FOR_PASSWORD);
        passcodeClientId = option(context, "clientIdForPasscode", TOTP.CLIENT_ID_FOR_PASSCODE);
        passcodeClientSecret = option(context, "clientSecretForPasscode", TOTP.CLIENT_SECRET_FOR_PASSCODE);
        grantType = option(context, "grantType", TOTP.GRANT_TYPE);
        expectedStatus = integerOption(context, "expectedStatus", TOTP.EXPECTED_STATUS);
    }

    @Override
    public Result authenticate(final String user, final String credentials, final String origin) throws IOException {
        if (url == null || url.isBlank() || user == null || user.isBlank() || credentials == null
                || credentials.isBlank()) {
            return Result.REJECTED;
        }
        final var passcode = isPasscode(credentials);
        final var parameters = new HashMap<String, String>();
        parameters.put("client_id", passcode ? passcodeClientId : passwordClientId);
        parameters.put("grant_type", grantType);
        parameters.put("client_secret", passcode ? passcodeClientSecret : passwordClientSecret);
        parameters.put("username", user);
        parameters.put(passcode ? "totp" : "password", credentials);
        try {
            return request(new URI(url), parameters) == expectedStatus ? Result.ACCEPTED : Result.REJECTED;
        } catch (URISyntaxException e) {
            throw new IOException("Invalid ECMWF authentication endpoint");
        }
    }

    protected int request(final URI uri, final Map<String, String> parameters) throws IOException {
        final var connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        try {
            try (var out = connection.getOutputStream()) {
                out.write(getParamsString(parameters).getBytes(StandardCharsets.UTF_8));
            }
            return connection.getResponseCode();
        } finally {
            connection.disconnect();
        }
    }

    private static String getParamsString(final Map<String, String> params) {
        final var result = new StringBuilder();
        for (final var entry : params.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append('&');
            }
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)).append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return result.toString();
    }

    private static String option(final ExtensionContext context, final String name, final String fallback) {
        final var value = context.option(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int integerOption(final ExtensionContext context, final String name, final int fallback) {
        final var value = context.option(name);
        return value == null || value.isBlank() ? fallback : Integer.parseInt(value);
    }

    private static boolean isPasscode(final String value) {
        if (value.length() != 6 && value.length() != 8) {
            return false;
        }
        for (var i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
