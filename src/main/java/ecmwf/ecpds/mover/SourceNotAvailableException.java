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

package ecmwf.ecpds.mover;

/**
 * ECMWF Product Data Store (OpenECPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */
public final class SourceNotAvailableException extends Exception {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8308330281908501968L;

    /**
     * Instantiates a new source not available exception.
     *
     * @param message
     *            the message
     */
    SourceNotAvailableException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new source not available exception.
     *
     * @param message
     *            the message
     * @param throwable
     *            the throwable
     */
    SourceNotAvailableException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
