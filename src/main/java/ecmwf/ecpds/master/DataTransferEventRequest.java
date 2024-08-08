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

package ecmwf.ecpds.master;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.Serializable;
import java.util.Date;

/**
 * The Class DataTransferEventRequest.
 */
public final class DataTransferEventRequest implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 500275739755464953L;

    /** The _from. */
    public final Date _from;

    /** The _to. */
    public final Date _to;

    /** The _destination name. */
    public final String _destinationName;

    /** The _meta stream. */
    public final String _metaStream;

    /** The _meta time. */
    public final String _metaTime;

    /**
     * Instantiates a new data transfer event request.
     *
     * @param from
     *            the from
     * @param to
     *            the to
     * @param destinationName
     *            the destination name
     * @param metaStream
     *            the meta stream
     * @param metaTime
     *            the meta time
     */
    public DataTransferEventRequest(final Date from, final Date to, final String destinationName,
            final String metaStream, final String metaTime) {
        _from = from;
        _to = to;
        _destinationName = destinationName;
        _metaStream = metaStream;
        _metaTime = metaTime;
    }

    /**
     * Gets the from.
     *
     * @return the from
     */
    public Date getFrom() {
        return _from;
    }

    /**
     * Gets the converts into.
     *
     * @return the converts into
     */
    public Date getTo() {
        return _to;
    }

    /**
     * Gets the destination name.
     *
     * @return the destination name
     */
    public String getDestinationName() {
        return _destinationName;
    }

    /**
     * Gets the meta stream.
     *
     * @return the meta stream
     */
    public String getMetaStream() {
        return _metaStream;
    }

    /**
     * Gets the meta time.
     *
     * @return the meta time
     */
    public String getMetaTime() {
        return _metaTime;
    }
}
