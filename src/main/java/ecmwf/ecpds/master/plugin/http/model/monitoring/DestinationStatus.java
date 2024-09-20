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

import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;

/**
 * The Interface DestinationStatus.
 */
public interface DestinationStatus extends MonitoringStatus {

    /**
     * Gets the queue size.
     *
     * @return the queue size
     */
    int getQueueSize();

    /**
     * Sets the queue size.
     *
     * @param i
     *            the new queue size
     */
    void setQueueSize(int i);

    /**
     * Gets the status.
     *
     * @return the status
     */
    String getStatus();

    /**
     * Sets the status.
     *
     * @param s
     *            the new status
     */
    void setStatus(String s);

    /**
     * Gets the big sister status.
     *
     * @return the big sister status
     */
    int getBigSisterStatus();

    /**
     * Sets the big sister status.
     *
     * @param i
     *            the new big sister status
     */
    void setBigSisterStatus(int i);

    /**
     * Gets the big sister status comment.
     *
     * @return the big sister status comment
     */
    String getBigSisterStatusComment();

    /**
     * Sets the big sister status comment.
     *
     * @param s
     *            the new big sister status comment
     */
    void setBigSisterStatusComment(String s);

    /**
     * Gets the last transfer.
     *
     * @return the last transfer
     */
    DataTransfer getLastTransfer();

    /**
     * Sets the last transfer.
     *
     * @param t
     *            the new last transfer
     */
    void setLastTransfer(DataTransfer t);

    /**
     * Gets the bad data transfers size.
     *
     * @return the bad data transfers size
     */
    int getBadDataTransfersSize();

    /**
     * Sets the bad data transfers size.
     *
     * @param i
     *            the new bad data transfers size
     */
    void setBadDataTransfersSize(int i);

    /**
     * Gets the using internet.
     *
     * @return If the main host is using internet or something else.
     */
    boolean getUsingInternet();

    /**
     * Gets the primary host.
     *
     * @return the primary host
     */
    Host getPrimaryHost();

    /**
     * Sets the primary host.
     *
     * @param h
     *            the new primary host
     */
    void setPrimaryHost(Host h);

    /**
     * Gets the currently used host name.
     *
     * @return the currently used host name
     */
    String getCurrentlyUsedHostName();

    /**
     * Sets the currently used host name.
     *
     * @param h
     *            the new currently used host name
     */
    void setCurrentlyUsedHostName(String h);

    /**
     * Checks if is using primary host.
     *
     * @return true, if is using primary host
     */
    boolean isUsingPrimaryHost();

    /**
     * When was updated this information.
     *
     * @return the calculation date
     */
    Date getCalculationDate();

    /**
     * Sets the calculation date.
     *
     * @param d
     *            the new calculation date
     */
    void setCalculationDate(Date d);

    /**
     * The day we are monitoring.
     *
     * @return the monitoring date
     */
    Date getMonitoringDate();

    /**
     * Sets the monitoring date.
     *
     * @param d
     *            the new monitoring date
     */
    void setMonitoringDate(Date d);
}
