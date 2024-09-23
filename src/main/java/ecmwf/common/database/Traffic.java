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
public final class Traffic extends DataBaseObject {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6818833292746851760L;

    /** The ban date. */
    protected String BAN_DATE;

    /** The ban bytes. */
    protected long BAN_BYTES = 0;

    /** The ban duration. */
    protected long BAN_DURATION = 0;

    /** The ban files. */
    protected int BAN_FILES = 0;

    /**
     * Gets the date.
     *
     * @return the date
     */
    public String getDate() {
        return BAN_DATE;
    }

    /**
     * Sets the date.
     *
     * @param date
     *            the new date
     */
    public void setDate(final String date) {
        BAN_DATE = date;
    }

    /**
     * Gets the bytes.
     *
     * @return the bytes
     */
    public long getBytes() {
        return BAN_BYTES;
    }

    /**
     * Sets the bytes.
     *
     * @param bytes
     *            the new bytes
     */
    public void setBytes(final long bytes) {
        BAN_BYTES = bytes;
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    public long getDuration() {
        return BAN_DURATION;
    }

    /**
     * Sets the duration.
     *
     * @param duration
     *            the new duration
     */
    public void setDuration(final long duration) {
        BAN_DURATION = duration;
    }

    /**
     * Gets the files.
     *
     * @return the files
     */
    public int getFiles() {
        return BAN_FILES;
    }

    /**
     * Sets the files.
     *
     * @param files
     *            the new files
     */
    public void setFiles(final int files) {
        BAN_FILES = files;
    }

    /**
     * Update.
     *
     * @param bytes
     *            the bytes
     * @param duration
     *            the duration
     * @param files
     *            the files
     */
    public synchronized void update(final long bytes, final long duration, final int files) {
        BAN_BYTES += bytes;
        BAN_DURATION += duration;
        BAN_FILES += files;
    }

    /**
     * Clear.
     */
    public synchronized void clear() {
        BAN_BYTES = 0;
        BAN_DURATION = 0;
        BAN_FILES = 0;
    }
}
