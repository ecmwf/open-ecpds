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

import ecmwf.ecpds.master.plugin.http.home.datafile.TransferServerHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferServer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;

/**
 * The Class DataTransferLightBean.
 */
public class DataTransferLightBean extends DataTransferBaseBean {

    /** The can be requeued. */
    private boolean canBeRequeued = false;

    /** The can be stopped. */
    private boolean canBeStopped = false;

    /** The can be put on hold. */
    private boolean canBePutOnHold = false;

    /**
     * Instantiates a new data transfer light bean.
     *
     * @param transfer
     *            the transfer
     */
    protected DataTransferLightBean(final ecmwf.common.database.DataTransfer transfer) {
        super(transfer);
    }

    /**
     * {@inheritDoc}
     *
     * Gets the destination.
     */
    @Override
    public Destination getDestination() throws TransferException {
        return DestinationHome.findByPrimaryKey(getDestinationName());
    }

    /**
     * {@inheritDoc}
     *
     * Gets the transfer server.
     */
    @Override
    public TransferServer getTransferServer() throws TransferException {
        try {
            return TransferServerHome.findByDataTransfer(this);
        } catch (final DataFileException e) {
            throw new TransferException("Problem getting a TransferServer for the DataTransfer", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the can be requeued.
     */
    @Override
    public boolean getCanBeRequeued() {
        return this.canBeRequeued;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the can be put on hold.
     */
    @Override
    public boolean getCanBePutOnHold() {
        return this.canBePutOnHold;
    }

    /**
     * {@inheritDoc}
     *
     * Gets the can be stopped.
     */
    @Override
    public boolean getCanBeStopped() {
        return this.canBeStopped;
    }

    /**
     * Sets the can be requeued.
     *
     * @param canBeRequeued
     *            the new can be requeued
     */
    protected void setCanBeRequeued(final boolean canBeRequeued) {
        this.canBeRequeued = canBeRequeued;
    }

    /**
     * Sets the can be put on hold.
     *
     * @param canBePutOnHold
     *            the new can be put on hold
     */
    protected void setCanBePutOnHold(final boolean canBePutOnHold) {
        this.canBePutOnHold = canBePutOnHold;
    }

    /**
     * Sets the can be stopped.
     *
     * @param canBeStopped
     *            the new can be stopped
     */
    protected void setCanBeStopped(final boolean canBeStopped) {
        this.canBeStopped = canBeStopped;
    }
}
