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
 * ECMWF Product Data Store (OpenPDS) Project
 *
 * @author Daniel Varela Santoalla <sy8@ecmwf.int>, ECMWF.
 * @version 6.7.7
 * @since 2004-10-09
 */

import java.util.Date;

import ecmwf.web.dao.ModelSearchBase;

/**
 * The Class DataTransfersCountByFilter.
 */
public class DataTransfersCountByFilter extends ModelSearchBase {

    /** The Constant DISS. */
    private static final String DISS = "target";

    /** The Constant DSTR. */
    private static final String DSTR = "stream";

    /** The Constant DTIME. */
    private static final String DTIME = "time";

    /** The Constant STAT. */
    private static final String STAT = "status";

    /** The Constant ALL. */
    private static final String ALL = "All";

    /** The name to get. */
    private final String nameToGet;

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

    /** The file name search. */
    private final String fileNameSearch;

    /** The date. */
    private Date date;

    /** The scheduled before. */
    private Date scheduledBefore;

    /**
     * Get count without time restrictions. Basic constructor
     *
     * @param nameToGet
     *            the name to get
     * @param destName
     *            the dest name
     * @param disseminationStream
     *            the dissemination stream
     * @param dataStream
     *            the data stream
     * @param dataTime
     *            the data time
     * @param status
     *            the status
     * @param fileNameSearch
     *            the file name search
     */
    public DataTransfersCountByFilter(final String nameToGet, final String destName, final String disseminationStream,
            final String dataStream, final String dataTime, final String status, final String fileNameSearch) {
        this.nameToGet = nameToGet;
        this.destinationName = destName;
        this.disseminationStream = disseminationStream;
        this.dataStream = dataStream;
        this.dataTime = dataTime;
        this.statusCode = status;
        this.fileNameSearch = fileNameSearch;
        this.date = new Date(0);
        this.setCacheable(false);

    }

    /**
     * Get count for a given period.
     *
     * @param nameToGet
     *            the name to get
     * @param destName
     *            the dest name
     * @param disseminationStream
     *            the dissemination stream
     * @param dataStream
     *            the data stream
     * @param dataTime
     *            the data time
     * @param status
     *            the status
     * @param fileNameSearch
     *            the file name search
     * @param date
     *            the date
     */
    public DataTransfersCountByFilter(final String nameToGet, final String destName, final String disseminationStream,
            final String dataStream, final String dataTime, final String status, final String fileNameSearch,
            final Date date) {
        this(nameToGet, destName, disseminationStream, dataStream, dataTime, status, fileNameSearch);
        this.date = date;
    }

    /**
     * Get count for a given period when the scheduled date is before a given date.
     *
     * @param nameToGet
     *            the name to get
     * @param destName
     *            the dest name
     * @param disseminationStream
     *            the dissemination stream
     * @param dataStream
     *            the data stream
     * @param dataTime
     *            the data time
     * @param status
     *            the status
     * @param fileNameSearch
     *            the file name search
     * @param date
     *            the date
     * @param scheduledBefore
     *            the scheduled before
     */
    public DataTransfersCountByFilter(final String nameToGet, final String destName, final String disseminationStream,
            final String dataStream, final String dataTime, final String status, final String fileNameSearch,
            final Date date, final Date scheduledBefore) {
        this(nameToGet, destName, disseminationStream, dataStream, dataTime, status, fileNameSearch, date);
        this.scheduledBefore = scheduledBefore;
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
     * Gets the name to get.
     *
     * @return the name to get
     */
    public String getNameToGet() {
        return this.nameToGet;
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
     * Gets the data stream.
     *
     * @return Returns the dataStream.
     */
    public String getDataStream() {
        return DSTR.equals(this.nameToGet) ? ALL : dataStream;
    }

    /**
     * Gets the data time.
     *
     * @return Returns the dataStream.
     */
    public String getDataTime() {
        return DTIME.equals(this.nameToGet) ? ALL : dataTime;
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
        return DISS.equals(this.nameToGet) ? ALL : disseminationStream;
    }

    /**
     * Gets the status code.
     *
     * @return Returns the statusCode.
     */
    public String getStatusCode() {
        return STAT.equals(this.nameToGet) ? ALL : statusCode;
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
