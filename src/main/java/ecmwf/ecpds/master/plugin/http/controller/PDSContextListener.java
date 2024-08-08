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

package ecmwf.ecpds.master.plugin.http.controller;

/**
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * Base Action for all PDS actions. If performs authorization before handling
 * the request to a subclass.
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import javax.management.timer.Timer;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.Cnf;
import ecmwf.ecpds.master.plugin.http.HandlerReceiver;
import ecmwf.ecpds.master.plugin.http.dao.monitoring.DestinationProductStatusResetterTask;
import ecmwf.ecpds.master.plugin.http.dao.monitoring.MonitoringEventHandler;
import ecmwf.ecpds.master.plugin.http.dao.monitoring.MonitoringStatusCalculatorTask;
import ecmwf.web.controller.CmsInfo;
import ecmwf.web.services.config.ConfigException;
import ecmwf.web.services.config.ConfigService;

/**
 * The listener interface for receiving PDSContext events. The class that is interested in processing a PDSContext event
 * implements this interface, and the object created with that class is registered with a component using the
 * component's <code>addPDSContextListener</code> method. When the PDSContext event occurs, that object's appropriate
 * method is invoked.
 *
 * @see PDSContextEvent
 */
public class PDSContextListener implements ServletContextListener {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(PDSContextListener.class);

    /** The monitoring task. */
    private MonitoringStatusCalculatorTask _monitoringTask = null;

    /** The destination product task. */
    private DestinationProductStatusResetterTask _destinationProductTask = null;

    /**
     * Context destroyed.
     *
     * @param event
     *            the event
     */
    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        _log.info("Shutting down ECMWF webapp " + CmsInfo.getCmsInfo() + " at "
                + event.getServletContext().getRealPath("/") + ".");
        _monitoringTask.shutdown();
        _destinationProductTask.shutdown();
        _log.info("Context destroyed " + event);
    }

    /**
     * Context initialized.
     *
     * @param event
     *            the event
     */
    @Override
    public void contextInitialized(final ServletContextEvent event) {
        final var realPath = event.getServletContext().getRealPath("/");
        _log.info("Initializing ECMWF webapp v" + CmsInfo.getCmsInfo() + " at " + realPath + ".");
        try {
            ConfigService.init(realPath, "/WEB-INF/config/");
        } catch (final ConfigException e) {
            _log.error("Problem initializing ConfigService", e);
        }
        if (Cnf.at("MonitorPlugin", "statusCalculator", false)) {
            final var receiver = (HandlerReceiver) event.getServletContext().getAttribute("ecpds.HttpPlugin");
            _log.info("Starting statusCalculator");
            _monitoringTask = new MonitoringStatusCalculatorTask("StatusCalculator");
            _monitoringTask.setJammedTimeout(Cnf.at("MonitorPlugin", "statusCalculatorTimeout", 15) * Timer.ONE_MINUTE);
            _monitoringTask.start();
            _destinationProductTask = new DestinationProductStatusResetterTask("DestinationProductStatusResetter");
            _destinationProductTask.setJammedTimeout(
                    Cnf.at("MonitorPlugin", "destinationProductStatusResetterTimeout", 15) * Timer.ONE_MINUTE);
            _destinationProductTask.start();
            receiver.registerEventHandler(new MonitoringEventHandler());
            _log.info("Finished Starting statusCalculator");
        }
    }
}
