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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import ecmwf.common.ecaccess.AbstractTicket;
import ecmwf.common.text.Format;

/**
 * The Class ECaccessTicket.
 */
public final class ECaccessTicket extends AbstractTicket {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -3456367494389896122L;

    /** The Constant INPUT. */
    public static final int INPUT = 0;

    /** The Constant OUTPUT. */
    public static final int OUTPUT = 1;

    /** The _proxy. */
    private final transient ECpdsProxy _proxy;

    /** The _mode. */
    private final int _mode;

    /**
     * Instantiates a new ecaccess ticket.
     *
     * @param mode
     *            the mode
     */
    public ECaccessTicket(final int mode) {
        _proxy = null;
        _mode = mode;
    }

    /**
     * Instantiates a new ecaccess ticket.
     *
     * @param proxy
     *            the proxy
     * @param mode
     *            the mode
     */
    public ECaccessTicket(final ECpdsProxy proxy, final int mode) {
        _proxy = proxy;
        _mode = mode;
    }

    /**
     * Gets the ecpds proxy.
     *
     * @return the ecpds proxy
     */
    public ECpdsProxy getECpdsProxy() {
        return _proxy;
    }

    /**
     * Gets the mode.
     *
     * @return the mode
     */
    public int getMode() {
        return _mode;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    @Override
    public String getStatus() {
        return "[" + hasError() + "][" + _mode + "][" + Format.getClassName(_proxy.getClass()) + "]["
                + Format.formatDuration(System.currentTimeMillis() - getTime()) + "]";
    }
}
