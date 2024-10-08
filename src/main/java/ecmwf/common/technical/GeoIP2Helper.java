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

package ecmwf.common.technical;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

/**
 * The Class GeoIP2Helper.
 */
public final class GeoIP2Helper {

    /** The DataBaseReader. */
    private static DatabaseReader _reader;

    /**
     * Gets the city response.
     *
     * @param hostName
     *            the host name
     *
     * @return the city response
     *
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     * @throws com.maxmind.geoip2.exception.GeoIp2Exception
     *             Signals that a GeoIP2 exception has occurred.
     */
    public static CityResponse getCityResponse(final String hostName) throws IOException, GeoIp2Exception {
        return _getDataBaseReader().city(InetAddress.getByName(hostName));
    }

    /**
     * Gets the DataBaseReader and create it if not yet done.
     *
     * @return the database reader
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private synchronized static DatabaseReader _getDataBaseReader() throws IOException {
        if (_reader == null) {
            _reader = new DatabaseReader.Builder(new File(Cnf.at("Server", "geoip2DataFile"))).withCache(new CHMCache())
                    .build();
        }
        return _reader;
    }
}
