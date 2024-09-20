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

import ecmwf.web.model.ModelBean;

/**
 * The Interface GenerationMonitoringStatus.
 */
public interface GenerationMonitoringStatus extends ModelBean {

    /** The status none. */
    int STATUS_NONE = -1; // White

    /** The status more than 30 ahead. */
    int STATUS_MORE_THAN_30_AHEAD = 1; // Green

    /** The status 30 15 ahead. */
    int STATUS_30_15_AHEAD = 2; // Yellow

    /** The status 15 0 ahead. */
    int STATUS_15_0_AHEAD = 3; // Orange

    /** The status 0 ahead. */
    int STATUS_0_AHEAD = 4; // Purple

    /** The status not received. */
    int STATUS_NOT_RECEIVED = 5; // Red

    // NOTE: To make sure the colors are those in the comments
    // check /assets/images/ecpds/g*.gif, the * being the status number.

    /** The status ok. */
    int STATUS_OK = STATUS_MORE_THAN_30_AHEAD;

    /**
     * Refresh status.
     */
    void refreshStatus();

    /**
     * Gets the generation status code.
     *
     * @return the generation status code
     */
    String getGenerationStatusCode();

    /**
     * Gets the generation status formatted code.
     *
     * @return the generation status formatted code
     */
    String getGenerationStatusFormattedCode();

    /**
     * Sets the generation status code.
     *
     * @param code
     *            the new generation status code
     */
    void setGenerationStatusCode(String code);

    /**
     * Gets the generation status.
     *
     * @return the generation status
     */
    int getGenerationStatus();

    /**
     * Sets the generation status.
     *
     * @param i
     *            the new generation status
     */
    void setGenerationStatus(int i);

    /**
     * Sets the generation status from code.
     *
     * @param code
     *            the new generation status from code
     */
    void setGenerationStatusFromCode(String code);

    /**
     * Gets the minutes before schedule.
     *
     * @return the minutes before schedule
     */
    long getMinutesBeforeSchedule();

    /**
     * Gets the arrival time.
     *
     * @return the arrival time
     */
    Date getArrivalTime();

    /**
     * Gets the scheduled time.
     *
     * @return the scheduled time
     */
    Date getScheduledTime();

    /**
     * Sets the scheduled time.
     *
     * @param d
     *            the new scheduled time
     */
    void setScheduledTime(Date d);

    /**
     * Gets the last update.
     *
     * @return the last update
     */
    Date getLastUpdate();

    /**
     * Sets the last update.
     *
     * @param d
     *            the new last update
     */
    void setLastUpdate(Date d);

    /**
     * Gets the product time.
     *
     * @return the product time
     */
    Date getProductTime();

    /**
     * Sets the product time.
     *
     * @param d
     *            the new product time
     */
    void setProductTime(Date d);

    /**
     * We are changing to another Time period, reset monitoring values.
     *
     * @throws MonitoringException
     *             the monitoring exception
     */
    void reset() throws MonitoringException;
}
