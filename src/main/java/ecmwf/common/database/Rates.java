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

package ecmwf.common.database;

/**
 * ECMWF Product Data Store (OpenPDS) Project.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 *
 * @version 6.7.7
 *
 * @since 2024-07-01
 */

public class Rates extends DataBaseObject {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6609721102924722194L;

    /** The daf get time. */
    protected String DATE;

    /** The file system. */
    protected Integer DAF_FILE_SYSTEM;

    /** The daf size. */
    protected long DAF_SIZE;

    /** The count. */
    protected long COUNT;

    /** The trg name. */
    protected String TRG_NAME;

    /** The daf get host. */
    protected String DAF_GET_HOST;

    /** The daf get duration. */
    protected long DAF_GET_DURATION;

    /**
     * Instantiates a new statistics.
     */
    public Rates() {
    }

    /**
     * Sets the gets the duration.
     *
     * @param param
     *            the new gets the duration
     */
    public void setGetDuration(final long param) {
        DAF_GET_DURATION = param;
    }

    /**
     * Sets the gets the duration.
     *
     * @param param
     *            the new gets the duration
     */
    public void setGetDuration(final String param) {
        DAF_GET_DURATION = Long.parseLong(param);
    }

    /**
     * Gets the gets the duration.
     *
     * @return the gets the duration
     */
    public long getGetDuration() {
        return DAF_GET_DURATION;
    }

    /**
     * Gets the gets the host.
     *
     * @return the gets the host
     */
    public String getGetHost() {
        return DAF_GET_HOST;
    }

    /**
     * Sets the gets the host.
     *
     * @param param
     *            the new gets the host
     */
    public void setGetHost(final String param) {
        DAF_GET_HOST = param;
    }

    /**
     * Gets the transfer group name.
     *
     * @return the transfer group name
     */
    public String getTransferGroupName() {
        return TRG_NAME;
    }

    /**
     * Sets the transfer group name.
     *
     * @param param
     *            the new transfer group name
     */
    public void setTransferGroupName(final String param) {
        TRG_NAME = param;
    }

    /**
     * Gets the file system.
     *
     * @return the file system
     */
    public Integer getFileSystem() {
        return DAF_FILE_SYSTEM;
    }

    /**
     * Sets the file system.
     *
     * @param param
     *            the new file system
     */
    public void setFileSystem(final Integer param) {
        DAF_FILE_SYSTEM = param;
    }

    /**
     * Sets the file system.
     *
     * @param param
     *            the new file system
     */
    public void setFileSystem(final int param) {
        DAF_FILE_SYSTEM = param;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public long getSize() {
        return DAF_SIZE;
    }

    /**
     * Sets the size.
     *
     * @param param
     *            the new size
     */
    public void setSize(final long param) {
        DAF_SIZE = param;
    }

    /**
     * Sets the size.
     *
     * @param param
     *            the new size
     */
    public void setSize(final String param) {
        DAF_SIZE = Long.parseLong(param);
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public long getCount() {
        return COUNT;
    }

    /**
     * Sets the count.
     *
     * @param param
     *            the new count
     */
    public void setCount(final long param) {
        COUNT = param;
    }

    /**
     * Sets the count.
     *
     * @param param
     *            the new count
     */
    public void setCount(final String param) {
        COUNT = Long.parseLong(param);
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public String getDate() {
        return DATE;
    }

    /**
     * Sets the date.
     *
     * @param param
     *            the new date
     */
    public void setDate(final String param) {
        DATE = param;
    }
}
