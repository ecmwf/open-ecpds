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

package ecmwf.common.ecaccess;

import java.io.Serializable;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public final class ECauthToken implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1948857166613608857L;

    /** The Constant COMPLETED. */
    public static final int COMPLETED = 0;

    /** The Constant PASSCODE_REQUIRED. */
    public static final int PASSCODE_REQUIRED = 1;

    /** The Constant PIN_REQUIRED. */
    public static final int PIN_REQUIRED = 2;

    /** The _token. */
    private final byte[] _token;

    /** The _status. */
    private final int _status;

    /** The _time. */
    private final long _time;

    /**
     * Instantiates a new ecauth token.
     *
     * @param token
     *            the token
     * @param status
     *            the status
     */
    public ECauthToken(final byte[] token, final int status) {
        _token = token;
        _status = status;
        _time = System.currentTimeMillis();
    }

    /**
     * Instantiates a new ecauth token.
     *
     * @param token
     *            the token
     * @param status
     *            the status
     */
    public ECauthToken(final String token, final int status) {
        this(token.getBytes(), status);
    }

    /**
     * Checks if is complete.
     *
     * @return true, if is complete
     */
    public boolean isComplete() {
        return _status == COMPLETED;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public int getStatus() {
        return _status;
    }

    /**
     * Gets the token.
     *
     * @return the token
     */
    public byte[] getToken() {
        return _token;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public long getTime() {
        return _time;
    }

    /**
     * Account is disabled.
     *
     * @return true, if successful
     */
    public boolean accountIsDisabled() {
        final var token = new String(_token);
        return token.indexOf("passwd/expired") != -1 || token.indexOf("passwd/disabled") != -1;
    }
}
