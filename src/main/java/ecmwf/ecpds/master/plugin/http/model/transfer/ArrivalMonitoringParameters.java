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

package ecmwf.ecpds.master.plugin.http.model.transfer;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Different parameters regarding the arrival to the system of a file, which are
 * relevant for monitoring issues.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.awt.Color;
import java.util.Date;

import ecmwf.web.model.ModelBean;

/**
 * The Interface ArrivalMonitoringParameters.
 */
public interface ArrivalMonitoringParameters extends ModelBean {

    /** The arrival status none. */
    int ARRIVAL_STATUS_NONE = 0;

    /** The arrival status before target1. */
    int ARRIVAL_STATUS_BEFORE_TARGET1 = 1;

    /** The arrival status before predicted. */
    int ARRIVAL_STATUS_BEFORE_PREDICTED = 2;

    /** The arrival status before target2. */
    int ARRIVAL_STATUS_BEFORE_TARGET2 = 3;

    /** The arrival status not before target2. */
    int ARRIVAL_STATUS_NOT_BEFORE_TARGET2 = 4;

    /** The arrival status not before target3. */
    int ARRIVAL_STATUS_NOT_BEFORE_TARGET3 = 5;

    /** The arrival status not before target4. */
    int ARRIVAL_STATUS_NOT_BEFORE_TARGET4 = 6;

    /** The colors. */
    Color[] COLORS = { Color.WHITE, Color.MAGENTA, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.RED };

    /** The anticipation target1. */
    int ANTICIPATION_TARGET1 = 75;

    /** The anticipation target2. */
    int ANTICIPATION_TARGET2 = 40;

    /** The anticipation target3. */
    int ANTICIPATION_TARGET3 = 25;

    /** The anticipation target4. */
    int ANTICIPATION_TARGET4 = 10;

    /**
     * The arrival status operators ok. The status to which things are reset after transmission is ok in the operators
     * display.
     */
    int ARRIVAL_STATUS_OPERATORS_OK = ARRIVAL_STATUS_BEFORE_PREDICTED;

    /**
     * TODO: The OFFSET should be configurable, not harcoded.
     *
     * @return Schedule time - OFFSET (=75m). This is a general target arbitrarily assigned my management
     *
     * @throws TransferException
     *             the transfer exception
     */
    Date getArrivalTargetTime() throws TransferException;

    /**
     * Guesswork. :-( 1 st dev using last 14 days discarding earliest and latest.
     *
     * @return Predicted value for arrival this time.
     *
     * @throws TransferException
     *             the transfer exception
     */
    Date getArrivalPredictedTime() throws TransferException;

    /**
     * Gets the arrival status.
     *
     * @return A status calculated as a function of all other four values
     *
     * @throws TransferException
     *             the transfer exception
     */
    int getArrivalStatus() throws TransferException;

    /**
     * Gets the arrival earliest time.
     *
     * @return The earliest file arrival time for a default number of days
     *
     * @throws TransferException
     *             the transfer exception
     */
    Date getArrivalEarliestTime() throws TransferException;

    /**
     * Gets the arrival latest time.
     *
     * @return The latest file arrival time for a default number of days
     *
     * @throws TransferException
     *             the transfer exception
     */
    Date getArrivalLatestTime() throws TransferException;
}
