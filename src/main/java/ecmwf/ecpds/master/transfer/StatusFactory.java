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

package ecmwf.ecpds.master.transfer;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * A factory for creating Status objects.
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

import java.sql.Timestamp;

import ecmwf.common.database.DataTransfer;
import ecmwf.common.database.Destination;

/**
 * A factory for creating Status objects.
 */
public final class StatusFactory {
    /** The Constant INIT. */
    public static final String INIT = "INIT";

    /** The Constant INTR. */
    public static final String INTR = "INTR";

    /** The Constant HOLD. */
    public static final String HOLD = "HOLD";

    /** The Constant DONE. */
    public static final String DONE = "DONE";

    /** The Constant WAIT. */
    public static final String WAIT = "WAIT";

    /** The Constant STOP. */
    public static final String STOP = "STOP";

    /** The Constant EXEC. */
    public static final String EXEC = "EXEC";

    /** The Constant RETR. */
    public static final String RETR = "RETR";

    /** The Constant FAIL. */
    public static final String FAIL = "FAIL";

    /** The Constant IDLE. */
    public static final String IDLE = "IDLE";

    /** The Constant SCHE. */
    public static final String SCHE = "SCHE";

    /** The Constant FETC. */
    public static final String FETC = "FETC";

    /** The Constant RSTR. */
    public static final String RSTR = "RSTR";

    /** The Constant dataTransferMapping. */
    public static final String[][] dataTransferMapping = { { INIT, "Arriving" }, { SCHE, "Preset" },
            { FETC, "Fetching" }, { HOLD, "StandBy" }, { WAIT, "Queued" }, { EXEC, "Transferring" }, { DONE, "Done" },
            { RETR, "ReQueued" }, { STOP, "Stopped" }, { FAIL, "Failed" }, { INTR, "Interrupted" } };

    /** The Constant destinationMapping. */
    public static final String[][] destinationMapping = { { INIT, "Initialized" }, { EXEC, "Running" },
            { SCHE, "Waiting" }, { FAIL, "NoHosts" }, { INTR, "Interrupted" }, { WAIT, "Idle" }, { DONE, "Resending" },
            { RETR, "Retrying" }, { RSTR, "Restarting" }, { STOP, "Stopped" }, { HOLD, "Failed" },
            { IDLE, "Sleeping" } };

    /** The Constant productMapping. */
    public static final String[][] productMapping = { { INIT, "Expected" }, { EXEC, "Started" },
            { DONE, "Completed" } };

    /**
     * Gets the data transfer status name.
     *
     * @param restrictedUser
     *            the restricted user
     * @param transfer
     *            the transfer
     * @param statusCode
     *            the status code
     *
     * @return the data transfer status name
     */
    public static String getDataTransferStatusName(final boolean restrictedUser, final DataTransfer transfer,
            final String statusCode) {
        final String statusName;
        if (restrictedUser && WAIT.equals(statusCode)
                && transfer.getScheduledTime().after(new Timestamp(System.currentTimeMillis()))) {
            statusName = "Planned";
        } else {
            statusName = getDataTransferStatusName(restrictedUser, statusCode);
        }
        final var userName = transfer.getUserStatus();
        if (userName != null) {
            return statusName + "-" + userName;
        }
        return statusName;
    }

    /**
     * Gets the data transfer status name.
     *
     * @param restrictedUser
     *            the restricted user
     * @param statusCode
     *            the status code
     *
     * @return the data transfer status name
     */
    public static String getDataTransferStatusName(final boolean restrictedUser, final String statusCode) {
        for (final String[] mapping : dataTransferMapping) {
            if (mapping[0].equals(statusCode)) {
                return mapping[1];
            }
        }
        return statusCode;
    }

    /**
     * Gets the data transfer status code.
     *
     * @param statusName
     *            the status name
     *
     * @return the data transfer status code
     */
    public static String getDataTransferStatusCode(final String statusName) {
        for (final String[] mapping : dataTransferMapping) {
            if (mapping[1].equals(statusName)) {
                return mapping[0];
            }
        }
        return statusName;
    }

    /**
     * Gets the destination status name.
     *
     * @param destination
     *            the destination
     * @param statusCode
     *            the status code
     *
     * @return the destination status name
     */
    public static String getDestinationStatusName(final Destination destination, final String statusCode) {
        final var statusName = getDestinationStatusName(statusCode);
        if (destination.getActive() || !WAIT.equals(statusCode) && !SCHE.equals(statusCode)) {
            final var userName = destination.getUserStatus();
            if (userName != null) {
                return statusName + "-" + userName;
            }
        }
        return statusName;
    }

    /**
     * Gets the destination status name.
     *
     * @param statusCode
     *            the status code
     *
     * @return the destination status name
     */
    public static String getDestinationStatusName(final String statusCode) {
        for (final String[] mapping : destinationMapping) {
            if (mapping[0].equals(statusCode)) {
                return mapping[1];
            }
        }
        return statusCode;
    }

    /**
     * Gets the destination status code.
     *
     * @param statusName
     *            the status name
     *
     * @return the destination status code
     */
    public static String getDestinationStatusCode(final String statusName) {
        for (final String[] mapping : destinationMapping) {
            if (mapping[1].equals(statusName)) {
                return mapping[0];
            }
        }
        return statusName;
    }

    /**
     * Gets the product status name.
     *
     * @param statusCode
     *            the status code
     *
     * @return the product status name
     */
    public static String getProductStatusName(final String statusCode) {
        for (final String[] mapping : productMapping) {
            if (mapping[0].equals(statusCode)) {
                return mapping[1];
            }
        }
        return statusCode;
    }

    /**
     * Gets the product status code.
     *
     * @param statusName
     *            the status name
     *
     * @return the product status code
     */
    public static String getProductStatusCode(final String statusName) {
        for (final String[] mapping : productMapping) {
            if (mapping[1].equals(statusName)) {
                return mapping[0];
            }
        }
        return statusName;
    }

    /**
     * Helper to get the status names.
     *
     * @param mapping
     *            the mapping
     *
     * @return the status names
     */
    private static String[] getStatusNames(final String[][] mapping) {
        final var result = new String[mapping.length];
        for (var i = 0; i < mapping.length; i++) {
            result[i] = mapping[i][1];
        }
        return result;
    }

    /**
     * Gets the status codes.
     *
     * @param mapping
     *            the mapping
     *
     * @return the status codes
     */
    private static String[] getStatusCodes(final String[][] mapping) {
        final var result = new String[mapping.length];
        for (var i = 0; i < mapping.length; i++) {
            result[i] = mapping[i][0];
        }
        return result;
    }

    /**
     * Gets all destination status names.
     *
     * @return the list of destination status names
     */
    public static String[] getDestinationStatusNames() {
        return getStatusNames(destinationMapping);
    }

    /**
     * Gets all data transfer status names.
     *
     * @return the list of data transfer status names
     */
    public static String[] getDataTransferStatusNames() {
        return getStatusNames(dataTransferMapping);
    }

    /**
     * Gets all destination status codes.
     *
     * @return the list of destination status codes
     */
    public static String[] getDestinationStatusCodes() {
        return getStatusCodes(destinationMapping);
    }

    /**
     * Gets all data transfer status codes.
     *
     * @return the list of data transfer status codes
     */
    public static String[] getDataTransferStatusCodes() {
        return getStatusCodes(dataTransferMapping);
    }
}
