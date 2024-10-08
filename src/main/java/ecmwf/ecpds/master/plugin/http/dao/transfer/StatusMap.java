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

package ecmwf.ecpds.master.plugin.http.dao.transfer;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.ecpds.master.plugin.http.home.monitoring.DestinationProductStatusHome;
import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationProductStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException;

/**
 * The Class StatusMap.
 */
public class StatusMap extends HashMap<String, DestinationProductStatus> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7184130263719344208L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(StatusMap.class);

    /** The destination name. */
    private final String destinationName;

    /**
     * Instantiates a new status map.
     *
     * @param destinationName
     *            the destination name
     */
    public StatusMap(final String destinationName) {
        super(0);
        this.destinationName = destinationName;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the.
     */
    @Override
    public DestinationProductStatus get(final Object o) {
        return get(o.toString());
    }

    /**
     * Gets the.
     *
     * @param tag
     *            the tag
     *
     * @return the destination product status
     */
    public DestinationProductStatus get(final String tag) {
        try {
            final var t = tag.toString();
            final var pos = t.indexOf("@");
            return DestinationProductStatusHome.find(destinationName, t.substring(0, pos), t.substring(pos + 1));
        } catch (final MonitoringException e) {
            log.error("Error getting Status", e);
            return null;
        }
    }
}
