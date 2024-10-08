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

package ecmwf.ecpds.master.plugin.http.dao.monitoring;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringStatus;
import ecmwf.ecpds.master.plugin.http.model.transfer.ArrivalMonitoringParameters;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMonitoringParameters;
import ecmwf.web.dao.ModelBeanBase;

/**
 * The Class MonitoringStatusBean.
 */
public class MonitoringStatusBean extends ModelBeanBase implements MonitoringStatus {

    /** The arrival status. */
    private int arrivalStatus = 0;

    /** The transfer status. */
    private int transferStatus = 0;

    /** The real time arrival status. */
    private int realTimeArrivalStatus = 0;

    /** The real time transfer status. */
    private int realTimeTransferStatus = 0;

    /** The calculated. */
    private boolean calculated = false;

    /** The present. */
    private boolean present = false;

    /**
     * Instantiates a new monitoring status bean.
     *
     * @param calculated
     *            the calculated
     */
    public MonitoringStatusBean(final boolean calculated) {
        this.calculated = calculated;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the bean interface name.
     */
    @Override
    public String getBeanInterfaceName() {
        return MonitoringStatus.class.getName();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the arrival status.
     */
    @Override
    public int getArrivalStatus() {
        return arrivalStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the arrival status.
     */
    @Override
    public void setArrivalStatus(final int arrivalStatus) {
        this.arrivalStatus = arrivalStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is calculated.
     */
    @Override
    public boolean isCalculated() {
        return calculated;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the calculated.
     */
    @Override
    public void setCalculated(final boolean calculated) {
        this.calculated = calculated;
    }

    /**
     * {@inheritDoc}
     *
     * Checks if is present.
     */
    @Override
    public boolean isPresent() {
        return present;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the present.
     */
    @Override
    public void setPresent(final boolean present) {
        this.present = present;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the real time arrival status.
     */
    @Override
    public int getRealTimeArrivalStatus() {
        return realTimeArrivalStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the real time arrival status.
     */
    @Override
    public void setRealTimeArrivalStatus(final int realTimeArrivalStatus) {
        this.realTimeArrivalStatus = realTimeArrivalStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the real time transfer status.
     */
    @Override
    public int getRealTimeTransferStatus() {
        return realTimeTransferStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the real time transfer status.
     */
    @Override
    public void setRealTimeTransferStatus(final int realTimeTransferStatus) {
        this.realTimeTransferStatus = realTimeTransferStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer status.
     */
    @Override
    public int getTransferStatus() {
        return transferStatus;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the transfer status.
     */
    @Override
    public void setTransferStatus(final int transferStatus) {
        this.transferStatus = transferStatus;
    }

    /**
     * Reset.
     */
    public void reset() {
        arrivalStatus = 0;
        transferStatus = 0;
        realTimeArrivalStatus = arrivalStatus;
        realTimeTransferStatus = transferStatus;
        calculated = false;
        present = false;
    }

    /**
     * Sets the is expected.
     */
    public void setIsExpected() {
        this.arrivalStatus = ArrivalMonitoringParameters.ARRIVAL_STATUS_OPERATORS_OK;
        this.transferStatus = TransferMonitoringParameters.TRANSFER_STATUS_OPERATORS_OK;
        this.realTimeArrivalStatus = this.arrivalStatus;
        this.realTimeTransferStatus = this.transferStatus;
        this.present = true;
    }
}
