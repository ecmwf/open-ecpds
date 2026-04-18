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
 * Takes in consideration PRODUCT_STATUS information to generate
 * monitoring information.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.ecaccess.MBeanScheduler;
import ecmwf.common.monitor.MonitorManager;
import ecmwf.ecpds.master.MasterManager;
import ecmwf.ecpds.master.plugin.http.controller.monitoring.MonitoringRequest;
import ecmwf.ecpds.master.plugin.http.home.monitoring.DestinationProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStepStatusHome;
import ecmwf.ecpds.master.plugin.http.home.transfer.DestinationHome;
import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationProductStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.DestinationStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.GenerationMonitoringStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStatus;
import ecmwf.ecpds.master.plugin.http.model.monitoring.ProductStepStatus;
import ecmwf.ecpds.master.plugin.http.model.transfer.Destination;
import ecmwf.ecpds.master.plugin.http.model.transfer.Host;
import ecmwf.ecpds.master.plugin.http.model.transfer.Status;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.web.util.bean.Pair;

/**
 * The Class MonitoringStatusCalculatorTask.
 */
public class MonitoringStatusCalculatorTask extends MBeanScheduler {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(MonitoringStatusCalculatorTask.class);

    /** The Constant CODES. */
    private static final String[] CODES = { Status.INIT, Status.EXEC, Status.DONE, "" };

    /**
     * Instantiates a new monitoring status calculator task.
     *
     * @param name
     *            the name
     */
    public MonitoringStatusCalculatorTask(final String name) {
        super("ECaccess:service", name);
        setDelay(60 * Timer.ONE_SECOND);
    }

    /**
     * {@inheritDoc}
     *
     * Next step.
     */
    @Override
    public int nextStep() {
        try {
            final var start = System.currentTimeMillis();
            log.info("Started calculating Monitoring Status");
            fillProductStatii();
            final var contactsPerDestinations = fillDestinationStatii();
            if (!contactsPerDestinations.isEmpty()) {
                MonitoringRequest.refreshContactsList(contactsPerDestinations);
            }
            log.info("Finished calculating Monitoring Status. Time taken: "
                    + (System.currentTimeMillis() - start) / 1000 + " seconds.");
        } catch (final Exception e) {
            log.error("Error calculating Monitoring Status", e);
        }
        return NEXT_STEP_DELAY;
    }

    /**
     * Fill data in the DestinationStatus cache and return the contacts per destinations!
     *
     * @return the hash map
     */
    private static final HashMap<String, String> fillDestinationStatii() {
        final var contactsPerDestinations = new HashMap<String, String>();
        try {
            try {
                contactsPerDestinations.putAll(MasterManager.getMI().getContacts());
            } catch (final Throwable t) {
                log.warn("Couldn't get contacts per Destinations", t);
            }
            for (final Destination destination : DestinationHome.findAll()) {
                final var ds = destination.getMonitoringStatus();
                ds.setCalculationDate(new Date());
                // Bad data transfers
                ds.setBadDataTransfersSize(destination.getBadDataTransfersSize());
                // Last successful transfer
                ds.setLastTransfer(destination.getLastTransfer());
                // Queue size
                try {
                    ds.setQueueSize(MasterManager.getMI().getPendingDataTransfersCount(destination.getName()));
                } catch (final Exception e) {
                    log.warn("Error trying to get Queue Size for destination '" + destination.getName() + "'", e);
                }
                // Primary host information for dissemination ... ?
                Host primaryHost = null;
                var maxPriority = Integer.MAX_VALUE;
                for (final Pair pair : destination.getDisseminationHostsAndPriorities()) {
                    final var host = (Host) pair.getName();
                    final var priority = (Integer) pair.getValue();
                    if (priority < maxPriority) {
                        primaryHost = host;
                        maxPriority = priority;
                    }
                }
                ds.setPrimaryHost(primaryHost);
                ds.setStatus(destination.getFormattedStatus());
                try {
                    final var bsm = MasterManager.getMI().getMonitorManager(destination.getName());
                    ds.setBigSisterStatus(bsm.getStatus());
                    ds.setBigSisterStatusComment(bsm.getComment() != null ? bsm.getComment() : "");
                } catch (final Exception e) {
                    final var message = e.getMessage();
                    if (!"MonitorManager is off line".equals(message)
                            && !"MonitoringThread is off line".equals(message)) {
                        log.warn("Problem getting Monitor status for destination '" + destination.getName() + "'", e);
                    }
                }
            }
        } catch (final TransferException e) {
            log.warn("Problem writing 'DestinationProductStatus' cache entry", e);
        }
        return contactsPerDestinations;
    }

    /**
     * Update ProductStatus and ProductStepStatus in the absence of events.
     */
    private static final void fillProductStatii() {
        try {
            // Update the status of all ProductStepStatus
            final List<ProductStatus> updatedProducts = new ArrayList<>();
            for (final ProductStepStatus sStatus : ProductStepStatusHome.findAll()) {
                // Refresh ProductStepStatus
                final var code = sStatus.getGenerationStatusCode();
                sStatus.refreshStatus();
                // Set ProductStatus to the lowest Status (INIT<EXEC<DONE) of
                // the steps, during this run
                final var pStatus = ProductStatusHome.findByProduct(sStatus.getProduct(), sStatus.getTime());
                if (updatedProducts.contains(pStatus)) {
                    if (lowerCode(code, pStatus.getGenerationStatusCode())) {
                        pStatus.setGenerationStatusFromCode(code);
                    }
                    if (sStatus.getGenerationStatus() > pStatus.getGenerationStatus()) {
                        pStatus.setGenerationStatus(sStatus.getGenerationStatus());
                    }
                } else {
                    updatedProducts.add(pStatus);
                    pStatus.setGenerationStatusFromCode(code);
                    pStatus.setGenerationStatus(sStatus.getGenerationStatus());
                }
            }
            // Update the status of all DestinationProductStatus
            for (final DestinationProductStatus status : DestinationProductStatusHome.findAll()) {
                status.setGenerationStatus(status.isPresent()
                        ? ProductStatusHome.findByProduct(status.getProduct(), status.getTime()).getGenerationStatus()
                        : GenerationMonitoringStatus.STATUS_NONE);
            }
        } catch (final MonitoringException e) {
            log.debug("Problem updating 'ProductStepStatus' cache entry", e);
        }
    }

    /**
     * Lower code.
     *
     * @param codeA
     *            the code A
     * @param codeB
     *            the code B
     *
     * @return true, if successful
     */
    private static final boolean lowerCode(final String codeA, final String codeB) {
        int posA = 0, posB = 0;
        for (var i = 0; i < CODES.length; i++) {
            if (CODES[i].equals(codeA)) {
                posA = i;
            }
            if (CODES[i].equals(codeB)) {
                posB = i;
            }
        }
        return posA < posB;
    }
}
