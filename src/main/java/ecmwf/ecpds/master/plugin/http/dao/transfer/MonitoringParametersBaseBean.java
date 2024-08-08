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
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.sql.Timestamp;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.database.MonitoringValue;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFile;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.web.dao.ModelBeanBase;
import ecmwf.web.home.users.UserHome;

/**
 * The Class MonitoringParametersBaseBean.
 */
public class MonitoringParametersBaseBean extends ModelBeanBase {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(MonitoringParametersBaseBean.class);

    /** The transfer. */
    private final DataTransfer transfer;

    /** The file. */
    private final DataFile file;

    /** The mv arrival. */
    private final MonitoringValue mvArrival;

    /** The mv transfer. */
    private final MonitoringValue mvTransfer;

    /**
     * Instantiates a new monitoring parameters base bean.
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
    public MonitoringParametersBaseBean(final DataTransfer dt, final MonitoringValue mvTransfer, final DataFile df,
            final MonitoringValue mvArrival) {
        this.transfer = dt;
        this.file = df;
        this.mvArrival = mvArrival;
        this.mvTransfer = mvTransfer;
    }

    /**
     * Gets the bean interface name.
     *
     * @return the bean interface name
     */
    @Override
    public String getBeanInterfaceName() {
        return MonitoringParametersBaseBean.class.getName();
    }

    /**
     * Gets the data file.
     *
     * @return the data file
     */
    public DataFile getDataFile() {
        return file;
    }

    /**
     * Gets the data transfer.
     *
     * @return the data transfer
     */
    public DataTransfer getDataTransfer() {
        return transfer;
    }

    /**
     * Gets the arrival monitoring.
     *
     * @return the arrival monitoring
     */
    public MonitoringValue getArrivalMonitoring() {
        return mvArrival;
    }

    /**
     * Gets the transfer monitoring.
     *
     * @return the transfer monitoring
     */
    public MonitoringValue getTransferMonitoring() {
        return mvTransfer;
    }

    /**
     * Calculate and save monitoring times.
     */
    protected void calculateAndSaveMonitoringTimes() {
        try {
            final var collection = DataTransferHome.findByDestinationAndIdentity(transfer.getDestinationName(),
                    transfer.getIdentity());
            if (collection.isEmpty()) {
                log.warn("No previous transfers found for Destination '" + transfer.getDestinationName()
                        + ", identity '" + transfer.getIdentity()
                        + "'. Impossible to calculate MonitoringParameters for DT '" + transfer.getId() + "'");
                return;
            }
            // Arrival
            final var stratA = new EarliestLatestPredictedArrivalStrategy(transfer, file);
            stratA.calculate(collection);
            mvArrival.setEarliestTime(getTimestamp(stratA.getEarliest()));
            mvArrival.setLatestTime(getTimestamp(stratA.getLatest()));
            mvArrival.setPredictedTime(getTimestamp(stratA.getPredicted()));
            file.save(UserHome.findByUidAndPass(Cnf.at("Server", "anonymousUser", "anonymous"),
                    Cnf.at("Server", "anonymousPass", "anonymous"), "local", getClass().getName(), ""));
            // Transfer
            final var stratT = new EarliestLatestPredictedTargetTransferStrategy(transfer, file);
            stratT.calculate(collection);
            mvTransfer.setEarliestTime(getTimestamp(stratT.getEarliest()));
            mvTransfer.setLatestTime(getTimestamp(stratT.getLatest()));
            mvTransfer.setPredictedTime(getTimestamp(stratT.getPredicted()));
            mvTransfer.setTargetTime(getTimestamp(stratT.getTarget()));
            transfer.save(UserHome.findByUidAndPass(Cnf.at("Server", "anonymousUser", "anonymous"),
                    Cnf.at("Server", "anonymousPass", "anonymous"), "local", getClass().getName(), ""));
        } catch (final Exception e) {
            log.debug("Problem calculating or saving MonitoringTimes for Transfer " + transfer.getId() + " for File "
                    + file.getId(), e);
        }
    }

    /**
     * Gets the timestamp.
     *
     * @param d
     *            the d
     *
     * @return the timestamp
     */
    private static final Timestamp getTimestamp(final Date d) {
        return new Timestamp(d.getTime());
    }
}
