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
public class DestinationExt extends Destination {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 2061060538074028082L;

    /** The pending transfers count. */
    protected int pendingTransfersCount = 0;

    /** The min queue time. */
    protected java.sql.Timestamp minQueueTime = null;

    /**
     * Instantiates a new destination ext.
     */
    public DestinationExt() {
    }

    /**
     * Instantiates a new destination ext.
     *
     * @param name
     *            the name
     */
    public DestinationExt(final String name) {
        super(name);
    }

    /**
     * Gets the min queue time.
     *
     * @return the min queue time
     */
    public java.sql.Timestamp getMinQueueTime() {
        return minQueueTime;
    }

    /**
     * Gets the pending transfer count.
     *
     * @return the pending transfer count
     */
    public int getPendingTransferCount() {
        return pendingTransfersCount;
    }
}
