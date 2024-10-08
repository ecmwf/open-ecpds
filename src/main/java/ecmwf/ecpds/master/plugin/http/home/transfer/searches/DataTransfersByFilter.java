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

package ecmwf.ecpds.master.plugin.http.home.transfer.searches;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Daniel Varela Santoalla - sy8@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Date;

import ecmwf.common.database.DataBaseCursor;
import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class DataTransfersByFilter.
 */
public class DataTransfersByFilter extends ModelSearchBase {

    /** The destination name. */
    private final String destinationName;

    /** The dissemination stream. */
    private final String disseminationStream;

    /** The data stream. */
    private final String dataStream;

    /** The data time. */
    private final String dataTime;

    /** The status code. */
    private final String statusCode;

    /** The date. */
    private final Date date;

    /** The file name search. */
    private final String fileNameSearch;

    /** The scheduled before. */
    private Date scheduledBefore;

    /** The cursor. */
    private final DataBaseCursor cursor;

    /**
     * Instantiates a new data transfers by filter.
     *
     * @param dest
     *            the dest
     * @param disseminationStream
     *            the dissemination stream
     * @param dataStream
     *            the data stream
     * @param dataTime
     *            the data time
     * @param status
     *            the status
     * @param scheduledBefore
     *            the scheduled before
     * @param fileNameSearch
     *            the file name search
     * @param date
     *            the date
     * @param cursor
     *            the cursor
     */
    public DataTransfersByFilter(final String dest, final String disseminationStream, final String dataStream,
            final String dataTime, final String status, final Date scheduledBefore, final String fileNameSearch,
            final Date date, final DataBaseCursor cursor) {
        this.destinationName = dest;
        this.disseminationStream = disseminationStream;
        this.dataStream = dataStream;
        this.dataTime = dataTime;
        this.statusCode = status;
        this.date = date == null ? new Date(0) : date;
        this.fileNameSearch = fileNameSearch;
        this.scheduledBefore = scheduledBefore;
        this.cursor = cursor;
        this.setCacheable(false);
    }

    /**
     * Instantiates a new data transfers by filter.
     *
     * @param dest
     *            the dest
     * @param disseminationStream
     *            the dissemination stream
     * @param dataStream
     *            the data stream
     * @param dataTime
     *            the data time
     * @param status
     *            the status
     * @param scheduledBefore
     *            the scheduled before
     * @param fileNameSearch
     *            the file name search
     * @param date
     *            the date
     */
    public DataTransfersByFilter(final String dest, final String disseminationStream, final String dataStream,
            final String dataTime, final String status, final Date scheduledBefore, final String fileNameSearch,
            final Date date) {
        this.destinationName = dest;
        this.disseminationStream = disseminationStream;
        this.dataStream = dataStream;
        this.dataTime = dataTime;
        this.statusCode = status;
        this.date = date == null ? new Date(0) : date;
        this.fileNameSearch = fileNameSearch;
        this.scheduledBefore = scheduledBefore;
        this.cursor = null;
        this.setCacheable(false);
    }

    /**
     * Gets the data base cursor.
     *
     * @return the data base cursor
     */
    public DataBaseCursor getDataBaseCursor() {
        return this.cursor;
    }

    /**
     * Checks for scheduled before.
     *
     * @return true, if successful
     */
    public boolean hasScheduledBefore() {
        return this.scheduledBefore != null;
    }

    /**
     * Gets the scheduled before.
     *
     * @return the scheduled before
     */
    public Date getScheduledBefore() {
        return this.scheduledBefore;
    }

    /**
     * Sets the scheduled before.
     *
     * @param d
     *            the new scheduled before
     */
    public void setScheduledBefore(final Date d) {
        this.scheduledBefore = d;
    }

    /**
     * Checks for date.
     *
     * @return true, if successful
     */
    public boolean hasDate() {
        return this.date.getTime() != 0;
    }

    /**
     * Gets the data stream.
     *
     * @return Returns the dataStream.
     */
    public String getDataStream() {
        return dataStream;
    }

    /**
     * Gets the data time.
     *
     * @return Returns the dataTime.
     */
    public String getDataTime() {
        return dataTime;
    }

    /**
     * Gets the date.
     *
     * @return Returns the date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Gets the destination name.
     *
     * @return Returns the destination.
     */
    public String getDestinationName() {
        return destinationName;
    }

    /**
     * Gets the dissemination stream.
     *
     * @return Returns the disseminationStream.
     */
    public String getDisseminationStream() {
        return disseminationStream;
    }

    /**
     * Gets the status code.
     *
     * @return Returns the statusCode.
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the file name search.
     *
     * @return the file name search
     */
    public String getFileNameSearch() {
        return this.fileNameSearch;
    }
}
