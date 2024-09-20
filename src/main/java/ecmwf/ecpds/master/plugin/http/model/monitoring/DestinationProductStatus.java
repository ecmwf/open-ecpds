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

package ecmwf.ecpds.master.plugin.http.model.monitoring;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Date;

/**
 * The Interface DestinationProductStatus.
 */
public interface DestinationProductStatus extends MonitoringStatus, GenerationMonitoringStatus {

    /**
     * Gets the destination name.
     *
     * @return the destination name
     */
    String getDestinationName();

    /**
     * Gets the product.
     *
     * @return the product
     */
    String getProduct();

    /**
     * Gets the time.
     *
     * @return the time
     */
    String getTime();

    /**
     * Gets the when received last done.
     *
     * @return the when received last done
     */
    Date getWhenReceivedLastDone();

    /**
     * Sets the when received last done.
     *
     * @param d
     *            the new when received last done
     */
    void setWhenReceivedLastDone(Date d);

    /**
     * Sets the is expected.
     */
    void setIsExpected();

    /**
     * Reset.
     */
    @Override
    void reset();
}
