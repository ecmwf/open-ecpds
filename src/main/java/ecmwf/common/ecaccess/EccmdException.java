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

import ecmwf.common.text.Format;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public class EccmdException extends Exception {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1909068326708015315L;

    /** The _original message. */
    private final String _originalMessage;

    /**
     * Instantiates a new eccmd exception.
     *
     * @param me
     *            the me
     * @param throwable
     *            the throwable
     */
    public EccmdException(final Object me, final Throwable throwable) {
        super(Format.trimString(throwable.getMessage(), null));
        _originalMessage = throwable.getMessage();
    }

    /**
     * Instantiates a new eccmd exception.
     *
     * @param message
     *            the message
     */
    public EccmdException(final String message) {
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
