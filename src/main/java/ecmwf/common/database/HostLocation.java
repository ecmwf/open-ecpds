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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.util.Objects;

/**
 * The Class HostLocation.
 */
public class HostLocation extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6269434296900284206L;

    /** The hlo id. */
    protected Integer HLO_ID;

    /** The hos ip. */
    protected String HLO_IP;

    /** The hos latitude. */
    protected Double HLO_LATITUDE;

    /** The hos longitude. */
    protected Double HLO_LONGITUDE;

    /**
     * Instantiates a new host.
     */
    public HostLocation() {
    }

    /**
     * Instantiates a new host.
     *
     * @param id
     *            the id
     */
    public HostLocation(final int id) {
        setId(id);
    }

    /**
     * Gets the ip.
     *
     * @return the ip
     */
    public String getIp() {
        return HLO_IP;
    }

    /**
     * Sets the ip.
     *
     * @param param
     *            the new ip
     */
    public void setIp(final String param) {
        HLO_IP = param;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return HLO_ID;
    }

    /**
     * Sets the id.
     *
     * @param param
     *            the new id
     */
    public void setId(final int param) {
        HLO_ID = param;
    }

    /**
     * Gets the latitude.
     *
     * @return the latitude
     */
    public Double getLatitude() {
        return HLO_LATITUDE;
    }

    /**
     * Sets the latitude.
     *
     * @param latitude
     *            the new latitude
     */
    public void setLatitude(final Double latitude) {
        HLO_LATITUDE = latitude;
    }

    /**
     * Gets the longitude.
     *
     * @return the longitude
     */
    public Double getLongitude() {
        return HLO_LONGITUDE;
    }

    /**
     * Sets the longitude.
     *
     * @param longitude
     *            the new longitude
     */
    public void setLongitude(final Double longitude) {
        HLO_LONGITUDE = longitude;
    }

    /**
     * {@inheritDoc}
     *
     * Hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(HLO_ID);
    }

    /**
     * {@inheritDoc}
     *
     * Equals.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final var other = (HostLocation) obj;
        return HLO_ID == other.HLO_ID;
    }
}
