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

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.text.Format;

/**
 * The Class DataBaseException.
 */
public class DataBaseException extends Exception {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6452614344200650438L;

    /** The _original message. */
    private final String _originalMessage;

    /**
     * Instantiates a new data base exception.
     *
     * @param me
     *            the me
     * @param exception
     *            the exception
     */
    public DataBaseException(final Object me, final Exception exception) {
        super(Format.trimString(exception.getMessage(), null));
        _originalMessage = exception.getMessage();
    }

    /**
     * Instantiates a new data base exception.
     *
     * @param message
     *            the message
     */
    public DataBaseException(final String message) {
        super(Format.trimString(message, "Internal error"));
        _originalMessage = message;
    }

    /**
     * Gets the original message.
     *
     * @return the original message
     */
    public String getOriginalMessage() {
        return _originalMessage;
    }
}
