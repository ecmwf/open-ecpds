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
 * Switches to GREEN the RealTimeTransferStatus parameter of a
 * DestinationProductStatus when all the transfer for that Dest, Prod and Time
 * are COMPLETED.
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.DataTransferEventRequest;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.dao.PDSDAOBase;
import ecmwf.ecpds.master.plugin.http.home.monitoring.DestinationProductStatusHome;
import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationProductStatus;
import ecmwf.ecpds.master.plugin.http.model.transfer.ArrivalMonitoringParameters;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMonitoringParameters;

/**
 * The Class DestinationProductStatusResetterTask.
 */
public class DestinationProductStatusResetterTask extends MBeanScheduler {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DestinationProductStatusResetterTask.class);

    /** The last check done. */
    private Date lastCheckDone = new Date(0);

    /** The previous check. */
    private static Date previousCheck = new Date(0);

    /**
     * Instantiates a new destination product status resetter task.
     *
     * @param group
     *            the group
     * @param name
     *            the name
     */
    public DestinationProductStatusResetterTask(final String group, final String name) {
        super(group, name);
        // setDelay(5 * Timer.ONE_MINUTE);
        setDelay(Cnf.at("MonitorPlugin", "refreshDelay", 20 * Timer.ONE_SECOND));
    }

    /**
     * Instantiates a new destination product status resetter task.
     *
     * @param name
     *            the name
     */
    public DestinationProductStatusResetterTask(final String name) {
        this("ECaccess:service", name);
    }

    /**
     * Gets the previous check date.
     *
     * @return the previous check date
     */
    public static final Date getPreviousCheckDate() {
        return previousCheck;
    }

    /**
     * Next step.
     *
     * @return the int
     */
    @Override
    public int nextStep() {
        try {
            final var start = System.currentTimeMillis();
            log.info("Started updating DestinationProductStatus RealTime Monitoring Status.");
            previousCheck = lastCheckDone; // Last check date to be
                                           // used in this run
            lastCheckDone = new Date(); // Set the value here, not at
            // the end of the run, since we
            // could lose DONEs set during this run.
            updateDestinationProductRealTimeStatii(previousCheck);
            log.info("Finished updating DestinationProductStatus RealTime Monitoring Status. Time taken: "
                    + (System.currentTimeMillis() - start) / 1000 + " seconds.");
        } catch (final Exception e) {
            log.error("Error calculating Monitoring Status", e);
        }
        return NEXT_STEP_DELAY;
    }

    /**
     * Fill data in the DestinationStatus cache.
     *
     * @param previousCheck
     *            the previous check
     */
    private static final void updateDestinationProductRealTimeStatii(final Date previousCheck) {
        try {
            final var c = DestinationProductStatusHome.findAll();
            final Collection<DataTransferEventRequest> statiiToReset = new ArrayList<>(0);
            for (DestinationProductStatus status : c) {
                if (status.isPresent() && (status
                        .getRealTimeTransferStatus() > TransferMonitoringParameters.TRANSFER_STATUS_OPERATORS_OK
                        || status
                                .getRealTimeArrivalStatus() > ArrivalMonitoringParameters.ARRIVAL_STATUS_OPERATORS_OK)) {
                    // Present and Status worse than green
                    // Check if it received a DONE after last time we run
                    if (status.getWhenReceivedLastDone() == null
                            || previousCheck.before(status.getWhenReceivedLastDone())) {
                        // Now, differently to what we do in
                        // DestinationProductStatusCalculatorTask, we will
                        // reset this DestinationProductStatus to green and
                        // re-request all its events, which will
                        // set the last status.
                        statiiToReset
                                .add(new DataTransferEventRequest(PDSDAOBase.getBaseDate(status.getProductTime(), 0),
                                        PDSDAOBase.getBaseDate(status.getProductTime(), 1), status.getDestinationName(),
                                        status.getProduct(), status.getTime()));
                        if (log.isDebugEnabled()) {
                            log.debug("Checked STATUS :" + status.getDestinationName() + "," + status.getProduct()
                                    + ", " + status.getTime()
                                    + ". RESET TO GREEN and added to the list for event resend.");
                        }

                    } else if (log.isDebugEnabled()) {
                        log.debug("Checked STATUS :" + status.getDestinationName() + "," + status.getProduct() + ", "
                                + status.getTime() + ". Not reset (no new DONE events (Last done: "
                                + status.getWhenReceivedLastDone() + "))");
                    }
                }
            }
            final var requests = new DataTransferEventRequest[statiiToReset.size()];
            final var mi = MasterManager.getMI();
            mi.resendDataTransferEvents(MasterManager.getRoot(), statiiToReset.toArray(requests));
        } catch (final Exception e) {
            log.debug("Problem updating 'DestinationProductStatus' cache entry", e);
        }
    }
}
