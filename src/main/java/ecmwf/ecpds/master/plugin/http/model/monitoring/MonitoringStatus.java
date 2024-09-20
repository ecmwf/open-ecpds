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

import ecmwf.web.model.ModelBean;

/**
 * The Interface MonitoringStatus.
 */
public interface MonitoringStatus extends ModelBean {

    /**
     * Checks if is present.
     *
     * @return Does this combination exist
     */
    boolean isPresent();

    /**
     * Sets the present.
     *
     * @param b
     *            the new present
     */
    void setPresent(boolean b);

    /**
     * Checks if is calculated.
     *
     * @return Do we have information about IF this combination exists
     */
    boolean isCalculated();

    /**
     * Sets the calculated.
     *
     * @param b
     *            the new calculated
     */
    void setCalculated(boolean b);

    /**
     * Gets the arrival status.
     *
     * @return Arrival Status coming from ECPDS calculations
     */
    int getArrivalStatus();

    /**
     * Sets the arrival status.
     *
     * @param i
     *            the new arrival status
     */
    void setArrivalStatus(int i);

    /**
     * Gets the real time arrival status.
     *
     * @return Arrival Status coming from ECPDS calculation, but in "realtime" version, that is, once the product is
     *         received the status will be set to OK, no matter what happened before
     */
    int getRealTimeArrivalStatus();

    /**
     * Sets the real time arrival status.
     *
     * @param i
     *            the new real time arrival status
     */
    void setRealTimeArrivalStatus(int i);

    /**
     * Gets the transfer status.
     *
     * @return Transfer Status coming from ECPDS calculations
     */

    int getTransferStatus();

    /**
     * Sets the transfer status.
     *
     * @param i
     *            the new transfer status
     */
    void setTransferStatus(int i);

    /**
     * Gets the real time transfer status.
     *
     * @return Transfer Status coming from ECPDS calculation, but in "realtime" version, that is, once the product is
     *         transferred the status will be set to OK, no matter what happened before
     */

    int getRealTimeTransferStatus();

    /**
     * Sets the real time transfer status.
     *
     * @param i
     *            the new real time transfer status
     */
    void setRealTimeTransferStatus(int i);
}
