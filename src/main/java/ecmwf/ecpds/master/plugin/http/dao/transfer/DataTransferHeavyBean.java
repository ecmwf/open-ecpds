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

import ecmwf.ecpds.master.plugin.http.home.datafile.TransferServerHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.HostHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferServer;
import ecmwf.ecpds.master.plugin.http.model.transfer.ArrivalMonitoringParameters;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMonitoringParameters;

/**
 * The Class DataTransferHeavyBean.
 */
public class DataTransferHeavyBean extends DataTransferBaseBean {

    /** The arrival parameters. */
    private final ArrivalMonitoringParameters arrivalParameters;

    /** The transfer parameters. */
    private final TransferMonitoringParameters transferParameters;

    /** The data file. */
    private final DataFile dataFile;

    /**
     * Instantiates a new data transfer heavy bean.
     *
     * @param transfer
     *            the transfer
     */
    public DataTransferHeavyBean(final ecmwf.common.database.DataTransfer transfer) {
        super(transfer);
        dataFile = new DataFileBean(transfer.getDataFile());
        final var transferMonitoringValue = transfer.getMonitoringValue();
        final var datafileMonitoringValue = transfer.getDataFile().getMonitoringValue();
        if (transferMonitoringValue != null && datafileMonitoringValue != null) {
            arrivalParameters = new ArrivalMonitoringParametersBean(this, transferMonitoringValue, dataFile,
                    datafileMonitoringValue);
            transferParameters = new TransferMonitoringParametersBean(this, transferMonitoringValue, dataFile,
                    datafileMonitoringValue);
        } else {
            arrivalParameters = null;
            transferParameters = null;
        }
    }

    /**
     * Gets the data file.
     *
     * @return the data file
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public DataFile getDataFile() throws TransferException {
        return dataFile;
    }

    /**
     * Gets the arrival monitoring parameters.
     *
     * @return the arrival monitoring parameters
     */
    @Override
    public ArrivalMonitoringParameters getArrivalMonitoringParameters() {
        return arrivalParameters;
    }

    /**
     * Gets the transfer monitoring parameters.
     *
     * @return the transfer monitoring parameters
     */
    @Override
    public TransferMonitoringParameters getTransferMonitoringParameters() {
        return transferParameters;
    }

    /**
     * Gets the destination.
     *
     * @return the destination
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Destination getDestination() throws TransferException {
        return DestinationHome.findByPrimaryKey(getDestinationName());
    }

    /**
     * Gets the host.
     *
     * @return the host
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public Host getHost() throws TransferException {
        final var hostName = getHostName();
        if (hostName == null) {
            throw new TransferException("No host assigned yet for transfer " + getId());
        }
        return HostHome.findByPrimaryKey(hostName);
    }

    /**
     * Gets the transfer server.
     *
     * @return the transfer server
     *
     * @throws TransferException
     *             the transfer exception
     */
    @Override
    public TransferServer getTransferServer() throws TransferException {
        try {
            return TransferServerHome.findByDataTransfer(this);
        } catch (final DataFileException e) {
            throw new TransferException("Problem getting a TransferServer for the DataTransfer", e);
        }
    }
}
