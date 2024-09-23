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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.awt.Color;
import java.util.Date;

import ecmwf.common.database.MonitoringValue;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.transfer.ArrivalMonitoringParameters;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;

/**
 * The Class ArrivalMonitoringParametersBean.
 */
public class ArrivalMonitoringParametersBean extends MonitoringParametersBaseBean
        implements ArrivalMonitoringParameters {

    /**
     * Instantiates a new arrival monitoring parameters bean.
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
    public ArrivalMonitoringParametersBean(final DataTransfer dt, final MonitoringValue mvTransfer, final DataFile df,
            final MonitoringValue mvArrival) {
        super(dt, mvTransfer, df, mvArrival);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the arrival target time.
     */
    @Override
    public Date getArrivalTargetTime() {
        return new Date(this.getDataTransfer().getScheduledTime().getTime() - ANTICIPATION_TARGET1 * 60 * 1000);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the arrival predicted time.
     */
    @Override
    public synchronized Date getArrivalPredictedTime() {
        Date d;
        if ((d = this.getArrivalMonitoring().getPredictedTime()) != null) {
            return d;
        }
        calculateAndSaveMonitoringTimes();
        return this.getArrivalMonitoring().getPredictedTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the arrival status.
     */
    @Override
    public int getArrivalStatus() {
        var d = getDataFile().getArrivedTime();
        if (d == null) {
            d = new Date();
        }
        if (d.before(this.getArrivalTargetTime())) {
            return ARRIVAL_STATUS_BEFORE_TARGET1;
        }
        if (d.before(this.getArrivalPredictedTime())) {
            return ARRIVAL_STATUS_BEFORE_PREDICTED;
        } else if (d.before(getDataTransfer().getScheduledTimeMinusMinutes(ANTICIPATION_TARGET2))) {
            return ARRIVAL_STATUS_BEFORE_TARGET2;
        } else if (!d.before(getDataTransfer().getScheduledTimeMinusMinutes(ANTICIPATION_TARGET4))) {
            return ARRIVAL_STATUS_NOT_BEFORE_TARGET4;
        } else if (!d.before(getDataTransfer().getScheduledTimeMinusMinutes(ANTICIPATION_TARGET3))) {
            return ARRIVAL_STATUS_NOT_BEFORE_TARGET3;
        } else if (!d.before(getDataTransfer().getScheduledTimeMinusMinutes(ANTICIPATION_TARGET2))) {
            return ARRIVAL_STATUS_NOT_BEFORE_TARGET2;
        } else {
            return ARRIVAL_STATUS_NONE;
        }
    }

    /**
     * Gets the arrival status color.
     *
     * @return the arrival status color
     */
    public Color getArrivalStatusColor() {
        final var status = this.getArrivalStatus();
        if (status < COLORS.length) {
            return COLORS[status];
        }
        return Color.BLACK;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the arrival earliest time.
     */
    @Override
    public synchronized Date getArrivalEarliestTime() {
        Date d;
        if ((d = this.getArrivalMonitoring().getEarliestTime()) != null) {
            return d;
        }
        this.calculateAndSaveMonitoringTimes();
        return this.getArrivalMonitoring().getEarliestTime();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the arrival latest time.
     */
    @Override
    public synchronized Date getArrivalLatestTime() {
        Date d;
        if ((d = this.getArrivalMonitoring().getLatestTime()) != null) {
            return d;
        }
        this.calculateAndSaveMonitoringTimes();
        return this.getArrivalMonitoring().getLatestTime();
    }
}
