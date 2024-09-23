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

import ecmwf.common.plugin.PluginEvent;
import ecmwf.common.technical.Cnf;
import ecmwf.common.technical.Synchronized;
import ecmwf.common.text.Format;
import ecmwf.ecpds.master.ChangeHostEvent;
import ecmwf.ecpds.master.DataTransferEvent;
import ecmwf.ecpds.master.ProductStatusEvent;
import ecmwf.ecpds.master.ResetDestinationProductEvent;
import ecmwf.ecpds.master.ResetProductEvent;
import ecmwf.ecpds.master.plugin.http.EventHandler;
import ecmwf.ecpds.master.plugin.http.dao.transfer.DataTransferHeavyBean;
import ecmwf.ecpds.master.plugin.http.home.monitoring.DestinationProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.DestinationStatusHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStatusHome;
import ecmwf.ecpds.master.plugin.http.home.monitoring.ProductStepStatusHome;
import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringException;
import ecmwf.ecpds.master.plugin.http.model.monitoring.MonitoringStatus;
import ecmwf.ecpds.master.plugin.http.model.transfer.ArrivalMonitoringParameters;
import ecmwf.ecpds.master.plugin.http.model.transfer.DataTransfer;
import ecmwf.ecpds.master.plugin.http.model.transfer.Status;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferException;
import ecmwf.ecpds.master.plugin.http.model.transfer.TransferMonitoringParameters;
import ecmwf.web.model.ModelException;

/**
 * The Class MonitoringEventHandler.
 */
public class MonitoringEventHandler implements EventHandler {

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(MonitoringEventHandler.class);

    /** The Constant PRODUCT_STATUS_EVENT_MAX_AGE. */
    private static final int PRODUCT_STATUS_EVENT_MAX_AGE = Cnf.at("MonitorPlugin", "productStatusEventMaxAge", 7);

    /** The Constant monitoringEventMutex. */
    private static final Synchronized monitoringEventMutex = Synchronized.getInstance(MonitoringEventHandler.class);

    /**
     * Instantiates a new monitoring event handler.
     */
    public MonitoringEventHandler() {
        log.debug("EventHandler() > Thread.ContextClassLoader: " + Thread.currentThread().getContextClassLoader()
                + ", ThreadGroup: " + Thread.currentThread().getThreadGroup().getName() + ", EventHandler:" + this);
    }

    /**
     * {@inheritDoc}
     *
     * Handle.
     */
    @Override
    public void handle(final PluginEvent<?> ev) {
        try {
            if (ev instanceof final DataTransferEvent dte) {
                final var dt = dte.getDataTransfer();
                final var df = dt.getDataFile();
                if (df.getMonitoringValue() != null && dt.getMonitoringValue() != null) {
                    final var mutex = monitoringEventMutex.getMutex("DT" + dt.getId());
                    synchronized (mutex.lock()) {
                        try {
                            updateStatus(dt, dt.getDestinationName(), df.getMetaStream(), df.getMetaTime(),
                                    df.getTimeBase(), df.getTimeStep(), df.getMetaType());
                        } finally {
                            mutex.free();
                        }
                    }
                } else {
                    log.warn("Event '" + ev + "' has no monitoring value set");
                }
            } else if (ev instanceof final ProductStatusEvent productStatusEvent) {
                final var ps = productStatusEvent.getProductStatus();
                final var mutex = monitoringEventMutex.getMutex("PS" + ps.getStream() + ps.getTime());
                synchronized (mutex.lock()) {
                    try {
                        updateProductStatus(ps);
                    } finally {
                        mutex.free();
                    }
                }
            } else if (ev instanceof final ResetDestinationProductEvent rpe) {
                if (log.isInfoEnabled()) {
                    log.info("Received ResetDestinationProductEvent with dest '" + rpe.getDestinationName()
                            + "' product '" + rpe.getMetaStream() + "' time '" + rpe.getMetaTime() + "'");
                }
                final var mutex = monitoringEventMutex.getMutex("PS" + rpe.getMetaStream() + rpe.getMetaTime());
                synchronized (mutex.lock()) {
                    try {
                        resetDestinationProduct(rpe.getDestinationName(), rpe.getMetaStream(), rpe.getMetaTime());
                    } finally {
                        mutex.free();
                    }
                }
            } else if (ev instanceof final ResetProductEvent rpe) {
                if (log.isInfoEnabled()) {
                    log.info("Received ResetProductEvent with product '" + rpe.getMetaStream() + "' time '"
                            + rpe.getMetaTime() + "'");
                }
                final var mutex = monitoringEventMutex.getMutex("PS" + rpe.getMetaStream() + rpe.getMetaTime());
                synchronized (mutex.lock()) {
                    try {
                        resetProduct(rpe.getMetaStream(), rpe.getMetaTime());
                    } finally {
                        mutex.free();
                    }
                }
            } else if (ev instanceof final ChangeHostEvent che) {
                final var d = che.getDestination();
                if (log.isInfoEnabled()) {
                    log.info("Received ChangeHostEvent for Destination '" + d.getName() + "'");
                }
                final var mutex = monitoringEventMutex.getMutex("CH" + d.getName());
                synchronized (mutex.lock()) {
                    try {
                        setHostChange(che);
                    } finally {
                        mutex.free();
                    }
                }
            } else {
                log.warn("Unknown event '" + ev + "'");
            }
        } catch (final Exception e) {
            log.error("Error handling event '" + ev + "'", e);
        }
    }

    /**
     * Update status.
     *
     * @param dt
     *            the dt
     * @param destinationName
     *            the destination name
     * @param product
     *            the product
     * @param time
     *            the time
     * @param transferProductTime
     *            the transfer product time
     * @param step
     *            the step
     * @param type
     *            the type
     */
    private static final void updateStatus(final ecmwf.common.database.DataTransfer dt, final String destinationName,
            final String product, final String time, final Date transferProductTime, final long step,
            final String type) {
        final DataTransfer t = new DataTransferHeavyBean(dt);
        if (log.isDebugEnabled()) {
            log.debug("Handling DT event ("
                    + getDetails(t, product, time, transferProductTime, Long.toString(step), t.getIdentity()) + ")");
        }
        if (updateProductStatus(t, product, time, transferProductTime)) {
            updateDestinationStatus(t, destinationName);
            updateDestinationProductStatus(t, destinationName, product, time, transferProductTime);
            // updateProductStepStatus(t, product, time, transferProductTime,
            // buffer, step, type);
        }
    }

    /**
     * Update destination status.
     *
     * @param t
     *            the t
     * @param destinationName
     *            the destination name
     */
    private static final void updateDestinationStatus(final DataTransfer t, final String destinationName) {
        try {
            final var status = DestinationStatusHome.findByName(destinationName);
            updateMonitoringStatus(t, status);
        } catch (final MonitoringException e) {
            log.error("Problem updating monitoring info for destination '" + destinationName + "' from transfer '"
                    + t.getId() + "'", e);
        }
    }

    /**
     * Update destination product status.
     *
     * @param t
     *            the t
     * @param destinationName
     *            the destination name
     * @param product
     *            the product
     * @param time
     *            the time
     * @param transferProductTime
     *            the transfer product time
     */
    private static final void updateDestinationProductStatus(final DataTransfer t, final String destinationName,
            final String product, final String time, final Date transferProductTime) {
        try {
            final var status = DestinationProductStatusHome.find(destinationName, product, time);
            updateMonitoringStatus(t, status);
            status.setProductTime(transferProductTime);
            // Update the last event timestamp in every case, to avoid problems
            // with missing DONEs!
            status.setWhenReceivedLastDone(new Date());
        } catch (final MonitoringException e) {
            log.error("Problem updating monitoring info for Destination '" + destinationName + "', Product '" + product
                    + "' from Transfer '" + t.getId() + "'", e);
        }
    }

    /**
     * <p>
     * As a response to a ProductStatusEvent, modify these two caches:
     * <ul>
     * <li>ProductStatus</li>
     * <li>ProductStepStatus</li>
     * </ul>
     * </p>
     * .
     *
     * @param eventStatus
     *            The ProductStatus that comes with the event
     *
     * @throws MonitoringException
     *             the monitoring exception
     * @throws ModelException
     *             the model exception
     */
    private static final void updateProductStatus(final ecmwf.common.database.ProductStatus eventStatus)
            throws MonitoringException, ModelException {
        final var duration = System.currentTimeMillis() - eventStatus.getLastUpdate().getTime();
        if (duration > Timer.ONE_MINUTE) {
            log.warn("Delay in receiving PS event '" + getDetails(eventStatus) + " (" + Format.formatDuration(duration)
                    + ")");
        }
        if (log.isDebugEnabled()) {
            log.debug("Handling PS event '" + getDetails(eventStatus));
        }
        // Discard VERY old events
        if (duration < PRODUCT_STATUS_EVENT_MAX_AGE * 24 * 3600 * 1000L) {
            // Product Status: Global information for the product
            final var pStatus = ProductStatusHome.findByProduct(eventStatus.getStream(), eventStatus.getTime());
            if (!pStatus.isCalculated() && log.isInfoEnabled()) {
                log.info("Creating new entry for product: " + eventStatus.getStream() + ", time: "
                        + eventStatus.getTime());
            }
            if (pStatus.getLastUpdate() == null
                    || eventStatus.getLastUpdate().getTime() > pStatus.getLastUpdate().getTime()) {
                // Older events wont change ProductStatus
                final var oldProductTime = pStatus.getProductTime();
                final var oldProductStatusUpdate = pStatus.getLastUpdate();
                final Date newProductTime = eventStatus.getTimeBase();
                if (oldProductTime != null && newProductTime.getTime() > oldProductTime.getTime()) {
                    log.warn("Cleaning up product '" + pStatus.getProduct() + "' after receiving time '"
                            + newProductTime + "' updated on '" + eventStatus.getLastUpdate() + "'. Previous was '"
                            + oldProductTime + "' updated on '" + oldProductStatusUpdate + "'");
                    pStatus.reset(); // This will clean ProductStatus,
                    // ProductStepStatus and
                    // DestinationProductStatus too.
                }
                pStatus.setLastUpdate(eventStatus.getLastUpdate());
                pStatus.setProductTime(eventStatus.getTimeBase());
                // Set the earliest schedule time from all steps;
                if (pStatus.getScheduledTime() == null
                        || pStatus.getScheduledTime().getTime() > eventStatus.getScheduleTime().getTime()) {
                    pStatus.setScheduledTime(eventStatus.getScheduleTime());
                    // if (needsGuessing)
                    // this.generateGuessesForProductStatus(pStatus);
                }
            } else {
                log.info("ProductStatusEvent " + getDetails(eventStatus)
                        + " discarded because its updateTime is older than current status'");
            }
            pStatus.setPresent(true);
            pStatus.setCalculated(true);
            // ProductStepStatus: Information for the product at one step
            if (pStatus.getProductTime() != null && pStatus.getProductTime().equals(eventStatus.getTimeBase())) {
                final var pSStatus = ProductStepStatusHome.findByProductAndStep(eventStatus.getStream(),
                        eventStatus.getTime(), eventStatus.getBuffer(), eventStatus.getStep(), eventStatus.getType());
                if (pSStatus.getLastUpdate() == null
                        || eventStatus.getLastUpdate().getTime() >= pSStatus.getLastUpdate().getTime()) {
                    log.debug("ProductStatusEvent " + getDetails(eventStatus) + " processed because last update is '"
                            + pSStatus.getLastUpdate() + "'");
                    pSStatus.setLastUpdate(eventStatus.getLastUpdate());
                    pSStatus.setScheduledTime(eventStatus.getScheduleTime());
                    pSStatus.setGenerationStatusFromCode(eventStatus.getStatusCode());
                    pSStatus.setPresent(true);
                    pSStatus.setCalculated(true);
                    pSStatus.setProductTime(eventStatus.getTimeBase());
                } else {
                    log.warn("ProductStatusEvent " + getDetails(eventStatus) + " discarded because event last update '"
                            + eventStatus.getLastUpdate() + "' is older that current last update '"
                            + pSStatus.getLastUpdate() + "'.");
                }
            } else {
                log.warn(
                        "ProductStatusEvent " + getDetails(eventStatus) + " discarded because ProductTime for product '"
                                + pStatus.getProduct() + "' is currently '" + pStatus.getProductTime() + "'.");
            }
        } else {
            log.info("ProductStatusEvent " + getDetails(eventStatus) + " discarded because it is older than "
                    + PRODUCT_STATUS_EVENT_MAX_AGE + " days");
        }
    }

    /**
     * Sets the host change.
     *
     * @param ev
     *            the new host change
     */
    private static final void setHostChange(final ChangeHostEvent ev) {
        try {
            final var d = ev.getDestination();
            final var ds = DestinationStatusHome.findByName(d.getName());
            ds.setCurrentlyUsedHostName(d.getSchedulerValue().getHostName());
        } catch (final MonitoringException e) {
        }
    }

    /**
     * Update product status.
     *
     * @param t
     *            the t
     * @param product
     *            the product
     * @param time
     *            the time
     * @param transferProductTime
     *            the transfer product time
     *
     * @return true, if successful
     */
    private static final boolean updateProductStatus(final DataTransfer t, final String product, final String time,
            final Date transferProductTime) {
        try {
            final var status = ProductStatusHome.findByProduct(product, time);
            if (status.getProductTime() == null || status.getProductTime().equals(transferProductTime)) {
                updateMonitoringStatus(t, status);
                return true;
            }
            log.debug("Ignored event for DataTransfer "
                    + getDetails(t, product, time, transferProductTime, null, t.getIdentity())
                    + " since current ProductStatus time is '" + status.getProductTime() + "'");
            return false;
        } catch (final MonitoringException e) {
            log.error("Problem updating monitoring info for "
                    + getDetails(t, product, time, transferProductTime, null, t.getIdentity()), e);
            return false;
        }
    }

    /**
     * A Generic method that can be used because all the Statii are instances of MonitoringStatus, so at least, the
     * common fields can be filled in a common method.
     *
     * @param t
     *            The Data Transfer source of the information.
     * @param status
     *            The status object to fill.
     *
     * @throws MonitoringException
     *             the monitoring exception
     */
    private static final void updateMonitoringStatus(final DataTransfer t, final MonitoringStatus status)
            throws MonitoringException {
        status.setPresent(true);
        try {
            var aStatus = t.getArrivalStatus();
            var tStatus = t.getTransferStatus();
            if (aStatus > status.getArrivalStatus()) {
                status.setArrivalStatus(aStatus);
            }
            if (tStatus > status.getTransferStatus()) {
                status.setTransferStatus(tStatus);
            }
            // If the Transfer is DONE or HOLD, its RTStatus will be always OK
            if (Status.DONE.equals(t.getStatusCode()) || Status.HOLD.equals(t.getStatusCode())) {
                aStatus = ArrivalMonitoringParameters.ARRIVAL_STATUS_OPERATORS_OK;
                tStatus = TransferMonitoringParameters.TRANSFER_STATUS_OPERATORS_OK;
            }
            if (aStatus > status.getRealTimeArrivalStatus()) {
                if (log.isDebugEnabled()) {
                    log.debug(status + " change from " + status.getRealTimeArrivalStatus() + " to " + aStatus);
                }
                status.setRealTimeArrivalStatus(aStatus);
            }
            if (tStatus > status.getRealTimeTransferStatus()) {
                if (log.isDebugEnabled()) {
                    log.debug(status + " change from " + status.getRealTimeTransferStatus() + " to " + tStatus);
                }
                status.setRealTimeTransferStatus(tStatus);
            }
        } catch (final TransferException e) {
            throw new MonitoringException("Problem updating MonitoringStatus from transfer '" + t.getId() + "'", e);
        }
    }

    /**
     * Reset product.
     *
     * @param product
     *            the product
     * @param time
     *            the time
     *
     * @throws MonitoringException
     *             the monitoring exception
     */
    private static final void resetProduct(final String product, final String time) throws MonitoringException {
        final var status = ProductStatusHome.findByProduct(product, time);
        status.reset();
    }

    /**
     * Reset destination product.
     *
     * @param destination
     *            the destination
     * @param product
     *            the product
     * @param time
     *            the time
     *
     * @throws MonitoringException
     *             the monitoring exception
     */
    private static final void resetDestinationProduct(final String destination, final String product, final String time)
            throws MonitoringException {
        final var status = DestinationProductStatusHome.find(destination, product, time);
        status.setRealTimeTransferStatus(TransferMonitoringParameters.TRANSFER_STATUS_OPERATORS_OK);
        status.setRealTimeArrivalStatus(ArrivalMonitoringParameters.ARRIVAL_STATUS_OPERATORS_OK);
    }

    /**
     * Gets the details.
     *
     * @param t
     *            the t
     * @param product
     *            the product
     * @param time
     *            the time
     * @param productTime
     *            the product time
     * @param step
     *            the step
     * @param identity
     *            the identity
     *
     * @return the details
     */
    private static final String getDetails(final DataTransfer t, final String product, final String time,
            final Date productTime, final String step, final String identity) {
        return "DT: '" + t.getId() + "', ST: '" + t.getStatusCode() + "' DES:'" + t.getDestinationName() + "', Time: '"
                + time + "', P '" + product + "', PT: '" + (productTime != null ? productTime.toString() : "")
                + "', S: '" + (step != null ? step : "") + "', IDENTITY: " + identity;
    }

    /**
     * Gets the details.
     *
     * @param t
     *            the t
     *
     * @return the details
     */
    private static final String getDetails(final ecmwf.common.database.ProductStatus t) {
        return "Product '" + t.getStream() + "', T '" + t.getTimeBase() + "', S '" + t.getStep() + "', T '"
                + t.getType() + "', ST: '" + t.getStatusCode() + "', Sched: '" + t.getScheduleTime() + "', Updated: '"
                + t.getLastUpdate();
    }
}
