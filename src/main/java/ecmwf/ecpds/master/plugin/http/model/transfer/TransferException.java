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

package ecmwf.ecpds.master.plugin.http.model.transfer;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.common.text.Format;
import ecmwf.web.ECMWFException;

/**
 * The Class TransferException.
 */
public class TransferException extends ECMWFException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6097659891597162033L;

    /**
     * Instantiates a new transfer exception.
     *
     * @param message
     *            the message
     */
    public TransferException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new transfer exception.
     *
     * @param message
     *            the message
     * @param t
     *            the t
     */
    public TransferException(final String message, final Throwable t) {
        super(Format.getLastMessage(t, message));
    }
}
