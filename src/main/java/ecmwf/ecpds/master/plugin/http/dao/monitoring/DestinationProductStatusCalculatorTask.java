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
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Date;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.ecpds.master.plugin.http.home.monitoring.DestinationProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DataTransferHome;
import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationProductStatus;
import ecmwf.ecpds.master.plugin.http.model.transfer.ArrivalMonitoringParameters;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMonitoringParameters;

/**
 * The Class DestinationProductStatusCalculatorTask.
 */
public class DestinationProductStatusCalculatorTask extends MBeanScheduler {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(DestinationProductStatusCalculatorTask.class);

    /** The last check done. */
    private Date lastCheckDone = new Date(0);

    /**
     * Instantiates a new destination product status calculator task.
     *
     * @param group
     *            the group
     * @param name
     *            the name
     */
    public DestinationProductStatusCalculatorTask(final String group, final String name) {
        super(group, name);
        setDelay(1 * Timer.ONE_MINUTE);
    }

    /**
     * Instantiates a new destination product status calculator task.
     *
     * @param name
     *            the name
     */
    public DestinationProductStatusCalculatorTask(final String name) {
        this("ECaccess:service", name);
    }

    /**
     * Next step.
     *
     * @return the int
     */
    @Override
    public int nextStep() {
        try {
            final var start = new Date();
            log.info("Started updating DestinationProductStatus RealTime Monitoring Status.");
            updateDestinationProductRealTimeStatii(lastCheckDone);
            lastCheckDone = new Date();
            final var end = new Date();
            log.info("Finished updating DestinationProductStatus RealTime Monitoring Status. Time taken: "
                    + (end.getTime() - start.getTime()) / 1000 + " seconds.");
        } catch (final Exception e) {
            log.error("Error calculating Monitoring Status", e);
        }
        return NEXT_STEP_DELAY;
    }

    /**
     * Fill data in the DestinationStatus cache.
     *
     * @param lastCheckDone
     *            the last check done
     */
    private static final void updateDestinationProductRealTimeStatii(final Date lastCheckDone) {
        try {
            final var c = DestinationProductStatusHome.findAll();
            for (DestinationProductStatus status : c) {
                if (status.isPresent() && (status
                        .getRealTimeTransferStatus() > TransferMonitoringParameters.TRANSFER_STATUS_OPERATORS_OK
                        || status
                                .getRealTimeArrivalStatus() > ArrivalMonitoringParameters.ARRIVAL_STATUS_OPERATORS_OK)) {
                    // Present and Status worse than green
                    // Check if it received a DONE after last time we run
                    if (status.getWhenReceivedLastDone() == null
                            || lastCheckDone.before(status.getWhenReceivedLastDone())) {
                        // So, only now, get the number of transfers NOT
                        // DONE for this combination.
                        final var notDone = DataTransferHome.getNotDoneTransferCount(status.getDestinationName(),
                                status.getProduct(), status.getTime(), status.getProductTime());
                        if (notDone == 0) {
                            status.setRealTimeTransferStatus(TransferMonitoringParameters.TRANSFER_STATUS_OPERATORS_OK);
                            status.setRealTimeArrivalStatus(ArrivalMonitoringParameters.ARRIVAL_STATUS_OPERATORS_OK);
                            log.info("Updated STATUS :" + status.getDestinationName() + "," + status.getProduct() + ", "
                                    + status.getTime() + ". Set to GREEN!");

                        } else if (log.isDebugEnabled()) {
                            log.debug("Checked STATUS :" + status.getDestinationName() + "," + status.getProduct()
                                    + ", " + status.getTime() + ". Not reset to GREEN (" + notDone + " still pending)");
                        }
                    } else if (log.isDebugEnabled()) {
                        log.debug("Checked STATUS :" + status.getDestinationName() + "," + status.getProduct() + ", "
                                + status.getTime() + ". Not reset to GREEN (no new DONE events (Last done: "
                                + status.getWhenReceivedLastDone() + "))");
                    }
                }
            }
        } catch (final Exception e) {
            log.debug("Problem updating 'DestinationProductStatus' cache entry", e);
        }
    }
}
