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
 * Different parameters regarding the transfer of a file, which are relevant for
 * monitoring issues.
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Date;

import ecmwf.web.model.ModelBean;

/**
 * The Interface TransferMonitoringParameters.
 */
public interface TransferMonitoringParameters extends ModelBean {

    /** The days to consider back. */
    int DAYS_TO_CONSIDER_BACK = 14;

    /** The transfer status none. */
    int TRANSFER_STATUS_NONE = 0;

    /** The transfer status before target. */
    int TRANSFER_STATUS_BEFORE_TARGET = 1;

    /** The transfer status before predicted. */
    int TRANSFER_STATUS_BEFORE_PREDICTED = 2;

    /** The transfer status before predicted plus range by 2. */
    int TRANSFER_STATUS_BEFORE_PREDICTED_PLUS_RANGE_BY_2 = 3;

    /** The transfer status not before predicted plus range by 2. */
    int TRANSFER_STATUS_NOT_BEFORE_PREDICTED_PLUS_RANGE_BY_2 = 4;

    /** The transfer status not before predicted plus range by 4. */
    int TRANSFER_STATUS_NOT_BEFORE_PREDICTED_PLUS_RANGE_BY_4 = 5;

    /** The transfer status not before predicted plus range by 6. */
    int TRANSFER_STATUS_NOT_BEFORE_PREDICTED_PLUS_RANGE_BY_6 = 6;

    /**
     * The transfer status operators ok. The status to which things are reset after transmission is ok in the operators
     * display.
     */
    int TRANSFER_STATUS_OPERATORS_OK = TRANSFER_STATUS_BEFORE_PREDICTED;

    /**
     * The "aimed" transfer finish time.
     *
     * @return The best possible time, based on line capacity and ideal conditions of pressure and temperature ;-)
     *
     * @throws TransferException
     *             the transfer exception
     */
    Date getTransferTargetTime() throws TransferException;

    /**
     * Guesswork. :-( 1 st dev using last 14 days discarding earliest and latest.
     *
     * @return Predicted value for arrival this time.
     *
     * @throws TransferException
     *             the transfer exception
     */
    Date getTransferPredictedTime() throws TransferException;

    /**
     * Gets the transfer status.
     *
     * @return A calculated value based on the other three parameters.
     *
     * @throws TransferException
     *             the transfer exception
     */
    int getTransferStatus() throws TransferException;

    /**
     * Gets the transfer status hex color.
     *
     * @return the transfer status hex color
     *
     * @throws TransferException
     *             the transfer exception
     */
    String getTransferStatusHexColor() throws TransferException;

    /**
     * Gets the transfer earliest time.
     *
     * @return The earliest transfer finish time for a default number of days
     *
     * @throws TransferException
     *             the transfer exception
     */
    Date getTransferEarliestTime() throws TransferException;

    /**
     * Gets the transfer latest time.
     *
     * @return The latest transfer finish time for a default number of days
     *
     * @throws TransferException
     *             the transfer exception
     */
    Date getTransferLatestTime() throws TransferException;
}
