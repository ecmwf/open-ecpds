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

package ecmwf.ecpds.master.plugin.http.home.datafile.searches;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Date;

import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class RatesByDates.
 */
public class RatesByDates extends ModelSearchBase {

    /** The from date. */
    private final Date fromDate;

    /** The to date. */
    private final Date toDate;

    /** The transfer server name. */
    private final String transferServerName;

    /** The caller. */
    private final String caller;

    /** The source host. */
    private final String sourceHost;

    /** The per transfer server. */
    private final boolean perTransferServer;

    /**
     * Instantiates a new rates by dates.
     *
     * @param fromDate
     *            the from date
     * @param toDate
     *            the to date
     * @param perTransferServer
     *            the per transfer server
     * @param caller
     *            the caller
     * @param sourceHost
     *            the source host
     */
    public RatesByDates(final Date fromDate, final Date toDate, final boolean perTransferServer, final String caller,
            final String sourceHost) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.transferServerName = null;
        this.caller = caller;
        this.sourceHost = sourceHost;
        this.perTransferServer = perTransferServer;
    }

    /**
     * Instantiates a new rates by dates.
     *
     * @param fromDate
     *            the from date
     * @param toDate
     *            the to date
     * @param transferServerName
     *            the transfer server name
     * @param caller
     *            the caller
     * @param sourceHost
     *            the source host
     */
    public RatesByDates(final Date fromDate, final Date toDate, final String transferServerName, final String caller,
            final String sourceHost) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.transferServerName = transferServerName;
        this.caller = caller;
        this.sourceHost = sourceHost;
        this.perTransferServer = true;
    }

    /**
     * Gets the from date.
     *
     * @return the from date
     */
    public Date getFromDate() {
        return this.fromDate;
    }

    /**
     * Gets the to date.
     *
     * @return the to date
     */
    public Date getToDate() {
        return this.toDate;
    }

    /**
     * Gets the transfer server name.
     *
     * @return the transfer server name
     */
    public String getTransferServerName() {
        return this.transferServerName;
    }

    /**
     * Gets the per transfer server.
     *
     * @return the per transfer server
     */
    public boolean getPerTransferServer() {
        return this.perTransferServer;
    }

    /**
     * Gets the caller.
     *
     * @return the caller
     */
    public String getCaller() {
        return this.caller;
    }

    /**
     * Gets the source host.
     *
     * @return the source host
     */
    public String getSourceHost() {
        return this.sourceHost;
    }
}
