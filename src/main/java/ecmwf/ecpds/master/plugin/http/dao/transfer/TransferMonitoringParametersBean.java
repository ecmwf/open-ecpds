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

package ecmwf.ecpds.master.plugin.http.dao.transfer;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.awt.Color;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.MonitoringValue;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMonitoringParameters;

/**
 * The Class TransferMonitoringParametersBean.
 */
public class TransferMonitoringParametersBean extends MonitoringParametersBaseBean
        implements TransferMonitoringParameters {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(TransferMonitoringParametersBean.class);

    /**
     * Instantiates a new transfer monitoring parameters bean.
     *
     * @param dt
     *            the dt
     * @param mvTransfer
     *            the mv transfer
     * @param df
     *            the df
     * @param mvArrival
     *            the mv arrival
     */
    public TransferMonitoringParametersBean(final DataTransfer dt, final MonitoringValue mvTransfer, final DataFile df,
            final MonitoringValue mvArrival) {
        super(dt, mvTransfer, df, mvArrival);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer predicted time.
     */
    @Override
    public Date getTransferPredictedTime() {
        return getTransferPredictedTime(14);
    }

    /**
     * Gets the transfer predicted time.
     *
     * @param days
     *            the days
     *
     * @return the transfer predicted time
     */
    public synchronized Date getTransferPredictedTime(final int days) {
        final Date date;
        if ((date = getTransferMonitoring().getPredictedTime()) != null) {
            return date;
        }
        calculateAndSaveMonitoringTimes();
        return getTransferMonitoring().getPredictedTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer status.
     */
    @Override
    public int getTransferStatus() {
        final var date = getDataTransfer().getFinishTime();
        final var dTime = (date != null ? date : new Date()).getTime();
        final var p = getTransferPredictedTime();
        if (p == null) {
            return TRANSFER_STATUS_NONE;
        }
        final var range = getTransferLatestTime().getTime() - getTransferEarliestTime().getTime();
        final var pTime = p.getTime();
        // >= P + 6r
        var range_6 = range * 6;
        if (range * 6 < 300000) {
            range_6 = 300000; // Make range * 6 be always at least 5 minutes.
        }
        if (dTime >= pTime + range_6) {
            if (log.isDebugEnabled()) {
                log.debug(getDataTransfer() + " has status 6 (red) because 'dTime >= (predictedTime + range * 6)':  "
                        + date + " >= (" + p + " + " + range + " * 6)");
            }
            return TRANSFER_STATUS_NOT_BEFORE_PREDICTED_PLUS_RANGE_BY_6;
            // >= P + 4r
        }
        if (dTime >= pTime + range * 4) {
            if (log.isDebugEnabled()) {
                log.debug(getDataTransfer() + " has status 5 (orange) because 'dTime >= (predictedTime + range * 4)':  "
                        + date + " >= (" + p + " + " + range + " * 4)");
            }
            return TRANSFER_STATUS_NOT_BEFORE_PREDICTED_PLUS_RANGE_BY_4;
            // >= P + 2r
        } else if (dTime >= pTime + range * 2) {
            if (log.isDebugEnabled()) {
                log.debug(getDataTransfer() + " has status 4 (yellow) because 'dTime >= (predictedTime + range * 2)':  "
                        + date + " >= (" + p + " + " + range + " * 2)");
            }
            return TRANSFER_STATUS_NOT_BEFORE_PREDICTED_PLUS_RANGE_BY_2;
            // >= P
        } else if (dTime >= pTime) {
            return TRANSFER_STATUS_BEFORE_PREDICTED_PLUS_RANGE_BY_2;
            // < T
        } else if ((date != null && date.before(getTransferTargetTime())) || (dTime < pTime)) {
            return TRANSFER_STATUS_BEFORE_PREDICTED; // Don't use the "Before Target" status
            // < P
        } else {
            return TRANSFER_STATUS_NONE;
        }
    }

    /**
     * Gets the transfer status color.
     *
     * @return the transfer status color
     */
    public Color getTransferStatusColor() {
        final var status = getTransferStatus();
        return switch (status) {
        case TRANSFER_STATUS_NONE -> Color.WHITE;
        case TRANSFER_STATUS_BEFORE_TARGET -> Color.CYAN;
        case TRANSFER_STATUS_BEFORE_PREDICTED -> Color.GREEN;
        case TRANSFER_STATUS_BEFORE_PREDICTED_PLUS_RANGE_BY_2 -> Color.BLUE;
        case TRANSFER_STATUS_NOT_BEFORE_PREDICTED_PLUS_RANGE_BY_2 -> Color.YELLOW;
        case TRANSFER_STATUS_NOT_BEFORE_PREDICTED_PLUS_RANGE_BY_4 -> Color.ORANGE;
        case TRANSFER_STATUS_NOT_BEFORE_PREDICTED_PLUS_RANGE_BY_6 -> Color.RED;
        default -> Color.BLACK;
        };
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer status hex color.
     */
    @Override
    public String getTransferStatusHexColor() {
        return Integer.toHexString(getTransferStatusColor().getRGB()).substring(2);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer earliest time.
     */
    @Override
    public synchronized Date getTransferEarliestTime() {
        final Date date;
        if ((date = getTransferMonitoring().getEarliestTime()) != null) {
            return date;
        }
        calculateAndSaveMonitoringTimes();
        return getTransferMonitoring().getEarliestTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer latest time.
     */
    @Override
    public synchronized Date getTransferLatestTime() {
        final Date date;
        if ((date = getTransferMonitoring().getLatestTime()) != null) {
            return date;
        }
        calculateAndSaveMonitoringTimes();
        return getTransferMonitoring().getLatestTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer target time.
     */
    @Override
    public synchronized Date getTransferTargetTime() {
        final Date date;
        if ((date = getTransferMonitoring().getTargetTime()) != null) {
            return date;
        }
        calculateAndSaveMonitoringTimes();
        return getTransferMonitoring().getTargetTime();
    }
}
