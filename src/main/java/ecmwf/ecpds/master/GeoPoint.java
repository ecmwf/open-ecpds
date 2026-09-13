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
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 7.4.0
 * @since 2026-09-13
 */

import java.io.Serializable;

/**
 * A resolved GeoIP location, sent from the MasterServer (the only JVM holding the GeoIP2 database, see
 * {@link ManagementInterface#getGeoLocations(String[])}) to the Monitor plugin's "Live ECPDS Earth" globe
 * visualisation. Carries the ISO country code alongside the coordinates so the frontend can aggregate/group transfers
 * by destination country (e.g. once there are too many individual Hosts to usefully show one arc each).
 *
 * @param latitude
 *            the latitude
 * @param longitude
 *            the longitude
 * @param country
 *            the ISO country code, or {@code null} if unknown
 */
public record GeoPoint(double latitude, double longitude, String country) implements Serializable {

    private static final long serialVersionUID = 1L;
}
