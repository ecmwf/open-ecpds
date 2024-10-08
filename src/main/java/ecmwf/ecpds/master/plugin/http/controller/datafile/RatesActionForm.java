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

package ecmwf.ecpds.master.plugin.http.controller.datafile;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <sy8iecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.ArrayList;
import java.util.Collection;

import javax.management.timer.Timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.text.Format;
import ecmwf.ecpds.master.plugin.http.home.datafile.TransferServerHome;
import ecmwf.ecpds.master.plugin.http.model.datafile.DataFileException;
import ecmwf.ecpds.master.plugin.http.model.datafile.TransferServer;
import ecmwf.web.controller.ECMWFActionForm;
import ecmwf.web.util.bean.Pair;

/**
 * The Class RatesActionForm.
 */
public class RatesActionForm extends ECMWFActionForm {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 6716935024591378992L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(RatesActionForm.class);

    /** The to date (today by default). */
    private String toDate = Format.formatTime("yyyy-MM-dd", System.currentTimeMillis());

    /** The from date (4 days ago by default). */
    private String fromDate = Format.formatTime("yyyy-MM-dd", System.currentTimeMillis() - 4 * Timer.ONE_DAY);

    /** The to time. */
    private String toTime = "23:59:59";

    /** The from time. */
    private String fromTime = "00:00:00";

    /** The caller. */
    private String caller = "*q2diss*";

    /** The source host. */
    private String sourceHost = "*";

    /** The per transfer server. */
    private String perTransferServer = "off";

    /** The transfer server name. */
    private String transferServerName = null;

    /**
     * Gets the caller.
     *
     * @return the caller
     */
    public String getCaller() {
        return caller;
    }

    /**
     * Gets the source host.
     *
     * @return the source host
     */
    public String getSourceHost() {
        return sourceHost;
    }

    /**
     * Sets the caller.
     *
     * @param caller
     *            the new caller
     */
    public void setCaller(final String caller) {
        this.caller = caller;
    }

    /**
     * Sets the source host.
     *
     * @param sourceHost
     *            the new source host
     */
    public void setSourceHost(final String sourceHost) {
        this.sourceHost = sourceHost;
    }

    /**
     * Gets the transfer server name.
     *
     * @return the transfer server name
     */
    public String getTransferServerName() {
        return transferServerName;
    }

    /**
     * Sets the transfer server name.
     *
     * @param transferServerName
     *            the new transfer server name
     */
    public void setTransferServerName(final String transferServerName) {
        this.transferServerName = transferServerName;
    }

    /**
     * Gets the per transfer server.
     *
     * @return the per transfer server
     */
    public String getPerTransferServer() {
        return perTransferServer;
    }

    /**
     * Gets the per transfer server boolean.
     *
     * @return the per transfer server boolean
     */
    public boolean getPerTransferServerBoolean() {
        return convertToBoolean(perTransferServer);
    }

    /**
     * Sets the per transfer server.
     *
     * @param perTransferServer
     *            the new per transfer server
     */
    public void setPerTransferServer(final String perTransferServer) {
        this.perTransferServer = perTransferServer;
    }

    /**
     * Gets the from date.
     *
     * @return the from date
     */
    public String getFromDate() {
        return fromDate;
    }

    /**
     * Sets the from date.
     *
     * @param fromDate
     *            the new from date
     */
    public void setFromDate(final String fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * Gets the to date.
     *
     * @return the to date
     */
    public String getToDate() {
        return toDate;
    }

    /**
     * Sets the to date.
     *
     * @param toDate
     *            the new to date
     */
    public void setToDate(final String toDate) {
        this.toDate = toDate;
    }

    /**
     * Gets the from time.
     *
     * @return the from time
     */
    public String getFromTime() {
        return fromTime;
    }

    /**
     * Sets the from time.
     *
     * @param fromTime
     *            the new from time
     */
    public void setFromTime(final String fromTime) {
        this.fromTime = fromTime;
    }

    /**
     * Gets the to time.
     *
     * @return the to time
     */
    public String getToTime() {
        return toTime;
    }

    /**
     * Sets the to time.
     *
     * @param toTime
     *            the new to time
     */
    public void setToTime(final String toTime) {
        this.toTime = toTime;
    }

    /**
     * Gets the transfer servers.
     *
     * @return the transfer servers
     */
    public Collection<Pair> getTransferServers() {
        final Collection<Pair> options = new ArrayList<>();
        options.add(new Pair("All", "All Transfer Servers"));
        try {
            for (final TransferServer ts : TransferServerHome.findAll()) {
                options.add(
                        new Pair(ts.getName(), ts.getName() + " (" + (ts.getActive() ? "active" : "not-active") + ")"));
            }
        } catch (final DataFileException e) {
            log.error("Problem getting TransferServers", e);
        }
        return options;
    }
}
