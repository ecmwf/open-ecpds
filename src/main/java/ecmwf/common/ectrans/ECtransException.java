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

package ecmwf.common.ectrans;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import static ecmwf.common.text.Util.isNotEmpty;

/**
 * The Class ECtransException.
 */
public final class ECtransException extends Exception {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8656567659658845870L;

    /**
     * Instantiates a new ectrans exception.
     *
     * @param message
     *            the message
     */
    public ECtransException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new ectrans exception.
     *
     * @param message
     *            the message
     * @param throwable
     *            the throwable
     */
    public ECtransException(final String message, final Throwable throwable) {
        super(isNotEmpty(message) ? message + _getMessage(true, throwable) : _getMessage(false, throwable));
        initCause(throwable);
    }

    /**
     * Gets the message.
     *
     * @param useParentheses
     *            the use parentheses
     * @param throwable
     *            the throwable
     *
     * @return the string
     */
    @SuppressWarnings("null")
	private static String _getMessage(final boolean useParentheses, final Throwable throwable) {
        final var message = throwable != null ? throwable.getMessage() : null;
        return isNotEmpty(message) ? useParentheses ? " (" + message.trim() + ")" : message.trim() : "";
    }
}
