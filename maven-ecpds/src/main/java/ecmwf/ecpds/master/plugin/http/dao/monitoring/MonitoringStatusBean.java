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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
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
     * Gets the bean interface name.
     *
     * @return the bean interface name
     */
    @Override
    public String getBeanInterfaceName() {
        return MonitoringStatus.class.getName();
    }

    /**
     * Gets the arrival status.
     *
     * @return the arrival status
     */
    @Override
    public int getArrivalStatus() {
        return arrivalStatus;
    }

    /**
     * Sets the arrival status.
     *
     * @param arrivalStatus
     *            the new arrival status
     */
    @Override
    public void setArrivalStatus(final int arrivalStatus) {
        this.arrivalStatus = arrivalStatus;
    }

    /**
     * Checks if is calculated.
     *
     * @return true, if is calculated
     */
    @Override
    public boolean isCalculated() {
        return calculated;
    }

    /**
     * Sets the calculated.
     *
     * @param calculated
     *            the new calculated
     */
    @Override
    public void setCalculated(final boolean calculated) {
        this.calculated = calculated;
    }

    /**
     * Checks if is present.
     *
     * @return true, if is present
     */
    @Override
    public boolean isPresent() {
        return present;
    }

    /**
     * Sets the present.
     *
     * @param present
     *            the new present
     */
    @Override
    public void setPresent(final boolean present) {
        this.present = present;
    }

    /**
     * Gets the real time arrival status.
     *
     * @return the real time arrival status
     */
    @Override
    public int getRealTimeArrivalStatus() {
        return realTimeArrivalStatus;
    }

    /**
     * Sets the real time arrival status.
     *
     * @param realTimeArrivalStatus
     *            the new real time arrival status
     */
    @Override
    public void setRealTimeArrivalStatus(final int realTimeArrivalStatus) {
        this.realTimeArrivalStatus = realTimeArrivalStatus;
    }

    /**
     * Gets the real time transfer status.
     *
     * @return the real time transfer status
     */
    @Override
    public int getRealTimeTransferStatus() {
        return realTimeTransferStatus;
    }

    /**
     * Sets the real time transfer status.
     *
     * @param realTimeTransferStatus
     *            the new real time transfer status
     */
    @Override
    public void setRealTimeTransferStatus(final int realTimeTransferStatus) {
        this.realTimeTransferStatus = realTimeTransferStatus;
    }

    /**
     * Gets the transfer status.
     *
     * @return the transfer status
     */
    @Override
    public int getTransferStatus() {
        return transferStatus;
    }

    /**
     * Sets the transfer status.
     *
     * @param transferStatus
     *            the new transfer status
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
